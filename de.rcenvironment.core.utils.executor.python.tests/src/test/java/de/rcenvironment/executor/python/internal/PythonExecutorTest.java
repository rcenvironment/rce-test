/*
 * Copyright (C) 2010-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python.internal;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import de.rcenvironment.commons.SerializableList;
import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.commons.variables.BoundVariable;
import de.rcenvironment.commons.variables.VariableType;
import de.rcenvironment.executor.commons.ExecutorException;
import de.rcenvironment.executor.python.PythonExecutionContext;
import de.rcenvironment.executor.python.PythonExecutionResult;
import de.rcenvironment.executor.python.PythonExecutor;
import de.rcenvironment.executor.python.PythonExecutorFactory;


/**
 * Tests if the executor works.
 *
 * @version $LastChangedRevision: 0$
 * @author Arne Bachmann
 */
public class PythonExecutorTest {
    
    private static final String B = "b";

    /**
     * Test constant.
     */
    private static final double VALUE = 13D;

    /**
     * The factory instance.
     */
    private PythonExecutorFactory factory;
    
    /**
     * An execution context.
     */
    private PythonExecutionContext context;
    
    /**
     * The executor.
     */
    private PythonExecutor executor;
    
    
    /**
     * Tests the code replacement.
     */
    @Test
    public void testCreateEscapedPythonString() {
        final String orig = "c\"d'''f\"\"\"g'";
        final String exp = "base64.b64decode(\"YyJkJycnZiIiImcn\")";
        final String res = PythonExecutorImpl.createEscapedPythonString(orig);
        assertThat(res, is(exp));
    }
    
    /**
     * Initialize the executor if there is a python installed.
     * 
     * @throws ExecutorException For any error
     * @throws  ExecutorException a
     * @throws IOException b
     */
    @Before
    public void setUp() throws ExecutorException, IOException {
        factory = new PythonExecutorFactoryImpl();
    }
    
    /**
     * Test if a script really executes and returns a value.
     * 
     * @throws ExecutorException Fail on exceptions
     * @throws InterruptedException Also
     */
    @SuppressWarnings({ "unchecked", "serial", "boxing" })
    @Test
    public void testWrappedPythonScript() throws ExecutorException, InterruptedException {
        if (executor == null) {
            return;
        }
        context.setInputVariables(new ArrayList<BoundVariable>() {
            {
                add(new BoundVariable("a", VariableType.Integer).setIntegerValue(10L));
                add(new BoundVariable(B, VariableType.Real).setRealValue(VALUE));
                add(new BoundVariable("c", VariableType.Logic).setLogicValue(true));
                add(new BoundVariable("d", VariableType.String).setStringValue("bla"));
                add(new BoundVariable("z", VariableType.Integer).setIntegerValue(9));
            }
        });
        context.setOutputVariables(context.get("inputVariables", SerializableList.class));
        context.setInputArrays(new ArrayList<VariantArray>() {
            {
                add(new VariantArray("e", 2, 2).setValue(10D, 0, 0).setValue(true, 0, 1).setValue(10, 1, 0).setValue("bla", 1, 1));
            }
        });
        context.setOutputArrays(new ArrayList<String>() {
            {
                add("f"); // unknown returned size!
            }
        });
        context.setPythonScript("print a,b,c,d,e\na=1; b=3.0; c=False; d='blupp'; del z; " // unset z
            + "f = [[None for col in xrange(2)] for row in xrange(2)]; f[0][0]=0; f[0][1]=1; f[1][0]=2; f[1][1]=3\n");
        PythonExecutionResult result;
        try {
            result = executor.execute().get();
            final StringBuilder sb = new StringBuilder();
            for (final BoundVariable variable: result.getOutputVariables()) {
                final String add;
                if (variable.getType() == VariableType.String) {
                    add = "'" + variable.getValue() + "'";
                } else {
                    add = (String) variable.getValue().toString();
                }
                sb.append(variable.getName())
                    .append("=")
                    .append(add)
                    .append("\n");
            }
            for (int row = 0; row < 2; row ++) {
                for (int col = 0; col < 2; col ++) {
                    sb.append(result.getOutputArrays().get("f").getValue(row, col).getStringValue());
                }
            }
            assertThat(sb.toString(), is("a=1\nb=3.0\nc=false\nd='blupp'\n0123"));
        } catch (final java.util.concurrent.ExecutionException e) {
            fail(e.toString());
        }
    }
    
    /**
     * Test no return, but this test isn't able to reproduce original error.
     * 
     * @throws InterruptedException A
     */
    @SuppressWarnings({ "serial", "boxing" })
    @Test
    public void testNoReturn() throws InterruptedException {
        if (executor == null) {
            return;
        }
        context.setInputArrays(new ArrayList<VariantArray>());
        context.setInputVariables(new ArrayList<BoundVariable>() {
            {
                add(new BoundVariable("a", VariableType.Integer).setIntegerValue(3));
                add(new BoundVariable(B, VariableType.String).setStringValue("rrdsg"));
                add(new BoundVariable("c", VariableType.Real).setRealValue(3.0D));
            }
        });
        context.setOutputArrays(new ArrayList<String>());
        context.setOutputVariables(new ArrayList<BoundVariable>() {
            {
                add(new BoundVariable(B, VariableType.String).setStringValue("rrdsg"));
            }
        });
        context.setPythonScript("del a; b = 'xx'; del c");
        try {
            final PythonExecutionResult result = executor.execute().get();
            assertThat(result.getOutputVariables().size(), is(1));
            assertThat(result.getOutputVariables().get(0).getName(), is(B));
            assertThat(result.getOutputArrays().size(), is(0));
        } catch (final java.util.concurrent.ExecutionException e) {
            fail(e.toString());
        }
        
    }
    
}
