/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.components.pioneer.gui.properties;

import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_EXECUTE_INITIAL;
import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_ITERATIONS;
import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_MESSAGE;
import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_OPERATION_MODE;
import static de.rcenvironment.components.pioneer.common.PioneerConstants.KEY_WAIT;

import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.components.pioneer.common.PioneerOperationMode;
import de.rcenvironment.rce.component.workflow.ChannelEvent;
import de.rcenvironment.rce.gui.workflow.editor.properties.AbstractWorkflowNodeCommand;
import de.rcenvironment.rce.gui.workflow.editor.properties.ValidatingWorkflowNodePropertySection;


/**
 * Sample implementation for contributing to the tabbed properties view.
 * 
 * <p>
 * This section inherits from {@link ValidatingWorkflowNodePropertySection}, thus highlights managed
 * configuration fields with values that contain errors (as reported by registered
 * {@link de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidator}s.
 * </p>
 * 
 * @author Heinrich Wendel
 * @author Christian Weiss
 */
public class PioneerSection extends ValidatingWorkflowNodePropertySection {

    private Text text;

    private Label textLabel;

    private Composite modeGroup;

    private Button activeModeButton;

    private Button passiveModeButton;

    private Composite activeSection;

    private Composite passiveSection;

    private Spinner iterationsSpinner;

    private Label overviewLabel;

    private Button resetButton;
    
    @Override
    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        final TabbedPropertySheetWidgetFactory toolkit = aTabbedPropertySheetPage.getWidgetFactory();
        
        final Composite content = new LayoutComposite(parent);
        content.setLayout(new GridLayout(2, true));
        
        final Composite messageSection = toolkit.createFlatFormComposite(content);
        initMessageSection(toolkit, messageSection);

        final Composite modeSection = toolkit.createFlatFormComposite(content);
        initModeSection(toolkit, modeSection);

        activeSection = toolkit.createFlatFormComposite(content);
        initActiveSection(toolkit, activeSection);

        passiveSection = toolkit.createFlatFormComposite(content);
        initPassiveSection(toolkit, passiveSection);

        final Composite overviewSection = toolkit.createFlatFormComposite(content);
        initOverviewSection(toolkit, overviewSection);
    }

    private void initModeSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite container) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        container.setLayoutData(layoutData);
        container.setLayout(new FillLayout());
        final Section section = toolkit.createSection(container, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Operation Mode"); //Messages.modeSectionTitle);
        final Composite client = toolkit.createComposite(section);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        client.setLayoutData(layoutData);
        // --
        client.setLayout(new GridLayout(1, false));
        // --
        /*
         * A group of radio buttons to choose between the available operation modes, which are
         * represented in an Enum. This code showes the Enum-facilities.
         * 
         * Note, that the 'group' Composite is linked to the enum TYPE, while the Button controls
         * are linked to the enum VALUE they represent.
         */
        modeGroup = toolkit.createComposite(client);
        modeGroup.setLayout(new RowLayout(SWT.VERTICAL));
        activeModeButton = toolkit.createButton(modeGroup, PioneerOperationMode.ACTIVE.getLabel(), SWT.RADIO);
        activeModeButton.setData(CONTROL_PROPERTY_KEY, KEY_OPERATION_MODE);
        activeModeButton.setData(ENUM_TYPE_KEY, PioneerOperationMode.class);
        activeModeButton.setData(ENUM_VALUE_KEY, PioneerOperationMode.ACTIVE);
        passiveModeButton = toolkit.createButton(modeGroup, PioneerOperationMode.PASSIVE.getLabel(), SWT.RADIO);
        passiveModeButton.setData(CONTROL_PROPERTY_KEY, KEY_OPERATION_MODE);
        passiveModeButton.setData(ENUM_TYPE_KEY, PioneerOperationMode.class);
        passiveModeButton.setData(ENUM_VALUE_KEY, PioneerOperationMode.PASSIVE);
        modeGroup.setData(CONTROL_PROPERTY_KEY, KEY_OPERATION_MODE);
        // --
        section.setClient(client);
    }

    private void initMessageSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite container) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        container.setLayoutData(layoutData);
        container.setLayout(new FillLayout());
        final Section section = toolkit.createSection(container, Section.TITLE_BAR | Section.EXPANDED);
        section.setText(Messages.messageSectionTitle);
        final Composite client = toolkit.createComposite(section);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        client.setLayoutData(layoutData);
        // --
        client.setLayout(new GridLayout(2, false));
        // --
        // label for messages Text input
        toolkit.createLabel(client, Messages.messageLabel);
        /*
         * A managed text filed. The setData(CONTROL_PROPERTY_KEY, ...) puts the text field under
         * management. The initial refresh and all updates of the backing value will be handled by
         * the Controller, Synchronizer, Updater framework.
         */
        text = toolkit.createText(client, "");
        text.setData(CONTROL_PROPERTY_KEY, KEY_MESSAGE); // put under framework management
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
        text.setLayoutData(layoutData);
        /*
         * A sample label which needs special handling of the synchronization with the underlying model.
         */
        textLabel = toolkit.createLabel(client, "");
        textLabel.setData(CONTROL_PROPERTY_KEY, KEY_MESSAGE);
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
        layoutData.horizontalSpan = 2;
        textLabel.setLayoutData(layoutData);
        // --
        section.setClient(client);
    }

    private void initActiveSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite container) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        container.setLayoutData(layoutData);
        container.setLayout(new FillLayout());
        final Section section = toolkit.createSection(container, Section.TITLE_BAR | Section.EXPANDED);
        section.setText(Messages.activeSectionTitle);
        final Composite client = toolkit.createComposite(section);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        client.setLayoutData(layoutData);
        // --
        client.setLayout(new GridLayout(2, false));
        // --
        // label for iterations Spinner input
        toolkit.createLabel(client, Messages.iterationsLabel);
        /*
         * A sample Spinner control to set the number of iterations. The
         * setData(CONTROL_PROPERTY_KEY, ...) puts the Spinner control under management. The other
         * setter calls configure the Spinner control.
         */
        iterationsSpinner = new Spinner(client, SWT.NONE);
        iterationsSpinner.setData(CONTROL_PROPERTY_KEY, KEY_ITERATIONS); // put under framework management
        iterationsSpinner.setMinimum(0);
        iterationsSpinner.setMaximum(Integer.MAX_VALUE);
        iterationsSpinner.setDigits(0);
        iterationsSpinner.setIncrement(1);
        final int pageIncrement = 100;
        iterationsSpinner.setPageIncrement(pageIncrement);
        toolkit.adapt(iterationsSpinner);
        // --
        section.setClient(client);
    }

    private void initPassiveSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite container) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        container.setLayoutData(layoutData);
        container.setLayout(new FillLayout());
        final Section section = toolkit.createSection(container, Section.TITLE_BAR | Section.EXPANDED);
        section.setText(Messages.passiveSectionTitle);
        final Composite client = toolkit.createComposite(section);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        client.setLayoutData(layoutData);
        // --
        client.setLayout(new GridLayout(2, false));
        // --
        /*
         * Two sample Button controls to demonstrate the management capabilities for CHECK and
         * TOGGLE buttons. The two buttons are registered with the same property thus will display
         * the same value. The control the execute initial property.
         */
        final Button waitInitialButtonCheck = toolkit.createButton(client, Messages.executeInitialButtonLabel, SWT.CHECK);
        waitInitialButtonCheck.setData(CONTROL_PROPERTY_KEY, KEY_EXECUTE_INITIAL);
        final Button waitInitialButtonToggle = toolkit.createButton(client, Messages.executeInitialButtonLabel, SWT.TOGGLE);
        waitInitialButtonToggle.setData(CONTROL_PROPERTY_KEY, KEY_EXECUTE_INITIAL);
        // --
        section.setClient(client);
    }

    private void initOverviewSection(final TabbedPropertySheetWidgetFactory toolkit, final Composite container) {
        GridData layoutData;
        layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        container.setLayoutData(layoutData);
        container.setLayout(new FillLayout());
        final Section section = toolkit.createSection(container, Section.TITLE_BAR | Section.EXPANDED);
        section.setText(Messages.overviewSectionTitle);
        final Composite client = toolkit.createComposite(section);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        client.setLayoutData(layoutData);
        // --
        client.setLayout(new GridLayout(1, false));
        // --
        /*
         * A Label control to be synchronized with custom logic via a Synchronizer.
         */
        overviewLabel = toolkit.createLabel(client, "test");
        layoutData = new GridData(GridData.GRAB_VERTICAL | GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 2;
        overviewLabel.setLayoutData(layoutData);
        /*
         * A Button control to realize custom logic via a Controller.
         */
        resetButton = toolkit.createButton(client, Messages.resetButtonLabel, SWT.PUSH);
        layoutData = new GridData(GridData.GRAB_VERTICAL);
        layoutData.horizontalSpan = 2;
        resetButton.setLayoutData(layoutData);
        // --
        section.setClient(client);
    }

    @Override
    public void refreshBeforeValidation() {
        displayOverview();
        adjustGuiToOperationMode();
    }

    private void displayOverview() {
        final Serializable message = getProperty(KEY_MESSAGE);
        final Serializable iterations = getProperty(KEY_ITERATIONS);
        final PioneerOperationMode operationMode = getProperty(KEY_OPERATION_MODE, PioneerOperationMode.class);
        final boolean passivePossible = hasInputs();
        final boolean isActive = operationMode == PioneerOperationMode.ACTIVE && passivePossible;
        final String overviewText;
        if (isActive) {
            overviewText = Messages.bind(Messages.overviewActiveLabelText, message, iterations.toString());
        } else {
            overviewText = Messages.bind(Messages.overviewPassiveLabelText, message, iterations.toString());
        }
        overviewLabel.setText(overviewText);
    }

    private void adjustGuiToOperationMode() {
        final PioneerOperationMode operationMode = getProperty(KEY_OPERATION_MODE, PioneerOperationMode.class);
        final boolean passivePossible = hasInputs();
        passiveModeButton.setEnabled(passivePossible);
        final boolean isActive = operationMode == PioneerOperationMode.ACTIVE;
        final boolean isPassive = operationMode == PioneerOperationMode.PASSIVE && passivePossible;
        setEnabled(activeSection, isActive);
        setEnabled(passiveSection, isPassive);
    }

    private void setEnabled(final Composite composite, final boolean enabled) {
        for (final Control control : composite.getChildren()) {
            if (control instanceof Composite) {
                setEnabled((Composite) control, enabled);
            }
            control.setEnabled(enabled);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * The custom {@link Updater} {@link PioneerUpdater} shall be used.
     * </p>
     *
     * @see de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodePropertySection#createUpdater()
     */
    @Override
    protected Updater createUpdater() {
        return new PioneerUpdater();
    }

    @Override
    protected Controller createController() {
        return new PioneerController();
    }

    @Override
    protected Synchronizer createSynchronizer() {
        return new PioneerSynchronizer();
    }

    /**
     * Custom {@link DefaultUpdater} extension to synchronize managed GUI elements that need special handling.
     *
     * @author Christian Weiss
     */
    private final class PioneerUpdater extends DefaultUpdater {

        @Override
        public void updateControl(final Control control, final String propertyName, final Serializable newValue,
            final Serializable oldValue) {
            super.updateControl(control, propertyName, newValue, oldValue); // always invoke super implementation!
            /*
             * The textLabel control shall get a special message as its content. As this Label
             * control is managed (via textControl.setData(CONTROL_PROPERTY_KEY, <propertyKey>)),
             * the updater gets invoked with the Label and the property key as parameters. Upon this
             * invocation the text of the Label is set as desired.
             */
            if (control == textLabel) {
                final String oldTextLabelText = textLabel.getText();
                final String newTextLabelText = Messages.bind(Messages.textLabelMessage, oldTextLabelText, "test");
                textLabel.setText(newTextLabelText);
            }
        }

    }

    /**
     * Custom {@link DefaultSynchronizer} implementation to handle the changes in the model and
     * react accordingly through synchronizing the GUI to reflect those changes.
     * 
     * <p>
     * This implementation reacts upon changes in the 'message' and 'iterations' property through
     * updating a overview label.
     * </p>
     * 
     * @author Christian Weiss
     */
    private final class PioneerSynchronizer extends DefaultSynchronizer {

        /**
         * Updates the overview label if the properties holding the content ('message' and
         * 'iterations') are changed.
         * 
         * {@inheritDoc}
         * 
         * @see de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodePropertySection.DefaultSynchronizer#handlePropertyChange(
         *      java.lang.String, java.io.Serializable, java.io.Serializable)
         */
        @Override
        public void handlePropertyChange(final String propertyName, final Serializable newValue, final Serializable oldValue) {
            super.handlePropertyChange(propertyName, newValue, oldValue); // always invoke super implementation!
            if (propertyName.equals(KEY_MESSAGE) || propertyName.equals(KEY_ITERATIONS)) {
                displayOverview();
            } else if (propertyName.equals(KEY_OPERATION_MODE)) {
                displayOverview();
                adjustGuiToOperationMode();
            }
        }

        /**
         * Updates the wait button enabled-state, if input channels are removed or added.
         * 
         * {@inheritDoc}
         * 
         * @see de.rcenvironment.rce.gui.workflow.editor.properties.WorkflowNodePropertySection.DefaultSynchronizer#handleChannelEvent(
         *          de.rcenvironment.rce.component.workflow.ChannelEvent)
         */
        @Override
        public void handleChannelEvent(final ChannelEvent event) {
            adjustGuiToOperationMode();
        }

    }

    /**
     * Custom {@link DefaultController} implementation to handle the activation of the GUI
     * controlls.
     * 
     * <p>
     * This implementation handles the activation of the non-managed Button control to reset the
     * component to its default values.
     * </p>
     * 
     * @author Christian Weiss
     */
    private final class PioneerController extends DefaultController {

        @Override
        protected void widgetSelected(final SelectionEvent event, final Control source) {
            super.widgetSelected(event, source); // always invoke super implementation!
            if (source == resetButton) {
                final ResetValuesCommand command = createResetValuesCommand();
                execute(command);
            }
        }

    }

    private ResetValuesCommand createResetValuesCommand() {
        return new ResetValuesCommand(getProperty(KEY_MESSAGE, String.class),
            getProperty(KEY_ITERATIONS, Integer.class),
            getProperty(KEY_WAIT, Boolean.class));
    }

    /**
     * Custom command to reset the values through a single CommandStack operation.
     *
     * @author Christian Weiss
     */
    private final class ResetValuesCommand extends AbstractWorkflowNodeCommand {

        private final String oldMessage;

        private final Integer oldIterations;

        private final Boolean oldWait;

        private ResetValuesCommand(final String oldMessage, final Integer oldIterations, final Boolean oldWait) {
            this.oldMessage = oldMessage;
            this.oldIterations = oldIterations;
            this.oldWait = oldWait;
        }

        @Override
        protected void execute2() {
            setPropertyUnobstrusive(KEY_MESSAGE, "ping");
            setPropertyUnobstrusive(KEY_ITERATIONS, 0);
            setPropertyUnobstrusive(KEY_WAIT, false);
        }

        @Override
        protected void undo2() {
            setPropertyUnobstrusive(KEY_MESSAGE, oldMessage);
            setPropertyUnobstrusive(KEY_ITERATIONS, oldIterations);
            setPropertyUnobstrusive(KEY_WAIT, oldWait);
        }

    }

}
