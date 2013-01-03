/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.gui.properties;


import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants;
import de.rcenvironment.rce.components.excel.commons.ExcelException;
import de.rcenvironment.rce.components.excel.commons.ExcelUtils;
import de.rcenvironment.rce.components.excel.commons.ExcelService;
import de.rcenvironment.rce.components.excel.commons.SimpleExcelService;
import de.rcenvironment.rce.gui.workflow.editor.validator.AbstractWorkflowNodeValidator;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidationMessage;


/**
 * A {@link AbstractWorkflowNodeValidator} implementation to validate the GUI of the Excel
 * component.
 * 
 * This implementation is special for VBA Macros because unstable OLE implementation should be used as least as possible
 * and this validation can only be used in MS Windows and MS Office environments. So therefore this class can be moved later 
 * to a special Windows bundle.
 * 
 * <p>
 * To be incorporated in the validation process, this validator is registered as an extension to the
 * extension point "de.rcenvironment.rce.gui.workflow.nodeValidators" in the plugin.xml.
 * </p>
 *
 * @author Markus Kunde
 */
public class ExcelMacroWorkflowNodeValidator extends AbstractWorkflowNodeValidator {
        
    @Override
    protected Collection<WorkflowNodeValidationMessage> validate() {
        ExcelService excelService = new SimpleExcelService();
        
        final List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();
        
        //Get all relevant validation items
        String excelFile = getProperty(ExcelComponentConstants.XL_FILENAME, String.class);
        String premacro = getProperty(ExcelComponentConstants.PRE_MACRO, String.class);
        String runmacro = getProperty(ExcelComponentConstants.RUN_MACRO, String.class);
        String postmacro = getProperty(ExcelComponentConstants.POST_MACRO, String.class);

        //Check
        File xlFile = ExcelUtils.getAbsoluteFile(excelFile);
               
        try {
            if (xlFile != null) {
                String[] macros = null;
                if (premacro != null && !premacro.isEmpty() 
                    || runmacro != null && !runmacro.isEmpty()
                    || postmacro != null && !postmacro.isEmpty()) { //This if is just for speed issues.
                    macros = excelService.getMacros(xlFile);
                }
                if (premacro != null && !premacro.isEmpty()) {
                    if (!Arrays.asList(macros).contains(premacro)) {
                        final WorkflowNodeValidationMessage validationMessage =
                            new WorkflowNodeValidationMessage(WorkflowNodeValidationMessage.Type.WARNING,
                                ExcelComponentConstants.PRE_MACRO,
                                Messages.errorWrongPreMacroRelative,
                                Messages.bind(Messages.errorWrongPreMacroAbsolute,
                                    ExcelComponentConstants.PRE_MACRO));
                        messages.add(validationMessage);
                    }
                }
                if (runmacro != null && !runmacro.isEmpty()) {
                    if (!Arrays.asList(macros).contains(runmacro)) {
                        final WorkflowNodeValidationMessage validationMessage =
                            new WorkflowNodeValidationMessage(WorkflowNodeValidationMessage.Type.WARNING,
                                ExcelComponentConstants.RUN_MACRO,
                                Messages.errorWrongRunMacroRelative,
                                Messages.bind(Messages.errorWrongRunMacroAbsolute,
                                    ExcelComponentConstants.RUN_MACRO));
                        messages.add(validationMessage);
                    }
                }
                if (postmacro != null && !postmacro.isEmpty()) {
                    if (!Arrays.asList(macros).contains(postmacro)) {
                        final WorkflowNodeValidationMessage validationMessage =
                            new WorkflowNodeValidationMessage(WorkflowNodeValidationMessage.Type.WARNING,
                                ExcelComponentConstants.POST_MACRO,
                                Messages.errorWrongPostMacroRelative,
                                Messages.bind(Messages.errorWrongPostMacroAbsolute,
                                    ExcelComponentConstants.POST_MACRO));
                        messages.add(validationMessage);
                    }
                }
            }
        } catch (ExcelException e) {
            // Just catching because ExcelException is not relevant at configuration validation time.
            final int i = 0;
        }

        return messages;
    }

}
