/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.gui.simplewrapper.properties;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for {@link ColumnMapping}s.
 * 
 * @author Christian Weiss
 */
public class InputMappingLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(final Object element, final int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(final Object element, final int columnIndex) {
        if (element instanceof String[]) {
            return ((String[]) element)[columnIndex];
        }
        return "";
    }

}
