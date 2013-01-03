/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel.gui.properties;

import java.util.ArrayList;
import java.util.List;

import de.rcenvironment.commons.channel.ChannelDataTypes;
import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.gui.workflow.editor.properties.AdvancedDynamicEndpointPropertySection;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionFactory;
import de.rcenvironment.rce.gui.workflow.editor.properties.TypeSelectionOption;


/**
 * "Properties" view tab for configuring cells as additional endpoints (i.e. inputs and outputs).
 * 
 * @author Patrick Schaefer
 * @author Markus Kunde
 */
public class VariablesSection extends AdvancedDynamicEndpointPropertySection {

    public VariablesSection() {
        super();
        
        TypeSelectionFactory defaultTypeSelectionFactory = new TypeSelectionFactory() {

            @Override
            public List<TypeSelectionOption> getTypeSelectionOptions() {
                List<TypeSelectionOption> result = new ArrayList<TypeSelectionOption>();
                /*result.add(new TypeSelectionOption(EDataType.ShortText.toString(), ShortText.class.getName()));
                result.add(new TypeSelectionOption(EDataType.Number.toString(), Number.class.getName()));
                result.add(new TypeSelectionOption(EDataType.Date.toString(), Date.class.getName()));
                result.add(new TypeSelectionOption(EDataType.Empty.toString(), Empty.class.getName()));
                result.add(new TypeSelectionOption(EDataType.Logic.toString(), Logic.class.getName()));
                result.add(new TypeSelectionOption(EDataType.Table.toString(), ITable.class.getName()));*/
                
                result.add(new TypeSelectionOption(ChannelDataTypes.BOOLEAN.name(), Boolean.class.getName()));
                result.add(new TypeSelectionOption(ChannelDataTypes.DOUBLE.name(), Double.class.getName()));
                result.add(new TypeSelectionOption(ChannelDataTypes.LONG.name(), Long.class.getName()));
                result.add(new TypeSelectionOption(ChannelDataTypes.STRING.name(), String.class.getName()));
                result.add(new TypeSelectionOption(ChannelDataTypes.VARIANTARRAY.name(), VariantArray.class.getName()));
                return result;
            }
        };
        inputPane = new VariablesSelectionPane("Input",
                ComponentDescription.EndpointNature.Input, defaultTypeSelectionFactory, this);
        outputPane = new VariablesSelectionPane("Output",
                ComponentDescription.EndpointNature.Output, defaultTypeSelectionFactory, this);
    }
}
