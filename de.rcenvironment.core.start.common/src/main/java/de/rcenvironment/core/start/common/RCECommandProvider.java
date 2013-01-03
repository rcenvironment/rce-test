/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.start.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationService;

/**
 * {@link CommandProvider} for the 'rce' command on the equinox console.
 * 
 * @author Christian Weiss
 * @author Doreen Seider
 * @author Robert Mischke
 */
// TODO needs rework/cleanup; hard to read/maintain -- misc_ro
public class RCECommandProvider implements CommandProvider {

    private static final Log LOGGER = LogFactory.getLog(RCECommandProvider.class);

    private CommunicationService commService;

    /**
     * Handler for the rce command.
     * 
     * @param interpreter the provided {@link CommandInterpreter}
     * @return null
     */
    public Object _rce(final CommandInterpreter interpreter) {
        final List<String> arguments = getArguments(interpreter);
        if (arguments.size() >= 1) {
            final String handlerIdentifier = arguments.get(0);
            final Method handlerMethod = getHandlerMethod(handlerIdentifier);
            if (handlerMethod != null) {
                Object result = invokeHandlerMethod(handlerMethod, arguments, interpreter);
                interpreter.println(result);
                return null;
            }
        }
        interpreter.println(getHelp());
        return null;
    }

    protected List<String> getArguments(final CommandInterpreter interpreter) {
        final List<String> arguments = new LinkedList<String>();
        String argument;
        while ((argument = interpreter.nextArgument()) != null) {
            arguments.add(argument);
        }
        return Collections.unmodifiableList(arguments);
    }

    protected Method getHandlerMethod(final String name) {
        final String handlerMethodName = "rce" + name.substring(0, 1).toUpperCase() + name.substring(1);
        for (final Method method : getClass().getMethods()) {
            if (method.getName().equals(handlerMethodName) && method.getReturnType() == Object.class) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 2 && parameterTypes[0] == List.class
                    && parameterTypes[1] == CommandInterpreter.class) {
                    return method;
                }
            }
        }
        return null;
    }

    protected Object invokeHandlerMethod(final Method handlerMethod, final List<String> arguments,
        final CommandInterpreter interpreter) {
        try {
            return handlerMethod.invoke(this, arguments, interpreter);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invovation handler for the "rce stop" sub-command. Called from
     * {@link #invokeHandlerMethod(Method, List, CommandInterpreter)}.
     * 
     * @param arguments commant-line arguments
     * @param interpreter CommandInterpreter instance
     * @return Object null
     */
    public Object rceStop(final List<String> arguments, final CommandInterpreter interpreter) {
        Platform.shutdown();
        return null;
    }

    /**
     * Network-related commands.
     * 
     * @param arguments commant-line arguments
     * @param interpreter CommandInterpreter instance
     * @return String TODO check return value
     */
    public Object rceNet(final List<String> arguments, final CommandInterpreter interpreter) {
        String result = null;
        if (arguments.size() >= 1) {
            result = rceNetSubcommand(arguments.subList(1, arguments.size()));
        }
        if (result == null) {
            result = getHelp();
        }
        return result;
    }

    /**
     * Prints statistics about asynchronous tasks.
     * 
     * @param arguments commant-line arguments
     * @param interpreter CommandInterpreter instance
     * @return String TODO check return value
     */
    public Object rceTasks(final List<String> arguments, final CommandInterpreter interpreter) {
        String result = SharedThreadPool.getInstance().getFormattedStatistics();
        return result;
    }

    private String rceNetSubcommand(final List<String> arguments) {
        int argc = arguments.size();
        if (argc == 0) {
            // show network status
            return commService.getNetworkInformation();
        }
        String subCmd = arguments.get(0); // assumed to be non-null
        if (subCmd.equals("add") && argc == 2) {
            String contactPointStr = arguments.get(1);
            String result; // CheckStyle hack... much more readable than two returns, isn't it?
            try {
                commService.addRuntimeNetworkPeer(contactPointStr);
                result = "Connection successful";
            } catch (CommunicationException e) {
                result = "Connection failed: " + e;
            }
            return result;
        }
        return "" + argc;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.osgi.framework.console.CommandProvider#getHelp()
     */
    @Override
    public String getHelp() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("---RCE commands---\n");
        buffer.append("\trce net - show network status\n");
        buffer.append("\trce net add <contact point string> - add remote RCE platform\n");
        buffer.append("\trce stop - shut down RCE\n");
        return buffer.toString();
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        commService = newCommunicationService;
    }

}
