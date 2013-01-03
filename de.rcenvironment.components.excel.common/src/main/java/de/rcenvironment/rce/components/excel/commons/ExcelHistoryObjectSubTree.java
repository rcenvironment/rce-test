/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.commons;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNode;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNodeType;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.HistoryObjectSubtreeBuilder;


/**
 * Excel history object subtree.
 *
 * @author Markus Kunde
 */
public class ExcelHistoryObjectSubTree implements HistoryObjectSubtreeBuilder {

    private static final String ENDING = "\"";
    
    private static final String[] SUPPORTED_CLASS_NAMES = { ExcelHistoryObject.class.getCanonicalName() };
    
    @Override
    public String[] getSupportedObjectClassNames() {
        return SUPPORTED_CLASS_NAMES;
    }

    @Override
    public Serializable deserializeHistoryObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        return (Serializable) ois.readObject();
    }

    @Override
    public void buildInitialHistoryObjectSubtree(Serializable historyObject, DMBrowserNode parent) {
        if (historyObject instanceof ExcelHistoryObject) {
            buildInitial((ExcelHistoryObject) historyObject, parent);
        } else {
            throw new IllegalArgumentException(historyObject.getClass().getCanonicalName());
        }
    }
    
    private void buildInitial(ExcelHistoryObject channelValues, DMBrowserNode parent) {
        List<ChannelValue> hPoints = channelValues.getHistoryPoints();
        
        if (hPoints != null && !hPoints.isEmpty()) {
            DMBrowserNode.addNewLeafNode("File: \"" + hPoints.get(0).getFile().getAbsolutePath() + ENDING,
                DMBrowserNodeType.InformationText, parent);
            
            for (ChannelValue cv: hPoints) {
                String direction;
                if (cv.isInputValue()) {
                    direction = "Incoming-";
                } else {
                    direction = "Outgoing-";
                }
                DMBrowserNode channel = DMBrowserNode.addNewChildNode(direction + "Channel: \"" + cv.getChannelName() + "("
                    + cv.getExcelAddress().getWorkSheetName()
                    + ExcelComponentConstants.DIVIDER_TABLECELLADDRESS
                    + cv.getExcelAddress().getFirstCell()
                    + ExcelComponentConstants.DIVIDER_CELLADDRESS
                    + cv.getExcelAddress().getLastCell() + ")" + ENDING,
                    DMBrowserNodeType.InformationText, parent);
                DMBrowserNode.addNewChildNode("Value: \"" + cv.getValues().getValueAsString()
                    + ENDING, DMBrowserNodeType.InformationText, channel);
            }
            
            
            
        }
        
    }

}
