/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.python.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.core.gui.workflow.executor.properties.AbstractScriptSection;
import de.rcenvironment.rce.components.python.commons.PythonComponentConstants;


/**
 * "Properties" view tab for loading and editing Python script files.
 *
 * @author Markus Litz
 * @author Arne Bachmann
 * @author Doreen Seider
 */
public class ScriptSection extends AbstractScriptSection {
   
    private static final Log LOGGER = LogFactory.getLog(ScriptSection.class);

    public ScriptSection() {
        super(AbstractScriptSection.LOCAL_FILE | AbstractScriptSection.NEW_SCRIPT_FILE | AbstractScriptSection.NO_SCRIPT_FILENAME);
    }

  

    @Override
    public void refresh() {
        super.refresh();
        
        if (getProperty(PythonComponentConstants.SCRIPT) == null){
            try {
                final InputStream is;
                if (getClass().getResourceAsStream("/resources/defaultScript.py") == null) {
                    is = new FileInputStream("./resources/defaultScript.py");
                } else {
                    is = getClass().getResourceAsStream("/resources/defaultScript.py");
                }
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
                setProperty(PythonComponentConstants.SCRIPT, returnValue);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        
    }
    /**
     * 
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodePropertySection#aboutToBeShown()
     */
    @Override
    public void aboutToBeShown() {
        super.aboutToBeShown();
        refresh();
    }

}
