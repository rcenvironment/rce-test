/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.executor.python.internal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.SerializableList;
import de.rcenvironment.commons.SerializableMap;
import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.commons.variables.BoundVariable;
import de.rcenvironment.commons.variables.TypedValue;
import de.rcenvironment.commons.variables.VariableType;
import de.rcenvironment.executor.commons.ExecutionException;
import de.rcenvironment.executor.commons.script.AbstractVariableExecutionContext;
import de.rcenvironment.executor.python.PythonExecutionContext;
import de.rcenvironment.executor.python.PythonExecutionResult;
import de.rcenvironment.executor.python.PythonExecutor;
import de.rcenvironment.executor.python.PythonExecutorFactory;
import de.rcenvironment.executor.python.PythonExecutorFactory.SystemType;
import de.rcenvironment.rce.component.ComponentState;

/**
 * Executes a given Python script.
 * 
 * @version $LastChangedRevision: 10653 $
 * @author Arne Bachmann
 */
public class PythonExecutorImpl implements PythonExecutor {

    /**
     * The log instance.
     */
    private static final Log LOGGER = LogFactory.getLog(PythonExecutorImpl.class);
    
    private static final String NONE = "None";
    private static final String GLOBAL = "    global ";
    private static final String PRINT = "    print >> sys.stderr, '";
    private static final String PLUS = "' + ";
    private static final String ASSIGNMENT = " = ";
    private static final String TRY = "try:";
    private static final String EXCEPT = "except NameError, e:";
    private static final String PRINT_NOTHING = PRINT + "-'";
    private static final String COMMA = ",";
    private static final String INDENT = "    ";
    private static final String DM_VAR_MARKER = "_D_0_M_";
    private static final String ARRAY_MARKER = "_A_0_R_";
    private static final String NL = "\n";
    private static final String ESCAPED_DOUBLE_QUOTE = "\"";
    
    /**
     * Several times used error message.
     */
    private static final String VAR_ERROR = "Not all output variables could be filled from script. "
        + "Probably a variable was forgotten or has been renamed in the script.";

    /**
     * To execute runnables.
     */
    private static ExecutorService threadPool;

    /**
     * Context for script execution.
     */
    private PythonExecutionContext context;
    
    /**
     * STDOUT.
     */
    private InputStream stdoutStream;

    /**
     * STDERR.
     */
    private InputStream stderrStream;
    
    /**
     * Filtered stream.
     */
    private InputStream pipedStream;
    
    /**
     * Where everything is created in.
     */
    private File tempDir;
    
    static {
        threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Constructor with a predefined context containing all necessary information.
     * 
     * @param setupContext The context
     */
    public PythonExecutorImpl(final PythonExecutionContext setupContext) {
        context = setupContext;
    }

    @Override
    public Future<PythonExecutionResult> execute() {
        // check if necessary context is set
        try {
            context.hasValidTypes(new Hashtable<String, Class<? extends Serializable>>() {
                private static final long serialVersionUID = -7299376635757537820L;
                {
                    put(PythonExecutionContext.OS, String.class);
                    put(PythonExecutionContext.PYTHON_EXECUTABLE_PATH, String.class);
                    put(PythonExecutionContext.PYTHON_SCRIPT, String.class);
                    put(PythonExecutionContext.TEMPLATE, String.class);
                    put(AbstractVariableExecutionContext.INPUT_VARIABLES, SerializableList.class);
                    put(AbstractVariableExecutionContext.OUTPUT_VARIABLES, SerializableList.class);
                    put(AbstractVariableExecutionContext.INPUT_ARRAYS, SerializableMap.class);
                    put(AbstractVariableExecutionContext.OUTPUT_ARRAYS, SerializableList.class);
                    put(PythonExecutionContext.DM_HANDLES, SerializableMap.class);
                }
            });
        } catch (final IllegalStateException e) {
            throw new ExecutionException("Not all required properties have been set in the given execution context", e);
        }
        if (context.get(PythonExecutionContext.OS, String.class).equals(PythonExecutorFactory.SystemType.Unspecified.name())) {
            throw new ExecutionException("The operating system property has not been set");
        }
        
        // find temp directory for all intermediate files
        try {
            tempDir = TempFileUtils.getDefaultInstance().createManagedTempDir("pythonexecutor");
        } catch (final IOException e) {
            LOGGER.error("Could not create managed temp directory, falling back to default");
            try {
                final File tmp = File.createTempFile("prefix", "suffix");
                tempDir = tmp.getParentFile();
                tmp.delete(); // not needed
            } catch (final IOException e1) {
                LOGGER.error("Failed to fall back.");
                throw new ExecutionException("Unable to create temp file and directory", e1);
            }
        }
        
        // create python script
        try {
            final File scriptLocation = createTemporaryPythonScript();
            final String[] command;
            if (SystemType.valueOf(context.get(PythonExecutionContext.OS, String.class)) == SystemType.Windows) { 
                command = new String[] {
                    ESCAPED_DOUBLE_QUOTE + context.get(PythonExecutionContext.PYTHON_EXECUTABLE_PATH, String.class) + ESCAPED_DOUBLE_QUOTE,
                    "-u",
                    ESCAPED_DOUBLE_QUOTE + scriptLocation.getAbsolutePath() + ESCAPED_DOUBLE_QUOTE
                };
            } else { // Linux/Mac Type
                command = new String[] {
                    context.get(PythonExecutionContext.PYTHON_EXECUTABLE_PATH, String.class).replaceAll(" ", "\\ "),
                    "-u",
                    scriptLocation.getAbsolutePath().replaceAll(" ", "\\ ")
                };
            }
            LOGGER.debug("PythonExecutor executes command: " + command[0] + " " + command[1] + " " + command[2]);
            final Process process = Runtime.getRuntime().exec(command);
            stdoutStream = process.getInputStream();
            stderrStream = process.getErrorStream();
            final FilteredStderrPipe pipedStdErrStream = new FilteredStderrPipe(stderrStream);
            pipedStream = pipedStdErrStream.getFilteredStdErrStream();
            final Future<String> futureRawStderr = threadPool.submit(pipedStdErrStream);

            return threadPool.submit(new Callable<PythonExecutionResult>() {

                @Override
                public PythonExecutionResult call() throws Exception {
                    final String varLines = futureRawStderr.get();
                    final int exitCode = process.waitFor(); // but the process will already have finished when we got the streams' values
                    
                    // parse wrapper code variable return from stderr
                    final ParseResult result = parseResult(varLines);
                    
                    // remove managed temp dir if not in debug mode
                    if (!context.containsKey(DEBUG)
                            || (context.hasType(DEBUG, Boolean.class) && !context.get(DEBUG, Boolean.class).booleanValue())
                            || (context.hasType(DEBUG, String.class) && !Boolean.parseBoolean(context.get(DEBUG, String.class)))) {
                        TempFileUtils.getDefaultInstance().disposeManagedTempDirOrFile(tempDir);
                    }
                    if (result == null) {
                        return null;
                    }
                    return new PythonExecutionResult(result, exitCode);
                }
            });
            
        } catch (final IOException e) {
            throw new ExecutionException("Could not run python executable", e);
        }
    }
    
    @Override
    public InputStream getStderrStream() {
        return pipedStream;
    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.executor.python.PythonExecutor#getStdoutStream()
     */
    @Override
    public InputStream getStdoutStream() {
        return stdoutStream;
    }

    /**
     * This creates a temporary file containing the wrapped python script.
     * 
     * @return The file handle to execute later with the python interpreter
     * @throws IOException For any file error
     */
    private File createTemporaryPythonScript() throws IOException {
        final File temp = new File(tempDir, "script.py");
        final String template = context.get("template", String.class);
        String wrapper = template.replace("    pass # init", createInitWrapCode());
        wrapper = wrapper.replace("    pass # cleanup", createCleanupWrapCode());
        wrapper = wrapper.replace("pass # main", createMainCode());
        final FileWriter writer = new FileWriter(temp);
        writer.write(wrapper);
        writer.close();
        return temp;
    }

    /**
     * Create the wrapping code to set the input variables.
     * 
     * @return The created python code
     */
    @SuppressWarnings("unchecked")
    private String createInitWrapCode() {
        // find all names, including input and output to create a "global" statement
        final Set<String> allVariables = new LinkedHashSet<String>();
        for (final BoundVariable variable: (List<BoundVariable>) context.get(AbstractVariableExecutionContext.INPUT_VARIABLES,
                SerializableList.class)) {
            allVariables.add(variable.getName());
        }
        for (final BoundVariable variable: (List<BoundVariable>) context.get(AbstractVariableExecutionContext.OUTPUT_VARIABLES,
                SerializableList.class)) {
            allVariables.add(variable.getName());
        }
        for (final VariantArray array: (List<VariantArray>) context.get(AbstractVariableExecutionContext.INPUT_ARRAYS,
                SerializableList.class)) {
            allVariables.add(array.getName());
        }
        for (final String array: (List<String>) context.get(AbstractVariableExecutionContext.OUTPUT_ARRAYS,
                SerializableList.class)) {
            allVariables.add(array);
        }
        
        // create global statement for all variables and arrays
        final StringBuilder sb = new StringBuilder();
        final Iterator<String> it = allVariables.iterator();
        if (it.hasNext()) {
            sb.append(GLOBAL).append(it.next());
            while (it.hasNext()) {
                sb.append(COMMA).append(it.next());
            }
            sb.append(NL);
        }
        
        // create variables initializations
        for (final BoundVariable variable: (List<BoundVariable>) context.get(AbstractVariableExecutionContext.INPUT_VARIABLES,
                SerializableList.class)) {  
            String valueToAppend;
            if (variable.getType() == VariableType.String) {
                valueToAppend = createEscapedPythonString(variable.getStringValue());
            } else if (variable.getType() == VariableType.Logic) {
                if (variable.getLogicValue()) {
                    valueToAppend = "True";
                } else {
                    valueToAppend = "False";
                }
            } else {
                valueToAppend = variable.getStringValue();
                if (valueToAppend == null) {
                    valueToAppend = NONE;
                }
            }
            sb.append(INDENT) // indentation because inside a def: block
                    .append(variable.getName())
                    .append(ASSIGNMENT)
                    .append(valueToAppend).append(NL);
            allVariables.remove(variable.getName()); // keep only outputs not yet initialized
        }
        // create array initializations
        int count = 0;
        for (final VariantArray array: (List<VariantArray>) context.get(AbstractVariableExecutionContext.INPUT_ARRAYS,
                SerializableList.class)) {
            createArrayInitCode(sb, array, count ++);
            allVariables.remove(array.getName()); // keep only outputs not yet initialized
        }
        // init data management paths
        if (context.getDataManagementHandles() != null) {
            for (final Entry<String, String> dmEntry: context.getDataManagementHandles().entrySet()) {
                final String path = dmEntry.getValue();
                sb.append("    _dm_['").append(dmEntry.getKey()).append("'] = ")
                        .append(createEscapedPythonString(path)).append(NL);
            }
        }
        sb.append("    pass\n"); // in case there are no vars at all
        return sb.toString();
    }
    
    /**
     * Write out one array's contents to the provided appendable.
     * 
     * @param sb The appendable
     * @param array The array
     */
    private void createArrayInitCode(final StringBuilder sb, final VariantArray array, final int count) {
        final int[] dims = array.getDimensions();
        sb.append(INDENT).append(array.getName()).append(ASSIGNMENT); // build a list comprehension for initialization
        for (int i = 0; i < dims.length; i ++) {
            sb.append("[");
        }
        sb.append(NONE); // create initial values for all cells (all None)
        for (int i = 0; i < dims.length; i ++) {
            sb.append(" for d").append(Integer.toHexString(i)).append(" in xrange(")
                .append(Integer.toString(dims[dims.length - 1 - i])).append(")]"); 
        }
        sb.append(NL);
        final int size = array.getSize();
        final File tempFile = new File(tempDir, "array_init_" + Integer.toString(count) + ".dat");
        try {
            final FileWriter fw = new FileWriter(tempFile);
            for (int i = 0; i < size; i ++) {
                final int[] index = array.getIndex(i); // singular indexes
                fw.append(array.getName());
                for (int j = 0; j < index.length; j ++) {
                    fw.append("[").append(Integer.toString(index[j])).append("]");
                }
                fw.append(ASSIGNMENT);
                final TypedValue value = array.getValue(index);
                if (value == null) {
                    fw.append(NONE);
                } else if (value.getType() == VariableType.String) {
                    fw.append(createEscapedPythonString(value.getStringValue()));
                } else if (value.getType() == VariableType.Logic) {
                    if (value.getLogicValue()) {
                        fw.append("True");
                    } else {
                        fw.append("False");
                    }
                } else if (value.getType() == VariableType.Empty) {
                    fw.append(NONE);
                } else {
                    fw.append(value.getStringValue());
                }
                fw.append(NL);
            }
            fw.close();
            sb.append(INDENT).append("__tm_p__ = open(").append(createEscapedPythonString(tempFile.getAbsolutePath()))
                .append(", 'r')").append(NL);
            sb.append(INDENT).append("for line in __tm_p__.xreadlines():").append(NL);
            sb.append(INDENT).append(INDENT).append("exec(line, {'").append(array.getName())
                .append("':").append(array.getName()).append(", 'base64': base64}, {})").append(NL);
            sb.append(INDENT).append("__tm_p__.close()").append(NL);
            sb.append(INDENT).append("os.remove(").append(createEscapedPythonString(tempFile.getAbsolutePath()) + ")")
                .append(NL);
        } catch (final IOException e) {
            LOGGER.error("Could not create temporary array input file");
        }
    }
    
    /**
     * Create the wrapping code to print out the output variables.
     * 
     * @return The created Python code
     */
    private String createCleanupWrapCode() {
        // create "global" statements for variables and arrays
        final StringBuilder sb = new StringBuilder();
        @SuppressWarnings("unchecked") final List<BoundVariable> variables =
            context.get(AbstractVariableExecutionContext.OUTPUT_VARIABLES, SerializableList.class);
        final Iterator<BoundVariable> itva = variables.iterator();
        if (itva.hasNext()) {
            sb.append(GLOBAL).append(itva.next().getName());
            while (itva.hasNext()) {
                sb.append(COMMA).append(itva.next().getName());
            }
            sb.append(" # variables").append(NL);
        }
        @SuppressWarnings("unchecked") final List<String> arrays =
            context.get(AbstractVariableExecutionContext.OUTPUT_ARRAYS, SerializableList.class);
        final Iterator<String> itar = arrays.iterator();
        if (itar.hasNext()) {
            sb.append(GLOBAL).append(itar.next());
            while (itar.hasNext()) {
                sb.append(COMMA).append(itar.next());
            }
            sb.append(" # arrays").append(NL);
        }
        
        // create output values printing to stderr after a special separator code
        final StringBuilder pythonCode = new StringBuilder();
        pythonCode.append("print >> sys.stderr, '" + VARS_SEPARATOR + "'" + NL); // end marker
        for (final BoundVariable variable: variables) {
            if (variable.getType() == VariableType.String) {
                pythonCode.append(TRY).append(NL)
                    .append(PRINT)
                    .append(variable.getName()).append(VAR_SEPARATOR).append(PLUS)
                    .append("base64.b64encode(").append(variable.getName()).append(")").append(NL)
                    .append(EXCEPT).append(NL)
                    .append(PRINT_NOTHING).append(NL);
            } else {
                pythonCode.append(TRY).append(NL)
                    .append(PRINT)
                    .append(variable.getName()).append(VAR_SEPARATOR).append(PLUS)
                    .append("repr(").append(variable.getName()).append(")").append(NL)
                    .append(EXCEPT).append(NL)
                    .append(PRINT_NOTHING).append(NL);
            }
        }
        if (arrays.size() > 0) {
            pythonCode.append("__fd__ = None").append(NL);
        }
        for (final String array: arrays) {
            pythonCode.append(TRY).append(NL);
            pythonCode.append("    __tmpf__ = tempfile.mkstemp(); __fd__ = open(__tmpf__[1], 'w')").append(NL);
            pythonCode.append(PRINT).append(ARRAY_MARKER).append(array).append(ARRAY_MARKER)
                .append("' + (','.join([str(x) for x in ___dims___(")
                .append(array).append(")]))").append(" + '" + ARRAY_MARKER + PLUS).append("repr(__tmpf__[1])[1:-1]").append(NL);
            pythonCode.append("    for x in ___array___(\"").append(array).append("\",")
                .append(array).append("): print >> __fd__, x").append(NL);
            pythonCode.append("    __fd__.close()").append(NL);
            pythonCode.append(EXCEPT).append(NL)
                .append("    if __fd__ is not None: ").append(NL) // don't write anything for undef'd arrays
                .append("        __fd__.close()").append(NL);
        }
        
        // TODO remove this, stems from 2/3 compatibility
        pythonCode.append("sys.stderr.flush()").append(NL); // important! missing this line was a bug thus resolved 
        sb.append(indent(pythonCode.toString(), 4));
        return sb.toString();
    }

    /**
     * Create the indented original script code.
     * 
     * @return The created Python code
     */
    private String createMainCode() {
        return createEscapedPythonString(context.get(PythonExecutionContext.PYTHON_SCRIPT, String.class).replaceAll("[\\r\\n\\f]+", "\n"));
    }
    
    /**
     * Parse the output of the script to analyze return values.
     * 
     * @param stderr The output created by the executed script
     * @return The list return object or null if nothing found.
     * (at)throws ExecutionException If we found less output lines than variables expected
     */
    private ParseResult parseResult(final String stderr) {
        @SuppressWarnings("unchecked") final List<BoundVariable> outputVariables =
            context.get(AbstractVariableExecutionContext.OUTPUT_VARIABLES, SerializableList.class);
        final Map<String, String> dmHandlesOut = new Hashtable<String, String>();
        final List<VariantArray> outputArrays = new ArrayList<VariantArray>();
        
        final String[] lines = stderr.split("[\\r\\n\\f]+", /* keep trailing empty lines */ -1); // separate lines
        if (lines.length == 0) {
            return null;
        }
        if (lines.length < outputVariables.size()) {
            throw new ExecutionException("Cannot parse output variables: not enough outputs gathered");
        }
        
        // set variable return values
        int index = 0;
        final Set<BoundVariable> unsetVariables = new HashSet<BoundVariable>(); // variables that were deleted by the user on purpose
        for (final BoundVariable var: outputVariables) {
            final String line = lines[index ++];
            if (line.startsWith(UNSET)) {
                unsetVariables.add(var);
                continue;
            }

            if (var.getType() == VariableType.String) {
                var.setValue(new String(Base64.decodeBase64(line.substring(var.getName().length() + VAR_SEPARATOR.length()))));
            } else {
                String stringValue = line.substring(var.getName().length() + VAR_SEPARATOR.length());
                if (stringValue.contains(ComponentState.FINISHED.name())) {
                    var.setEmptyValue();
                } else {
                    var.setValueFromString(stringValue);                        
                }
            }
            if (index > lines.length) { // less lines than expected
                LOGGER.error(VAR_ERROR);
            }
        }
        // parse data management and array return values, if any available (used in script by _dm_["name"] = "value")
    OUTER:
        while (index < lines.length) {
            final String l = lines[index ++];
            if (l.startsWith(DM_VAR_MARKER)) { // is a data management entry line
                final String line = new String(Base64.decodeBase64(l.substring(DM_VAR_MARKER.length()))); // transfer is encoded by wrapper
                final String[] tmp = line.split(DM_VAR_MARKER); // remove marker from line and split name from value
                if (tmp.length == 2) { // allow only correctly parsed lines
                    final String key = tmp[0]; // item zero is always empty due to _dm_ prefix
                    final String value = tmp[1];
                    dmHandlesOut.put(key, value);
                }
            } else if (l.startsWith(ARRAY_MARKER)) { // demarks the beginning of a new array
                final String[] tmpx = l.split(ARRAY_MARKER);
                final String arrayName = tmpx[1];
                final String[] dimensionString = tmpx[2].split(",");
                final String arrayFileName = tmpx[3];
                final int[] dimensions = new int[dimensionString.length];
                for (int i = 0; i < dimensions.length; i ++) {
                    dimensions[i] = Integer.parseInt(dimensionString[i]);
                }
                final VariantArray array = new VariantArray(arrayName, dimensions);
                final int size = array.getSize();
                try {
                    final LineNumberReader lnr = new LineNumberReader(new FileReader(arrayFileName));
                    int i = 0;
                    String nextLine = null;
                    while ((nextLine = lnr.readLine()) != null) {
                        final String contentLine = new String(Base64.decodeBase64(nextLine));
                        final String[] tmp = contentLine.split(ARRAY_MARKER);
                        if ((tmp.length == 3) || (tmp.length == 4)) { //3 = empty String, 4 = value non-empty
                            final String[] indexString = tmp[1].split(COMMA);
                            final String typeString = tmp[2]; // one out of [String, Integer, Real, Logic, Empty]
                            final int[] indexes = new int[indexString.length];
                            if (indexes.length > dimensions.length) {
                                LOGGER.error("Found array content with more dimensions than announced array");
                                continue; // OUTER
                            }
                            for (int j = 0; j < indexes.length; j ++) { // determine index position
                                indexes[j] = Integer.parseInt(indexString[j]);
                                if (indexes[j] >= dimensions[j]) {
                                    LOGGER.error("Found index too high for announced array");
                                    continue OUTER;
                                }
                            }
                            final VariableType type = VariableType.valueOf(typeString);
                            final TypedValue value;
                            if ((tmp.length == 3) || (type == VariableType.Empty)) {
                                value = new TypedValue(type); // with default value of its type
                            } else {
                                value = new TypedValue(type, tmp[3]); // additionally, set the value
                            }
                            array.setValue(value, array.getIndex(i ++));
                        }
                    }
                    if (i < size) {
                        LOGGER.error("Less array lines available than announced");
                    }
                    lnr.close();
                    if (!new File(arrayFileName).delete()) {
                        LOGGER.error("Could remove temporary array contents file");
                    }
                } catch (final IOException e) {
                    LOGGER.error("Could not read temporary array contents file", e);
                }
                if (!array.isMatrixComplete()) {
                    LOGGER.error("Return matrix for array " + array.getName() + " is not completely filled");
                }
                outputArrays.add(array);
            }
        }
        outputVariables.removeAll(unsetVariables);
        return new ParseResult(outputVariables, dmHandlesOut, outputArrays);
    }
    
    /**
     * Reliable escaping of arbitrary python code.
     * 
     * @param input The input code
     * @return The escaped unescape sequence
     */
    static String createEscapedPythonString(final String input) {
        final String encoded = Base64.encodeBase64String(input.getBytes());
        return "base64.b64decode(\"" + encoded.replaceAll("[\\\r\\\n\\\f]+", "") + "\")";
    }
    
    /**
     * Indent the given code by the number of space chars given.
     * 
     * @param code The code as a string
     * @param indentation The number if spaces to insert in front of every line
     * @return The Code created
     */
    static String indent(final String code, final int indentation) {
        String indent = "";
        for (int i = 0; i < indentation; i ++) {
            indent += " ";
        }
        return indent + code.replaceAll("\\n", "\n" + indent) + NL; // one more unnecessary line created here
    }
    
}
