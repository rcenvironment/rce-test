/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.gui.workflow.executor.properties;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.core.component.executor.ScriptUsage;
import de.rcenvironment.core.component.executor.SshExecutorConstants;
import de.rcenvironment.rce.gui.workflow.editor.properties.AbstractWorkflowNodeCommand;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;

/**
 * Abstract component for the ScriptSection.
 * @author Sascha Zur 
 */
public abstract class AbstractScriptSection extends ValidatingWorkflowNodePropertySection{

    /***/
    public static final int LOCAL_FILE = 1; 
    /***/
    public static final int REMOTE_FILE = 2; 
    /***/
    public static final int NEW_SCRIPT_FILE = 4; 
    /***/
    public static final int NO_SCRIPT_FILENAME = 8; 
    /***/
    public static final int ALL = LOCAL_FILE | REMOTE_FILE | NEW_SCRIPT_FILE;



    private static final int MINIMUM_HEIGHT_OF_JOB_SCRIPTING_TEXT = 500;

    private static Log log = LogFactory.getLog(ScriptSection.class);

    private Button useLocalScriptRadioButton;

    private Button useRemoteScriptRadioButton;

    private Text localScriptPathText;

    private Button selectLocalScriptButton;

    private Text remoteScriptPathText;

    private Button useNewScriptRadioButton;

    private Composite newScriptArea;

    private Text remoteUploadPathText;

    private Text scriptingText;

    private int style;

    public AbstractScriptSection(int style){
        this.style = style;
    }

    @Override
    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {

        TabbedPropertySheetWidgetFactory factory = aTabbedPropertySheetPage.getWidgetFactory();

        Section jobSection = factory.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        jobSection.setText(Messages.configureScript);

        Composite jobParent = factory.createFlatFormComposite(jobSection);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        jobParent.setLayout(layout);

        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        if ((style & LOCAL_FILE) > 0){
            useLocalScriptRadioButton = factory.createButton(jobParent, "Use local script", SWT.RADIO);
            useLocalScriptRadioButton.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_USAGEOFSCRIPT);
            useLocalScriptRadioButton.setData(ENUM_TYPE_KEY, ScriptUsage.class);
            useLocalScriptRadioButton.setData(ENUM_VALUE_KEY, ScriptUsage.LOCAL);


            Composite localPathArea = factory.createFlatFormComposite(jobParent);
            localPathArea.setLayoutData(gridData);

            layout = new GridLayout();
            layout.numColumns = 3;
            localPathArea.setLayout(layout);

            factory.createLabel(localPathArea, "Local script:");

            gridData = new GridData();
            gridData.grabExcessHorizontalSpace = true;
            gridData.horizontalAlignment = GridData.FILL;

            localScriptPathText = factory.createText(localPathArea, "");
            localScriptPathText.setLayoutData(gridData);
            localScriptPathText.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_LOCALSCRIPTNAME);

            selectLocalScriptButton = factory.createButton(localPathArea, "...", SWT.NONE);
        }

        if ((style & REMOTE_FILE) > 0){
            useRemoteScriptRadioButton = factory.createButton(jobParent, "Use script on host", SWT.RADIO);
            useRemoteScriptRadioButton.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_USAGEOFSCRIPT);
            useRemoteScriptRadioButton.setData(ENUM_TYPE_KEY, ScriptUsage.class);
            useRemoteScriptRadioButton.setData(ENUM_VALUE_KEY, ScriptUsage.REMOTE);

            gridData = new GridData();
            gridData.grabExcessHorizontalSpace = true;
            gridData.horizontalAlignment = GridData.FILL;

            Composite remotePathArea = factory.createFlatFormComposite(jobParent);
            remotePathArea.setLayoutData(gridData);

            layout = new GridLayout();
            layout.numColumns = 2;
            remotePathArea.setLayout(layout);

            factory.createLabel(remotePathArea, "Path on cluster:");

            gridData = new GridData();
            gridData.grabExcessHorizontalSpace = true;
            gridData.horizontalAlignment = GridData.FILL;

            remoteScriptPathText = factory.createText(remotePathArea, "");
            remoteScriptPathText.setLayoutData(gridData);
            remoteScriptPathText.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_REMOTEPATHOFSCRIPT);
        }


        if ((style & NEW_SCRIPT_FILE) > 0){

            useNewScriptRadioButton = factory.createButton(jobParent, "Write script here", SWT.RADIO);
            useNewScriptRadioButton.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_USAGEOFSCRIPT);
            useNewScriptRadioButton.setData(ENUM_TYPE_KEY, ScriptUsage.class);
            useNewScriptRadioButton.setData(ENUM_VALUE_KEY, ScriptUsage.NEW);

            Button openInEditorButton = factory.createButton(jobParent, "Open in Editor", SWT.PUSH);
            openInEditorButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    new EditScriptRunnable().run();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent arg0) {
                    widgetSelected(arg0);

                }
            });
            gridData = new GridData();
            gridData.grabExcessHorizontalSpace = true;
            gridData.horizontalAlignment = GridData.FILL;

            newScriptArea = factory.createFlatFormComposite(jobParent);
            newScriptArea.setLayoutData(gridData);

            layout = new GridLayout();
            layout.numColumns = 2;
            newScriptArea.setLayout(layout);

            gridData = new GridData();
            gridData.grabExcessHorizontalSpace = true;
            gridData.horizontalAlignment = GridData.FILL;

            if ((style & NO_SCRIPT_FILENAME)  == 0){
                factory.createLabel(newScriptArea, "Script file name:");

                remoteUploadPathText = factory.createText(newScriptArea, "");
                remoteUploadPathText.setLayoutData(gridData);
                remoteUploadPathText.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_NAMEOFNEWJOBSCRIPT);
            }

            gridData = new GridData();
            gridData.horizontalSpan = 2;
            gridData.grabExcessHorizontalSpace = true;
            gridData.horizontalAlignment = GridData.FILL;
            gridData.grabExcessVerticalSpace = true;
            gridData.verticalAlignment = GridData.FILL;

            scriptingText = factory.createText(newScriptArea, "", SWT.MULTI);
            scriptingText.setLayoutData(gridData);
            scriptingText.setData(CONTROL_PROPERTY_KEY, SshExecutorConstants.CONFIG_KEY_SCRIPT);
            ((GridData) newScriptArea.getLayoutData()).heightHint = MINIMUM_HEIGHT_OF_JOB_SCRIPTING_TEXT;

            addResizingListenerForJobScriptingText(parent.getParent());


        }
        jobSection.setClient(jobParent);
    }

    @Override
    protected Updater createUpdater() {
        return new JobUpdater();
    }

    @Override
    protected Controller createController() {
        return new JobController();
    }

    private void addResizingListenerForJobScriptingText(final Composite parent) {

        parent.addListener(SWT.Resize,  new Listener() {

            public void handleEvent(Event e) {
                setSizeOfJobScriptingText(parent);
            }
        });
    }

    private void setSizeOfJobScriptingText(Composite parent) {
        final int topMargin = 125;
        if (parent.getSize().y < MINIMUM_HEIGHT_OF_JOB_SCRIPTING_TEXT){
            ((GridData) newScriptArea.getLayoutData()).heightHint = MINIMUM_HEIGHT_OF_JOB_SCRIPTING_TEXT;
        } else {
            ((GridData) newScriptArea.getLayoutData()).heightHint = parent.getSize().y - topMargin;
            newScriptArea.update();
        }
    }

    private void setLocalScriptAreaEnabled(boolean enabled) {
        localScriptPathText.setEnabled(enabled);
        selectLocalScriptButton.setEnabled(enabled);
    }

    private void setRemoteScriptAreaEnabled(boolean enabled) {
        remoteScriptPathText.setEnabled(enabled);
    }

    private void setNewScriptAreaEnabled(boolean enabled) {
        if ((style & NO_SCRIPT_FILENAME) == 0){
            remoteUploadPathText.setEnabled(enabled);
        }
        scriptingText.setEnabled(enabled);
    }
    /**
     * Implementation of {@link AbstractEditScriptRunnable}.
     * 
     * @author Doreen Seider
     */
    private class EditScriptRunnable extends AbstractEditScriptRunnable {

        protected void setScript(String script) {
            setProperty(SshExecutorConstants.CONFIG_KEY_SCRIPT, script);
        }

        protected String getScript() {
            return getProperty(SshExecutorConstants.CONFIG_KEY_SCRIPT, String.class);
        }
    }
    /**
     * Custom {@link DefaultUpdater} extension to synchronize managed GUI elements that need special handling.
     *
     * @author Doreen Seider
     */
    private final class JobUpdater extends DefaultUpdater {

        @Override
        public void updateControl(final Control control, final String propertyName, final Serializable newValue,
            final Serializable oldValue) {
            super.updateControl(control, propertyName, newValue, oldValue);

            if (control == useLocalScriptRadioButton || control == useRemoteScriptRadioButton || control == useNewScriptRadioButton) {
                if ((style & LOCAL_FILE) > 0){
                    setLocalScriptAreaEnabled(useLocalScriptRadioButton.getSelection());
                }
                if ((style & REMOTE_FILE) > 0){
                    setRemoteScriptAreaEnabled(useRemoteScriptRadioButton.getSelection());
                }
                if ((style & NEW_SCRIPT_FILE) > 0){
                    setNewScriptAreaEnabled(useNewScriptRadioButton.getSelection());
                }
            }
        }

    }

    /**
     * Custom {@link DefaultController} implementation to handle the activation of the GUI
     * controls.
     * 
     * @author Doreen Seider
     */
    private final class JobController extends DefaultController {

        @Override
        protected void widgetSelected(final SelectionEvent event, final Control source) {
            super.widgetSelected(event, source);

            if (source == useLocalScriptRadioButton || source == useRemoteScriptRadioButton || source == useNewScriptRadioButton) {
                if ((style & LOCAL_FILE) > 0){
                    setLocalScriptAreaEnabled(useLocalScriptRadioButton.getSelection());
                }
                if ((style & REMOTE_FILE) > 0){
                    setRemoteScriptAreaEnabled(useRemoteScriptRadioButton.getSelection());
                }
                if ((style & NEW_SCRIPT_FILE) > 0){
                    setNewScriptAreaEnabled(useNewScriptRadioButton.getSelection());
                }
            } else if (source == selectLocalScriptButton) {
                FileDialog selectFilesDialog = new FileDialog(source.getShell(), SWT.OPEN);
                selectFilesDialog.setText("Select script");
                String path = selectFilesDialog.open();
                if (path != null) {
                    try {
                        File file = new File(path);
                        new LocalScriptCommand(getProperty(SshExecutorConstants.CONFIG_KEY_LOCALSCRIPT,  String.class),
                            FileUtils.readFileToString(file)).execute();
                        localScriptPathText.setText(file.getName());
                    } catch (IOException e) {
                        log.error("Adding local script file failed", e);
                    }
                }
            }
        }

    }

    /**
     * Abstract workflow command for adding local script.
     * @author Doreen Seider
     */
    private final class LocalScriptCommand extends AbstractWorkflowNodeCommand {

        protected final String oldScript;

        protected final String newScript;

        private LocalScriptCommand(String oldScript, String newScript) {
            this.oldScript = oldScript;
            this.newScript = newScript;
        }

        @Override
        protected void execute2() {
            setProperty(SshExecutorConstants.CONFIG_KEY_LOCALSCRIPT, newScript);
        }

        @Override
        protected void undo2() {
            setProperty(SshExecutorConstants.CONFIG_KEY_LOCALSCRIPT, oldScript);
        }

    }
}
