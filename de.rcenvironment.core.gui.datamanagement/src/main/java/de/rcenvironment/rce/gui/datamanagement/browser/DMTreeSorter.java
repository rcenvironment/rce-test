/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.datamanagement.browser;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import de.rcenvironment.commons.ComparatorUtils;
import de.rcenvironment.rce.component.datamanagement.history.HistoryMetaDataKeys;
import de.rcenvironment.rce.datamanagement.commons.MetaData;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNode;

/**
 * Class for sorting the DMItems. 
 * 
 * @author Sascha Zur
 */
public class DMTreeSorter extends ViewerSorter{

    /** Constant. */
    public static final int SORT_BY_TIMESTAMP = 0;
    /** Constant. */
    public static final int SORT_BY_NAME_ASC = 1;
    /** Constant. */
    public static final int SORT_BY_NAME_DESC = 2;
    /** Constant. */
    public static final int SORT_BY_TIMESTAMP_DESC = 3;
    
    private static final MetaData METADATA_HISTORY_TIMESTAMP = new MetaData(
        HistoryMetaDataKeys.HISTORY_TIMESTAMP, true, true);

    private int sortType;

    public DMTreeSorter(int sortType) {
        this.sortType = sortType;
    }
    @Override
    public int category(Object element) {
        return 0;
    }
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int result = 0;
        DMBrowserNode o1 = (DMBrowserNode) e1;
        DMBrowserNode o2 = (DMBrowserNode) e2;
        if (sortType == SORT_BY_NAME_ASC){
            result = o1.getTitle().compareToIgnoreCase(o2.getTitle());
        } else if (sortType == SORT_BY_NAME_DESC){
            result = -o1.getTitle().compareToIgnoreCase(o2.getTitle());
        } else {
            if (o1.getMetaData() != null && o2.getMetaData() != null) {
                String val1 = o1.getMetaData().getValue(METADATA_HISTORY_TIMESTAMP);
                String val2 = o2.getMetaData().getValue(METADATA_HISTORY_TIMESTAMP);
                long time1 = nullSafeLongValue(val1);
                long time2 = nullSafeLongValue(val2);
                result = ComparatorUtils.compareLong(time1, time2);
                if (sortType == SORT_BY_TIMESTAMP_DESC){
                    result *= (0 - 1);
                }
            } else {
                result = 0;
            }
        }
        return result;
    }

    private long nullSafeLongValue(String val1) {
        if (val1 == null) {
            return 0L;
        }
        return Long.parseLong(val1);
    }
}
