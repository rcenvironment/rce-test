/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.view.console;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;

import de.rcenvironment.rce.component.ConsoleRow;

/**
 * Class responsible for copying log entries to the clipboard.
 *
 * @author Doreen Seider
 */
public class CopyToClipboardListener implements SelectionListener {

    private TableViewer tableViewer;
    
    public CopyToClipboardListener(TableViewer aTableViewer) {
        tableViewer = aTableViewer;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        
        Clipboard cb = new Clipboard(Display.getDefault());
        ISelection selection = tableViewer.getSelection();
        List<ConsoleRow> consoleRows = new ArrayList<ConsoleRow>();
        if (selection != null && selection instanceof IStructuredSelection) {
            IStructuredSelection sel = (IStructuredSelection) selection;
            for (@SuppressWarnings("unchecked")
            Iterator<ConsoleRow> iterator = sel.iterator(); iterator.hasNext();) {
                ConsoleRow row = iterator.next();
                consoleRows.add(row);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (ConsoleRow row : consoleRows) {
            sb.append(row.toString() + System.getProperty("line.separator")); //$NON-NLS-1$
        }
        TextTransfer textTransfer = TextTransfer.getInstance();
        cb.setContents(new Object[] { sb.toString() }, new Transfer[] { textTransfer });
    }

}
