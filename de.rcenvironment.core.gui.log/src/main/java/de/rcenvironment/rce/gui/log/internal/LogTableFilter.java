/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.log.internal;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.osgi.service.log.LogService;

import de.rcenvironment.rce.log.SerializableLogEntry;

/**
 * Listener for GUI-elements such as check box, drop down box, and text field to realize changes.
 * In case of changes it pushes to refresh displaying the data and organizes filtering table data. 
 * 
 * @author Enrico Tappert
 */
public class LogTableFilter extends ViewerFilter implements SelectionListener, KeyListener {

    private boolean myDebugSetup;

    private boolean myErrorSetup;

    private boolean myInfoSetup;

    private boolean myWarnSetup;

    private LogView myLoggingView;

    private String mySearchTerm;

    private TableViewer myTableViewer;

    public LogTableFilter(LogView loggingView, TableViewer tableViewer) {
        myLoggingView = loggingView;
        myTableViewer = tableViewer;

        updateTableView();
        
        myTableViewer.getTable().setFocus();
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        boolean returnValue = false;

        if (element instanceof SerializableLogEntry) {
            SerializableLogEntry logEntry = (SerializableLogEntry) element;

            if (isLevelSelected(logEntry.getLevel())) {
                returnValue = isSelectedBySearchTerm(logEntry.getMessage());
            }
        }

        return returnValue;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        updateTableView();
        myTableViewer.getTable().setFocus();
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
    // do nothing
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        updateTableView();
    }

    /**
     * 
     * Collect view settings, push to refresh displaying.
     *
     */
    private void updateTableView() {
        myDebugSetup = myLoggingView.getDebugSelection();
        myErrorSetup = myLoggingView.getErrorSelection();
        myInfoSetup = myLoggingView.getInfoSelection();
        myWarnSetup = myLoggingView.getWarnSelection();

        setSearchTerm(myLoggingView.getSearchText());

        LogModel.getInstance().setCurrentPlatform(myLoggingView.getPlatform());
        
        myTableViewer.refresh();
    }

    private boolean isLevelSelected(int level) {
        boolean returnValue = false;

        if ((LogService.LOG_DEBUG == level) && myDebugSetup) {
            returnValue = true;
        } else if ((LogService.LOG_ERROR == level) && myErrorSetup) {
            returnValue = true;
        } else if ((LogService.LOG_INFO == level) && myInfoSetup) {
            returnValue = true;
        } else if ((LogService.LOG_WARNING == level) && myWarnSetup) {
            returnValue = true;
        }

        return returnValue;
    }

    private boolean isSelectedBySearchTerm(String message) {
        boolean returnValue = false;

        if (null == mySearchTerm || 0 == mySearchTerm.length()) {
            // "nothing" matches them all

            returnValue = true;
        } else if (messageMatchesSearchTerm(message)) {
            returnValue = true;
        }

        return returnValue;
    }

    private void setSearchTerm(String searchTerm) {
        // regular expression enables matching within a text
        // search is case insensitive
        mySearchTerm = ".*" + searchTerm.toLowerCase() + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private boolean messageMatchesSearchTerm(String message) {
        // search is case insensitive - see also method 'setSearchTerm'
        return message.toLowerCase().matches(mySearchTerm);
    }
}
