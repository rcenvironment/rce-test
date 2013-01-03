/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.editor.properties;

import java.io.Serializable;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import de.rcenvironment.gui.commons.configuration.BooleanPropertyDescriptor;
import de.rcenvironment.gui.commons.configuration.ConfigurationViewer;
import de.rcenvironment.gui.commons.configuration.ConfigurationViewerContentProvider;
import de.rcenvironment.gui.commons.configuration.ConfigurationViewerLabelProvider;
import de.rcenvironment.gui.commons.configuration.IConfigurationSource;
import de.rcenvironment.rce.component.workflow.WorkflowNode;


/**
 * {@link WorkflowNodePropertySection} for displaying and editing the properties of a workflow node.
 *
 * @author Christian Weiss
 */
public class WorkflowNodePropertiesSection extends WorkflowNodePropertySection {

    private static final String NULL_CONTROL_PROPERTY_KEY = "";

    private static final int MINIMUM_HEIGHT = 60;

    /** The content provider for the {@link #configurationViewer}. */
    private final ConfigurationViewerContentProvider configurationViewerContentProvider = new ConfigurationViewerContentProvider();

    /** The label provider for the {@link #configurationViewer}. */
    private final ConfigurationViewerLabelProvider configurationViewerLabelProvider = new ConfigurationViewerLabelProvider();

    private final ISelectionChangedListener propertyValueTextSynchronizer = new ConfigurationViewerSelectionChangedListener();

    private ConfigurationViewer configurationViewer;

    private Text propertyValueText;

    @Override
    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        final TabbedPropertySheetWidgetFactory toolkit = aTabbedPropertySheetPage.getWidgetFactory();

        final Composite content = parent;
        content.setLayout(new GridLayout(1, true));
        
        GridData layoutData;
        
        layoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        parent.setLayoutData(layoutData);

        final SashForm sashForm = createSash(content, SWT.VERTICAL);
        toolkit.adapt(sashForm);
    }

//    @Override
//    protected void createCompositeContent(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
//        final TabbedPropertySheetWidgetFactory toolkit = aTabbedPropertySheetPage.getWidgetFactory();
//
//        final Composite content = parent;
//        content.setLayout(new GridLayout(1, true));
//        
//        GridData layoutData;
//        
//        final Composite propertiesContainer = toolkit.createFlatFormComposite(content);
//        layoutData =
//            new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
//        propertiesContainer.setLayoutData(layoutData);
////        propertiesContainer.setLayout(new FillLayout());
//        propertiesContainer.setLayout(new FillLayout(SWT.VERTICAL));
//        final Section propertiesSection = toolkit.createSection(propertiesContainer, Section.TITLE_BAR | Section.EXPANDED);
//        propertiesSection.setLayout(new FillLayout(SWT.VERTICAL));
////        propertiesSection.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
////        propertiesSection.setLayout(new FillLayout());
//        propertiesSection.setText("Properties"); //Messages.propertiesSectionLabel);
//        final Composite propertiesClient = toolkit.createComposite(propertiesSection);
////        layoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
////        propertiesClient.setLayoutData(layoutData);
//        propertiesClient.setLayout(new FillLayout());
//        // sash
//        final SashForm sashForm = createSash(propertiesClient, SWT.VERTICAL);
//        toolkit.adapt(sashForm);
//        toolkit.adapt(configurationViewer.getTree());
//        propertiesSection.setClient(propertiesClient);
//        parent.layout();
//    }

    private SashForm createSash(final Composite content, final int style) {
        GridData layoutData;
        // sash
        final SashForm sashForm = new SashForm(content, style);
        // configuration viewer
        configurationViewer = new ConfigurationViewer(sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
        layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.minimumHeight = MINIMUM_HEIGHT;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        configurationViewer.getTree().getParent().setLayoutData(layoutData);
        configurationViewer.setContentProvider(configurationViewerContentProvider);
        configurationViewer.setLabelProvider(configurationViewerLabelProvider);
        // property value text
        propertyValueText = new Text(sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        propertyValueText.setText("");
        layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.minimumHeight = MINIMUM_HEIGHT;
        propertyValueText.setLayoutData(layoutData);
        propertyValueText.setData(CONTROL_PROPERTY_KEY, NULL_CONTROL_PROPERTY_KEY);
        sashForm.setWeights(new int[] { 3, 1 });
        layoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        layoutData.minimumHeight = MINIMUM_HEIGHT;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        sashForm.setLayoutData(layoutData);
        return sashForm;
    }

    @Override
    protected void afterInitializingModelBinding() {
        configurationViewer.addSelectionChangedListener(propertyValueTextSynchronizer);
    }

    @Override
    protected void beforeTearingDownModelBinding() {
        configurationViewer.removeSelectionChangedListener(propertyValueTextSynchronizer);
    }

    @Override
    protected Synchronizer createSynchronizer() {
        return new PropertiesSynchronizer();
    }

    @Override
    public void refresh() {
        super.refresh();
        configurationViewer.setInput(new PropertiesConfigurationSource(getCommandStack(), (WorkflowNode) getConfiguration()));
        configurationViewer.getTree().update();
    }

    /**
     * {@link Synchronizer} implementation to update the {@link #configurationViewer} upon property
     * changes.
     * 
     * @author Christian Weiss
     */
    protected class PropertiesSynchronizer extends DefaultSynchronizer {

        @Override
        public void handlePropertyChange(final String propertyName, final Serializable newValue, final Serializable oldValue) {
            super.handlePropertyChange(propertyName, newValue, oldValue);
            super.handlePropertyChange(propertyName, newValue, oldValue);
            configurationViewer.refresh();
        }

    }

    /**
     * {@link IConfigurationSource} implementation to wrap the properties of a
     * {@link de.rcenvironment.rce.component.workflow.ReadableComponentInstanceConfiguration} to
     * display them in a {@link ConfigurationViewer}.
     * 
     * @author Christian Weiss
     */
    protected class PropertiesConfigurationSource extends ComponentPropertySource implements IConfigurationSource {

        public PropertiesConfigurationSource(final CommandStack stack, final WorkflowNode node) {
            super(stack, node);
        }

        @Override
        public Serializable getProperty(String key) {
            return WorkflowNodePropertiesSection.this.getProperty(key);
        }

        @Override
        public Object getPropertyValue(final Object key) {
            return getProperty((String) key);
        }

        @Override
        public void setPropertyValue(final Object key, final Object value) {
            final String propertyName = (String) key;
            Serializable propertyValue = (Serializable) value;
            final Class<? extends Serializable> type = WorkflowNodePropertiesSection.this.getPropertyType(propertyName);
            if (value != null && !type.isAssignableFrom(value.getClass())) {
                // cast to the real property type necessary
                if (type == Integer.class) {
                    propertyValue = new Integer(value.toString());
                } else if (type == Double.class) {
                    propertyValue = new Double(value.toString());
                } else if (type == Boolean.class) {
                    propertyValue = Boolean.parseBoolean(value.toString());
                }
            }
            WorkflowNodePropertiesSection.this.setProperty(propertyName, propertyValue);
        }

        @Override
        public IPropertyDescriptor[] getConfigurationPropertyDescriptors() {
            final IPropertyDescriptor[] propertyDescriptors = getPropertyDescriptors();
            final int propertyCount = propertyDescriptors.length;
            final IPropertyDescriptor[] result = new IPropertyDescriptor[propertyCount];
            for (int index = 0; index < propertyCount; ++index) {
                final IPropertyDescriptor propertyDescriptor = propertyDescriptors[index];
                final String key = propertyDescriptor.getId().toString();
                final IPropertyDescriptor resultPart;
                final Class<? extends Serializable> propertyType = getPropertyType(key);
                if (propertyType == Boolean.class) {
                    resultPart = new BooleanPropertyDescriptor(propertyDescriptor.getId(), propertyDescriptor.getDisplayName());
                } else {
                    resultPart = propertyDescriptor;
                    if (propertyType == Integer.class){
                        ((TextPropertyDescriptor) propertyDescriptor).setValidator(new IntegerValidator(true));
                    } else if (propertyType == Double.class){
                        ((TextPropertyDescriptor) propertyDescriptor).setValidator(new DoubleValidator(true));
                    }
                }
                result[index] = resultPart;
            }
            return result;
        }

    }

    /**
     * {@link ICellEditorValidator} for {@link Integer} inputs.
     *
     * @author Christian Weiss
     */
    private static final class IntegerValidator implements ICellEditorValidator {

        private final boolean acceptNull;

        private IntegerValidator(final boolean acceptNull) {
            this.acceptNull = acceptNull;
        }

        @Override
        public String isValid(final Object value) {
            String result = null;
            final boolean invalidNullValue = value == null && !acceptNull;
            final boolean nonString = value != null && !(value instanceof String);
            if (invalidNullValue) {
                result = "Not a non-null value";
            }
            if (!invalidNullValue && nonString) {
                result = "Not a String";
            }
            if (result != null) {
                return result;
            }
            try {
                Integer.parseInt((String) value);
                return null;
            } catch (final NumberFormatException e) {
                return e.getLocalizedMessage();
            }
        }
        
    }

    /**
     * {@link ICellEditorValidator} for {@link Double} inputs.
     *
     * @author Christian Weiss
     */
    private static final class DoubleValidator implements ICellEditorValidator {

        private final boolean acceptNull;

        private DoubleValidator(final boolean acceptNull) {
            this.acceptNull = acceptNull;
        }

        @Override
        public String isValid(final Object value) {
            String result = null;
            final boolean invalidNullValue = value == null && !acceptNull;
            final boolean nonString = value != null && !(value instanceof String);
            if (invalidNullValue) {
                result = "Not a non-null value";
            }
            if (!invalidNullValue && nonString) {
                result = "Not a String";
            }
            if (result != null) {
                return result;
            }
            try {
                Double.parseDouble((String) value);
                return null;
            } catch (final NumberFormatException e) {
                return e.getLocalizedMessage();
            }
        }
        
    }

    /**
     * {@link ISelectionChangedListener} to listen to selection changes in the
     * {@link #configurationViewer} and update the {@link #propertyValueText} with the property
     * content.
     * 
     * @author Christian Weiss
     */
    private class ConfigurationViewerSelectionChangedListener implements ISelectionChangedListener {

        @Override
        public void selectionChanged(final SelectionChangedEvent event) {
            String propertyKey = NULL_CONTROL_PROPERTY_KEY;
            final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            if (selection.size() == 1) {
                final Object firstElement = selection.getFirstElement();
                if (firstElement instanceof TextPropertyDescriptor) {
                    final IPropertyDescriptor propertyDescriptor = (IPropertyDescriptor) firstElement;
                    propertyKey = (String) propertyDescriptor.getId();
                }
            }
            final boolean validProperty = propertyKey != null && !propertyKey.equals(NULL_CONTROL_PROPERTY_KEY);
            final boolean stringProperty = validProperty && getPropertyType(propertyKey) == String.class;
            propertyValueText.setEnabled(stringProperty);
            propertyValueText.setData(CONTROL_PROPERTY_KEY, propertyKey);
            final Serializable value = getProperty(propertyKey);
            getUpdater().initializeControl(propertyValueText, propertyKey, value);
        }
        
    }

}
