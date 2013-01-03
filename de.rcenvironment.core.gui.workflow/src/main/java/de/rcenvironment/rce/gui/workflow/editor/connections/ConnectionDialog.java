/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.connections;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import de.rcenvironment.rce.component.workflow.WorkflowNode;


/**
 * Dialog that helps to manage connections.
 *
 * @author Heinrich Wendel
 */
public class ConnectionDialog extends Dialog {
    /**initial string.*/
    public String initialSourceFilterText = "";
    /**initial string.*/
    public String initialTargetFilterText = "";
    /** sourceTreeViewer. */
    private EndpointTreeViewer sourceTreeViewer;

    /** targetTreeViewer. */
    private EndpointTreeViewer targetTreeViewer;

    /** connectionCanvas. */
    private ConnectionCanvas canvas;

    /** sourceTree. */
    private Tree sourceTree;

    /** targetTree. */
    private Tree targetTree;

    private ComponentViewerFilter sourceFilter;

    private ComponentViewerFilter targetFilter;

    private Text sourceFilterText;

    private Text targetFilterText;



    /**
     * Create the dialog.
     * @param parentShell The parent shell.
     */
    public ConnectionDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.RESIZE | SWT.MAX);
    }
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        container.setLayout(gridLayout);

        Group sourceGroup = new Group(container, SWT.NONE);
        sourceGroup.setText(Messages.source);
        GridData gridData1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gridData1.widthHint = 1;
        sourceGroup.setLayoutData(gridData1);
        GridLayout gridLayout1 = new GridLayout(1, false);
        gridLayout1.marginTop = 5;
        gridLayout1.marginWidth = 0;
        gridLayout1.verticalSpacing = 0;
        gridLayout1.marginHeight = 0;
        gridLayout1.horizontalSpacing = 0;

        sourceGroup.setLayout(gridLayout1);
        sourceTreeViewer = new EndpointTreeViewer(sourceGroup, SWT.NONE);
        sourceTree = sourceTreeViewer.getTree();
        sourceTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        sourceTree.setLinesVisible(true);
        sourceFilter = new ComponentViewerFilter(initialSourceFilterText);
        sourceTreeViewer.addFilter(sourceFilter);
        sourceFilterText = new Text(sourceGroup, SWT.BORDER);
        sourceFilterText.setMessage("Filter                                     ");
        sourceFilterText.setToolTipText("Filter");
        if (!initialSourceFilterText.equals("")){
            sourceFilterText.setText(initialSourceFilterText);
        }
        sourceFilterText.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent arg0) {

                sourceFilter.setExact(false);
                sourceFilter.setFilterString(((Text) arg0.getSource()).getText());          
                sourceTreeViewer.refresh();
                canvas.redraw();
                targetTreeViewer.refresh();
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                keyPressed(arg0);
            }
        });


        Group connectionGroup = new Group(container, SWT.NONE);
        connectionGroup.setText(Messages.connections);
        GridData gridData2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gridData2.widthHint = 1;
        connectionGroup.setLayoutData(gridData2);
        GridLayout gridLayout2 = new GridLayout(1, false);
        gridLayout2.marginTop = 5;
        gridLayout2.verticalSpacing = 0;
        gridLayout2.marginWidth = 0;
        gridLayout2.marginHeight = 0;
        gridLayout2.horizontalSpacing = 0;
        connectionGroup.setLayout(gridLayout2);

        canvas = new ConnectionCanvas(connectionGroup, SWT.NONE);
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        canvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        Group targetGroup = new Group(container, SWT.NONE);
        targetGroup.setText(Messages.target);
        GridData gridData3 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gridData3.widthHint = 1;
        targetGroup.setLayoutData(gridData3);
        GridLayout gridLayout3 = new GridLayout(1, false);
        gridLayout3.marginTop = 5;
        gridLayout3.verticalSpacing = 0;
        gridLayout3.marginWidth = 0;
        gridLayout3.marginHeight = 0;
        gridLayout3.horizontalSpacing = 0;
        targetGroup.setLayout(gridLayout3);
        targetTreeViewer = new EndpointTreeViewer(targetGroup, SWT.NONE);
        targetTree = targetTreeViewer.getTree();
        targetTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        targetTree.setLinesVisible(true);
        targetFilter = new ComponentViewerFilter(initialTargetFilterText);
        targetTreeViewer.addFilter(targetFilter);
        targetFilterText = new Text(targetGroup, SWT.BORDER | SWT.FILL);
        targetFilterText.setMessage("Filter                                     ");
        targetFilterText.setToolTipText("Filter");
        if (!initialTargetFilterText.equals("")){
            targetFilterText.setText(initialTargetFilterText);
        }

        targetFilterText.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent arg0) {
                targetFilter.setExact(false);
                targetFilter.setFilterString(((Text) arg0.getSource()).getText());          
                sourceTreeViewer.refresh();
                canvas.redraw();
                targetTreeViewer.refresh();
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                keyPressed(arg0);

            }
        });
        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected Point getInitialSize() {
        final int width = 512;
        final int height = 384;
        return new Point(width, height);
    }

    /**
     * Filter to remove unwanted components in connection dialog.
     * 
     * @author Sascha Zur
     */
    public class ComponentViewerFilter extends ViewerFilter{

        private String filterString = "";

        private boolean exact = false;

        public ComponentViewerFilter(String initialText){
            if (!initialText.equals("")){
                filterString = initialText;
                exact = true;
            }
        }

        @Override
        public boolean select(Viewer arg0, Object arg1, Object arg2) {
            if (arg2 instanceof WorkflowNode){
                WorkflowNode item = ((WorkflowNode) arg2);
                if (!exact){
                    if (!item.getName().toLowerCase().toString().contains(filterString.toLowerCase())) {
                        return false;
                    }
                } else {
                    if (item.getName().length() != filterString.length() 
                        || !item.getName().toLowerCase().toString().equals(filterString.toLowerCase())) {

                        return false;
                    }
                }
            }
            return true;
        }

        public String getFilterString() {
            return filterString;
        }



        public void setFilterString(String filterString) {
            this.filterString = filterString;
        }

        public boolean isExact() {
            return exact;
        }


        public void setExact(boolean exact) {
            this.exact = exact;
        }

    }



    /**
     * Getter.
     * @return Getter.
     */
    public EndpointTreeViewer getSourceTreeViewer() {
        return sourceTreeViewer;
    }

    /**
     * Getter.
     * @return Getter.
     */
    public EndpointTreeViewer getTargetTreeViewer() {
        return targetTreeViewer;
    }

    /**
     * Getter.
     * @return Getter.
     */
    public ConnectionCanvas getCanvas() {
        return canvas;
    }

    /**
     * Getter.
     * @return Getter.
     */
    public Tree getSourceTree() {
        return sourceTree;
    }

    /**
     * Getter.
     * @return Getter.
     */
    public Tree getTargetTree() {
        return targetTree;
    }



    public Text getSourceFilterText() {
        return sourceFilterText;
    }



    public void setSourceFilterText(Text sourceFilterText) {
        this.sourceFilterText = sourceFilterText;
    }


    public Text getTargetFilterText() {
        return targetFilterText;
    }

    public void setTargetFilterText(Text targetFilterText) {
        this.targetFilterText = targetFilterText;
    }

    public ComponentViewerFilter getSourceFilter() {
        return sourceFilter;
    }

    public void setSourceFilter(ComponentViewerFilter sourceFilter) {
        this.sourceFilter = sourceFilter;
    }


    public ComponentViewerFilter getTargetFilter() {
        return targetFilter;
    }


    public void setTargetFilter(ComponentViewerFilter targetFilter) {
        this.targetFilter = targetFilter;
    }


    public String getInitialSourceFilterText() {
        return initialSourceFilterText;
    }


    public void setInitialSourceFilterText(String initialSourceFilterText) {
        this.initialSourceFilterText = initialSourceFilterText;
    }


    public String getInitialTargetFilterText() {
        return initialTargetFilterText;
    }

    public void setInitialTargetFilterText(String initialTargetFilterText) {
        this.initialTargetFilterText = initialTargetFilterText;
    }
}
