/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import de.rcenvironment.executor.commons.ExecutorException;
import de.rcenvironment.executor.python.PythonExecutionContext;
import de.rcenvironment.executor.python.PythonExecutor;
import de.rcenvironment.executor.python.PythonExecutorFactory;


/**
 * Creates {@link PythonExecutor} objects and handles related stuff, e.g. find and provide present Python
 * installations.
 * 
 * @version $LastChangedRevision: 0$
 * @author Arne Bachmann
 */
public class PythonExecutorFactoryImpl implements PythonExecutorFactory {
    
    /**
     * The python wrapper template.
     */
    private final String template;
    
    
    /**
     * Constructor.
     * 
     * @throws ExecutorException if something went wrong
     */
    public PythonExecutorFactoryImpl() throws ExecutorException {
        try {
            template = loadTemplate();
        } catch (final IOException e) {
            throw new ExecutorException("Could not load template from bundle", e);
        }
    }

    @Override
    public PythonExecutor createExecutor(final PythonExecutionContext executionContext) throws ExecutorException {
        if (!executionContext.containsKey(PythonExecutionContext.TEMPLATE)) {
            executionContext.setWrapperTemplate(template);
        }
        final String os = System.getProperty("os.name", /* fallback */ "Linux");
        if (os.startsWith("Windows")) {
            executionContext.setSystemType(SystemType.Windows);
        } else if (os.toLowerCase().indexOf("linux") >= 0) {
            executionContext.setSystemType(SystemType.Linux);
        }
        return new PythonExecutorImpl(executionContext);
    }
    
    /**
     * Loads the Python wrapper script template.
     * 
     * @return the string containing the template
     * @throws IOException If loading fails
     */
    private String loadTemplate() throws IOException {
        InputStream is = getClass().getResourceAsStream("/wrapper-template.py");
        final InputStreamReader reader = new InputStreamReader(is);
        final StringWriter writer = new StringWriter();
        final int maxBufferSize = 4096;
        final char[] buffer = new char[maxBufferSize];
        int size;
        while ((size = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, size);
        }
        reader.close();
        final String returnValue = writer.toString();
        writer.close();
        return returnValue;
    }
    
}
