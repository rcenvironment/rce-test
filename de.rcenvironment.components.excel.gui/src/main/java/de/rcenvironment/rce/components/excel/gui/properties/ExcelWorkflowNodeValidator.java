/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.gui.properties;


import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.rcenvironment.rce.components.excel.commons.ExcelAddress;
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
 * <p>
 * To be incorporated in the validation process, this validator is registered as an extension to the
 * extension point "de.rcenvironment.rce.gui.workflow.nodeValidators" in the plugin.xml.
 * </p>
 *
 * @author Markus Kunde
 */
public class ExcelWorkflowNodeValidator extends AbstractWorkflowNodeValidator {
    
    @Override
    protected Collection<WorkflowNodeValidationMessage> validate() {
        ExcelService excelService = new SimpleExcelService();
        
        final List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();
        
        //Get all relevant validation items
        String excelFile = getProperty(ExcelComponentConstants.XL_FILENAME, String.class);
        boolean driver = getProperty(ExcelComponentConstants.DRIVER, Boolean.class);   

        //Check
        File xlFile = ExcelUtils.getAbsoluteFile(excelFile);
        
        if (xlFile == null || !excelService.isValidExcelFile(xlFile)) {
            final WorkflowNodeValidationMessage validationMessage = 
                new WorkflowNodeValidationMessage(WorkflowNodeValidationMessage.Type.ERROR, 
                    ExcelComponentConstants.XL_FILENAME,
                    Messages.errorNoExcelFileRelative, 
                    Messages.bind(Messages.errorNoExcelFileAbsolute,
                        ExcelComponentConstants.XL_FILENAME));
            
            messages.add(validationMessage);
        }

        if (driver && getOutputs().isEmpty()) {
            final WorkflowNodeValidationMessage validationMessage =
                new WorkflowNodeValidationMessage(WorkflowNodeValidationMessage.Type.WARNING,
                    ExcelComponentConstants.DRIVER,
                    Messages.errorNoOutputAsDriverRelative,
                    Messages.bind(Messages.errorNoOutputAsDriverAbsolute,
                        ExcelComponentConstants.DRIVER));
            messages.add(validationMessage);
        }
           
        for (final String inputName: getInputs().keySet()) {
            messages.addAll(testChannelMetaData(xlFile, getInputMetaData(inputName)));            
        }
        
        for (final String outputName: getOutputs().keySet()) {
            messages.addAll(testChannelMetaData(xlFile, getOutputMetaData(outputName)));
        }
        
        return messages;
    }
    
    
    private List<WorkflowNodeValidationMessage> testChannelMetaData(final File xlFile, final Map<String, Serializable> metaData) {
        final List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();
        
        if (!(metaData.get(ExcelComponentConstants.METADATA_EXPANDING) instanceof Boolean)) {
            final WorkflowNodeValidationMessage validationMessage =
                new WorkflowNodeValidationMessage(WorkflowNodeValidationMessage.Type.ERROR,
                    ExcelComponentConstants.METADATA_EXPANDING,
                    Messages.errorNoMetaDataExpandingRelative,
                    Messages.bind(Messages.errorNoMetaDataExpandingAbsolute,
                        ExcelComponentConstants.METADATA_EXPANDING));
            messages.add(validationMessage);
        }
        
        if (!(metaData.get(ExcelComponentConstants.METADATA_PRUNING) instanceof Boolean)) {
            final WorkflowNodeValidationMessage validationMessage =
                new WorkflowNodeValidationMessage(WorkflowNodeValidationMessage.Type.ERROR,
                    ExcelComponentConstants.METADATA_PRUNING,
                    Messages.errorNoMetaDataPruningRelative,
                    Messages.bind(Messages.errorNoMetaDataPruningAbsolute,
                        ExcelComponentConstants.METADATA_PRUNING));
            messages.add(validationMessage);
        }
        
        Object objAddress = metaData.get(ExcelComponentConstants.METADATA_ADDRESS); 
        if (objAddress instanceof String) {
            final WorkflowNodeValidationMessage validationMessage =
                new WorkflowNodeValidationMessage(WorkflowNodeValidationMessage.Type.ERROR,
                    ExcelComponentConstants.METADATA_ADDRESS,
                    Messages.errorNoMetaDataAddressRelative,
                    Messages.bind(Messages.errorNoMetaDataAddressAbsolute,
                        ExcelComponentConstants.METADATA_ADDRESS));
            try {
                new ExcelAddress(xlFile, (String) objAddress);
            } catch (ExcelException e) {
                messages.add(validationMessage);
            }          
        }
        
        
        return messages;
    }

}
