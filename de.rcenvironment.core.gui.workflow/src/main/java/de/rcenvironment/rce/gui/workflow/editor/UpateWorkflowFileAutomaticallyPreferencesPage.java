/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.editor;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.rcenvironment.rce.gui.workflow.Activator;


/**
 * Preferences page for pref: update workflow file automatically.
 *
 * @author Doreen Seider
 */
public class UpateWorkflowFileAutomaticallyPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public UpateWorkflowFileAutomaticallyPreferencesPage() {
       super(GRID);
    }
    
    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(WorkflowEditor.PREFS_KEY_UPDATEAUTOMATICALLY,
            Messages.updateIncompatibleVersionAutomatically, getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getInstance().getPreferenceStore());
        
    }

}
