/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.parts;

 import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySource;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.ComponentState;
import de.rcenvironment.rce.component.workflow.SimpleWorkflowRegistry;
import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.Activator;
import de.rcenvironment.rce.gui.workflow.editor.WorkflowPaletteFactory;
import de.rcenvironment.rce.gui.workflow.view.ComponentRuntimeView;
import de.rcenvironment.rce.gui.workflow.view.properties.ComponentInstancePropertySource;
import de.rcenvironment.rce.notification.Notification;
import de.rcenvironment.rce.notification.NotificationSubscriber;
import de.rcenvironment.rce.notification.SimpleNotificationService;

/**
 * Readonly EditPart representing a WorkflowNode.
 * 
 * @author Heinrich Wendel
 */
public class ReadonlyWorkflowNodePart extends WorkflowNodePart {

    private static final Image PREPARING_IMAGE = ImageDescriptor.createFromURL(
        ComponentStateFigureImpl.class.getResource("/resources/icons/preparing.gif")).createImage();
    
    private static final Image READY_IMAGE = ImageDescriptor.createFromURL(
        ComponentStateFigureImpl.class.getResource("/resources/icons/ready.gif")).createImage();
    
    private static final Image PAUSED_IMAGE = ImageDescriptor.createFromURL(
            ComponentStateFigureImpl.class.getResource("/resources/icons/suspend_16.gif")).createImage();
    
    private static final Image RUNNING_IMAGE = ImageDescriptor.createFromURL(
        ComponentStateFigureImpl.class.getResource("/resources/icons/run_enabled.gif")).createImage();
    
    private static final Image FINISHED_IMAGE = ImageDescriptor.createFromURL(
        ComponentStateFigureImpl.class.getResource("/resources/icons/finished.gif")).createImage();
    
    private static final Image FINISHED_NO_RUN_STEP_IMAGE = ImageDescriptor.createFromURL(
        ComponentStateFigureImpl.class.getResource("/resources/icons/finishedNotRun.gif")).createImage();
    
    private static final Image FAILED_IMAGE = ImageDescriptor.createFromURL(
        ComponentStateFigureImpl.class.getResource("/resources/icons/failed.gif")).createImage();
    
    private static final Image CANCELED_IMAGE = ImageDescriptor.createFromURL(
        ComponentStateFigureImpl.class.getResource("/resources/icons/cancel_enabled.gif")).createImage();
    
    private static final Image DISPOSED_IMAGE = ImageDescriptor.createFromURL(
        WorkflowPaletteFactory.class.getResource("/resources/icons/trash_16.gif")).createImage();
    
    private static final Image NO_STATE_IMAGE = ImageDescriptor.createFromURL(
        WorkflowPaletteFactory.class.getResource("/resources/icons/noState.gif")).createImage();
    
    private final NotificationSubscriber stateChangeListener;

    private ComponentStateFigure stateFigure;
    
    private int noOfRuns = 0;
        
    public ReadonlyWorkflowNodePart() {
        stateChangeListener = new ComponentStateChangeListener(this);
    }
    
    @Override
    protected void createEditPolicies() {}

    @Override
    protected IFigure createFigure() {
        // get the plain figure from the parent implementation
        final IFigure figure = super.createBaseFigure();
        // enhance the figure with an activity display element
        final ComponentStateFigure finalStateFigure = new ComponentStateFigureImpl();
        stateFigure = finalStateFigure;
        final int size = 22;
        finalStateFigure.setBounds(new Rectangle(0, 0, size, size));
        figure.add(finalStateFigure);
        Display.getCurrent().asyncExec(new Runnable() {

            @Override
            public void run() {
                initializeStatus();
            }
        });
        // return the figure
        return figure;
    }
    
    @Override
    public void performRequest(Request req) {
        if (req.getType().equals(RequestConstants.REQ_OPEN)) {

            WorkflowInformation wi = getWorkflowInformation();

            openDefaultView(wi, ((WorkflowNode) getModel()).getName());
        }
    }
    
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
        if (type == IPropertySource.class) {
            return new ComponentInstancePropertySource(getWorkflowInformation(), getComponentInstanceDescriptor());
        }
        return super.getAdapter(type);
    }
    
    @Override
    /**
     * Set the tooltip text.
     */
    protected void setTooltipText() {   
        String[] splitClass = ((WorkflowNode) getModel()).getComponentDescription().toString().split("_");
        final String nodeClass = splitClass[splitClass.length - 1];
        final String tooltipText = " " + nodeClass + " - " + ((WorkflowNode) getModel()).getName()
            + " - " + Messages.runs + noOfRuns;
        getFigure().setToolTip(new Label(tooltipText));
    }
    
    private WorkflowInformation getWorkflowInformation() {
        WorkflowInformation wi =
                    (WorkflowInformation) ((WorkflowInformationPart) ((WorkflowPart) getParent()).getParent()).getModel();
        return wi;
    }
    
    private ComponentInstanceDescriptor getComponentInstanceDescriptor() {
        final SimpleWorkflowRegistry registry = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
        final WorkflowNode workflowNode = (WorkflowNode) getModel();
        ComponentInstanceDescriptor cid = null;
        final Set<ComponentInstanceDescriptor> compInstDescs = registry.getComponentInstanceDescriptors(getWorkflowInformation());
        for (final ComponentInstanceDescriptor compInstDesc : compInstDescs) {
            if (compInstDesc.getName().equals(workflowNode.getName())
                        && compInstDesc.getComponentIdentifier().equals(workflowNode.getComponentDescription().getIdentifier())) {
                cid = compInstDesc;
                break;
            }
        }
        return cid;
    }

    private void initializeStatus() {
        
        final WorkflowNode workflowNode = (WorkflowNode) getModel();
        final ComponentInstanceDescriptor cid = getComponentInstanceDescriptor();
        final String stateNotifId = ComponentConstants.STATE_NOTIFICATION_ID_PREFIX + cid.getIdentifier();
        final String noOfRunsNotifId = ComponentConstants.NO_OF_RUNS_NOTIFICATION_ID_PREFIX + cid.getIdentifier();
        final PlatformIdentifier nodePlatform = workflowNode.getComponentDescription().getPlatform();
        final SimpleNotificationService notificationService = new SimpleNotificationService();
        notificationService.subscribe(stateNotifId, stateChangeListener, nodePlatform);
        final List<Notification> stateNotifs = notificationService.getNotifications(stateNotifId, nodePlatform).get(stateNotifId);
        if (stateNotifs != null && stateNotifs.size() > 0) {
            handleStateNotification(stateNotifs.get(stateNotifs.size() - 1));
        }
        notificationService.subscribe(noOfRunsNotifId, stateChangeListener, nodePlatform);
        final List<Notification> noOfRunsNotifs = notificationService.getNotifications(noOfRunsNotifId, nodePlatform).get(noOfRunsNotifId);
        if (noOfRunsNotifs != null && noOfRunsNotifs.size() > 0) {
            handleNoOfRunsNotification(noOfRunsNotifs.get(0));
        }
    }

    private void openDefaultView(WorkflowInformation wi, String nodeName) {
        SimpleWorkflowRegistry registry = new SimpleWorkflowRegistry(Activator.getInstance().getUser());
        Set<ComponentInstanceDescriptor> cis = registry.getComponentInstanceDescriptors(wi);
        
        // Get associated component information
        ComponentInstanceDescriptor cid = null;
        for (ComponentInstanceDescriptor desc: cis) {
            if (desc.getName().equals(nodeName)) {
                cid = desc;
                break;
            }
        }
        
        // Find registered views
        IExtensionRegistry extReg = Platform.getExtensionRegistry();
        IConfigurationElement[] confElements =
            extReg.getConfigurationElementsFor("de.rcenvironment.rce.gui.workflow.monitoring"); //$NON-NLS-1$
        IConfigurationElement[] viewConfElements =
            extReg.getConfigurationElementsFor("org.eclipse.ui.views"); //$NON-NLS-1$
        
        for (final IConfigurationElement confElement : confElements) {

            if (cid.getComponentIdentifier().matches(confElement.getAttribute("component"))
                && confElement.getAttribute("default") != null
                && Boolean.TRUE.toString().matches(confElement.getAttribute("default"))) { //$NON-NLS-1$
                for (final IConfigurationElement viewConfElement : viewConfElements) {
                    
                    if (viewConfElement.getAttribute("id").equals(confElement.getAttribute("view"))) {
                
                        IViewPart view;
                        try {
                            view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().
                                showView(viewConfElement.getAttribute("class"),
                                    cid.getIdentifier(), IWorkbenchPage.VIEW_VISIBLE); //$NON-NLS-1$
        
                            ((ComponentRuntimeView) view).setComponentInstanceDescriptor(cid);
                            view.setFocus();
                        } catch (PartInitException e) {
                            throw new RuntimeException(e);
                        } catch (InvalidRegistryObjectException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
        
    protected void handleStateNotification(Notification notification) {
        if (stateFigure == null) {
            return;
        }
        final Serializable body = notification.getBody();
        final ComponentState state = ComponentState.valueOf((String) body);

        if (state != null) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    stateFigure.setState(state);
                }
            });
        }

    }

    protected void handleNoOfRunsNotification(Notification notification) {
        noOfRuns = (Integer) notification.getBody();
        setTooltipText();
    }
    
    /**
     * Indicates the state of {@link Component}s via a figure.
     * @author Chrisitian Weiss
     */
    public interface ComponentStateFigure extends IFigure {

        /**
         * @param state of {@link Component}.
         */
        void setState(ComponentState state);

        /**
         * @return state of {@link Component}.
         */
        ComponentState getState();

    }

    /**
     * Implementation of {@link ComponentStateFigure}.
     * @author Christian Weiss
     */
    public final class ComponentStateFigureImpl extends Panel implements ComponentStateFigure {
        
        private final ImageFigure innerImageFigure = new ImageFigure(NO_STATE_IMAGE);
        {
            add(innerImageFigure);
            setVisible(true);
            setOpaque(true);
        }

        private ComponentState state;

        @Override
        public void setBounds(final Rectangle rect) {
            super.setBounds(rect);
            final Rectangle innerRectangleBounds = new Rectangle(rect.x + 3, rect.y + 3, rect.width - 6, rect.height - 6);
            innerImageFigure.setBounds(innerRectangleBounds);
        }

        @Override
        public void setState(ComponentState state) {
            if (this.state == state) {
                return;
            }
            this.state = state;
            switch (state) {
            case PREPARING:
                innerImageFigure.setImage(PREPARING_IMAGE);
                break;
            case READY:
                innerImageFigure.setImage(READY_IMAGE);
                break;
            case RUNNING:
                innerImageFigure.setImage(RUNNING_IMAGE);
                break;
            case PAUSED:
                innerImageFigure.setImage(PAUSED_IMAGE);
                break;
            case CANCELED:
                innerImageFigure.setImage(CANCELED_IMAGE);
                break;
            case FAILED:
                innerImageFigure.setImage(FAILED_IMAGE);
                break;
            case FINISHED:
                innerImageFigure.setImage(FINISHED_IMAGE);
                break;
            case FINISHED_NO_RUN_STEP:
                innerImageFigure.setImage(FINISHED_NO_RUN_STEP_IMAGE);
                break;
            case DISPOSED:
                innerImageFigure.setImage(DISPOSED_IMAGE);
                break;
            default:
                innerImageFigure.setImage(NO_STATE_IMAGE);
                break;
            }
            setVisible(true);
            refresh();
        }

        @Override
        public ComponentState getState() {
            return state;
        }

    }

}
