/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.datamanagement.browser;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.rcenvironment.rce.authentication.AuthenticationException;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.component.datamanagement.history.HistoryMetaDataKeys;
import de.rcenvironment.rce.datamanagement.SimpleFileDataService;
import de.rcenvironment.rce.datamanagement.SimpleMetaDataService;
import de.rcenvironment.rce.datamanagement.SimpleQueryService;
import de.rcenvironment.rce.datamanagement.commons.DMQLQuery;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaData;
import de.rcenvironment.rce.datamanagement.commons.MetaDataKeys;
import de.rcenvironment.rce.datamanagement.commons.MetaDataQuery;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResult;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNode;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNodeType;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNodeUtils;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.HistoryObjectSubtreeBuilder;

/**
 * @author Markus Litz
 * @author Robert Mischke
 * @author Christian Weiß
 */
public class DMContentProvider implements ITreeContentProvider {

    private static final String NODE_TEXT_FORMAT_TITLE_PLUS_TIMESTAMP = "%s (%s)";

    private static final MetaData METADATA_COMPONENT_CONTEXT_UUID = new MetaData(
            MetaDataKeys.COMPONENT_CONTEXT_UUID, true, true);

    private static final MetaData METADATA_COMPONENT_CONTEXT_NAME = new MetaData(
            MetaDataKeys.COMPONENT_CONTEXT_NAME, true, true);

    private static final MetaData METADATA_COMPONENT_UUID = new MetaData(
            MetaDataKeys.COMPONENT_UUID, true, true);

    private static final MetaData METADATA_COMPONENT_NAME = new MetaData(
            MetaDataKeys.COMPONENT_NAME, true, true);

    private static final MetaData METADATA_FILENAME = new MetaData(
            MetaDataKeys.FILENAME, true, true);

    private static final MetaData METADATA_DATE = new MetaData(
            MetaDataKeys.Managed.DATE, false, true);

    private static final MetaData METADATA_HISTORY_OBJECT_CLASS_NAME = new MetaData(
            HistoryMetaDataKeys.HISTORY_OBJECT_CLASS_NAME, true, true);

    private static final MetaData METADATA_HISTORY_USER_INFO_TEXT = new MetaData(
            HistoryMetaDataKeys.HISTORY_USER_INFO_TEXT, true, true);

    private static final MetaData METADATA_HISTORY_ORDERING = new MetaData(
            HistoryMetaDataKeys.HISTORY_TIMESTAMP, true, true);

    private static final String EXTENSION_POINT_ID_SUBTREE_BUILDER =
        "de.rcenvironment.rce.gui.datamanagement.browser.historysubtreebuilder";

    protected final Log log = LogFactory.getLog(getClass());

    private DMBrowserNode invisibleRoot = null;

    /**
     * FileDataService for storing/loading resources to the data management.
     */
    private SimpleFileDataService dataService = null;

    /**
     * Service to send queries to the rce data management.
     */
    private SimpleQueryService queryService = null;

    /**
     * Service for meta data.
     */
    private SimpleMetaDataService metaDataService;

    /**
     * The proxy cert from the component.
     */
    private User certificate = null;

    private Map<String, HistoryObjectSubtreeBuilder> historySubtreeBuilders;

    /** Cached results for the MetaDataQuery. */
    private MetaDataResultList metaData;

    /** Used to format timestamps in MetaData to readable dates. */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final List<DMBrowserNodeContentAvailabilityHandler> contentAvailabilityHandlers =
            new CopyOnWriteArrayList<DMBrowserNodeContentAvailabilityHandler>();

    private final Executor contentRetrieverExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private final Set<DMBrowserNode> inProgress = new CopyOnWriteArraySet<DMBrowserNode>();

    protected DMContentProvider() throws AuthenticationException {
        certificate = de.rcenvironment.rce.authentication.Session.getInstance()
                .getUser();
        dataService = new SimpleFileDataService(certificate);
        queryService = new SimpleQueryService(certificate);
        metaDataService = new SimpleMetaDataService(certificate);

        registerBuilders();
    }

    private void registerBuilders() {

        historySubtreeBuilders = new HashMap<String, HistoryObjectSubtreeBuilder>();

        // get all extensions
        IConfigurationElement[] config = Platform.getExtensionRegistry()
            .getConfigurationElementsFor(EXTENSION_POINT_ID_SUBTREE_BUILDER);
        for (IConfigurationElement e : config) {
            try {
                final Object o = e.createExecutableExtension("class");
                if (o instanceof HistoryObjectSubtreeBuilder) {
                    HistoryObjectSubtreeBuilder builder = (HistoryObjectSubtreeBuilder) o;
                    for (String supported : builder.getSupportedObjectClassNames()) {
                        // do not allow ambiguous mappings
                        if (historySubtreeBuilders.containsKey(supported)) {
                            throw new IllegalStateException("More than one builder tried to register for key " + supported);
                        }
                        // register
                        historySubtreeBuilders.put(supported, builder);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Registered subtree builder " + o.getClass());
                    }
                }
            } catch (CoreException ex) {
                log.error("Error registering extension " + e, ex);
            }
        }

    }

    @Override
    public DMBrowserNode[] getChildren(Object parent) {
        final DMBrowserNode[] result = getChildren(parent, true);
        return result;
    }

    /**
     * Returns the child elements of the given parent element with the option to choose between
     * synchronous and asynchronous execution.
     * 
     * @param parent the parent {@link DMBrowserNode}
     * @param async true, if execution shall be performed asynchronously in a different thread
     * @return the children
     */
    public DMBrowserNode[] getChildren(Object parent, boolean async) {
        final DMBrowserNode node = (DMBrowserNode) parent;

        if (node.areChildrenKnown()) {
            return node.getChildrenAsArray();
        } else {
            final Runnable retrieverTask = new RetrieverTask(node);
            if (async) {
                Job job = new Job(Messages.dataManagementBrowser) {
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        try {
                            monitor.beginTask(Messages.fetchingData, 3);
                            monitor.worked(2);
                            retrieverTask.run();
                            monitor.worked(3);
                            return Status.OK_STATUS;                    
                        } finally {
                            monitor.done();
                        }
                    };
                };
                job.setUser(true);
                job.schedule();
                // return a wait signal node as only child
                final DMBrowserNode waitSignalNode = new DMBrowserNode(
                        Messages.waitSignalNodeLabel);
                waitSignalNode.setType(DMBrowserNodeType.Loading);
                waitSignalNode.markAsLeaf();
                return new DMBrowserNode[] { waitSignalNode };
            } else {
                retrieverTask.run();
                return node.getChildrenAsArray();
            }
        }
    }

    // TODO caching!
    private MetaDataResultList getMetaDataForTimeline(final UUID workflowId) {
        // create MetaDataQuery
        MetaDataQuery query = new MetaDataQuery();
        query.addMetaDataKeyExistsConstraint(
                METADATA_HISTORY_OBJECT_CLASS_NAME
//                , MetaDataQuery.REVISION_FIRST, MetaDataQuery.REVISION_LAST
        );
        query.addMetaDataKeyExistsConstraint(METADATA_COMPONENT_UUID
//                , MetaDataQuery.REVISION_FIRST, MetaDataQuery.REVISION_LAST
        );
        query.addMetaDataConstraint(METADATA_COMPONENT_CONTEXT_UUID, workflowId.toString()
//                , MetaDataQuery.REVISION_FIRST, MetaDataQuery.REVISION_LAST
        );
        // FIXME remove
        final long start = System.currentTimeMillis();
        // perform MetaDataQuery
        final MetaDataResultList result = queryService.executeMetaDataQuery(query, 0);
        log.debug(String.format("metadata timeline query took %d milli", (System.currentTimeMillis() - start)));
        return result;
    }

    // TODO caching!
    private MetaDataResultList getMetaDataForComponent(final UUID workflowId, final String componentId) {
        // create MetaDataQuery
        MetaDataQuery query = new MetaDataQuery();
        query.addMetaDataKeyExistsConstraint(
                METADATA_HISTORY_OBJECT_CLASS_NAME
                // , MetaDataQuery.REVISION_FIRST, MetaDataQuery.REVISION_LAST
        );
        query.addMetaDataKeyExistsConstraint(METADATA_COMPONENT_UUID
                // , MetaDataQuery.REVISION_FIRST, MetaDataQuery.REVISION_LAST
        );
        query.addMetaDataConstraint(METADATA_COMPONENT_CONTEXT_UUID, workflowId.toString()
//          , MetaDataQuery.REVISION_FIRST, MetaDataQuery.REVISION_LAST
        );
        query.addMetaDataConstraint(METADATA_COMPONENT_UUID, componentId
                // , MetaDataQuery.REVISION_FIRST, MetaDataQuery.REVISION_LAST
        );
        // FIXME remove
        final long start = System.currentTimeMillis();
        // perform MetaDataQuery
        final MetaDataResultList result = queryService.executeMetaDataQuery(query, 0);
        log.debug(String.format("metadata component query took %d milli", (System.currentTimeMillis() - start)));
        return result;
    }

    // TODO caching!
    private MetaDataResultList getMetaDataForComponents(final UUID workflowId) {
        // TODO DMQL
        // create MetaDataQuery
        MetaDataQuery query = new MetaDataQuery();
        query.addMetaDataKeyExistsConstraint(
                METADATA_HISTORY_OBJECT_CLASS_NAME
                // , MetaDataQuery.REVISION_FIRST, MetaDataQuery.REVISION_LAST
        );
        query.addMetaDataKeyExistsConstraint(METADATA_COMPONENT_UUID
                // , MetaDataQuery.REVISION_FIRST, MetaDataQuery.REVISION_LAST
        );
        query.addMetaDataConstraint(METADATA_COMPONENT_CONTEXT_UUID, workflowId.toString()
                // , MetaDataQuery.REVISION_FIRST, MetaDataQuery.REVISION_LAST
        );
        // FIXME remove
        final long start = System.currentTimeMillis();
        // perform MetaDataQuery
        final MetaDataResultList result = queryService.executeMetaDataQuery(query, 0);
        log.debug(String.format("metadata components query took %d milli", (System.currentTimeMillis() - start)));
        return result;
    }

    private MetaDataResultList getMetaData(final String query) {
        final DMQLQuery queryInstance = new DMQLQuery(query);
        // FIXME remove
        final long start = System.currentTimeMillis();
        final MetaDataResultList results = queryService.executeMetaDataQuery(queryInstance, 0);
        log.debug(String.format("metadata dmql query took %d millis  ", (System.currentTimeMillis() - start)));
        return results;
    }

    private void createChildrenForHistoryRootNode(DMBrowserNode parent) {
        // get metadata
        MetaDataResultList metaDataResultList = getMetaData(
                "SELECT rce.common.component_context_uuid, rce.common.component_context_name, "
                    + "MIN(" + HistoryMetaDataKeys.HISTORY_TIMESTAMP + ") "
                    + "GROUP BY rce.common.component_context_uuid, rce.common.component_context_name");
        // map<id, node> to keep track of already-known contexts and their tree nodes
        Map<String, DMBrowserNode> encounteredContexts = new HashMap<String, DMBrowserNode>();
        // map<id, node> to keep track of already-known contexts and their tree nodes
        Map<String, Long> workflowStarts = new HashMap<String, Long>();
        // map<id, node> to keep track of already-known timelines and their tree nodes
        Map<String, DMBrowserNode> timelines = new HashMap<String, DMBrowserNode>();
        final MetaData minMetadataHistoryOrdering = new MetaData(
            "MIN(" + HistoryMetaDataKeys.HISTORY_TIMESTAMP + ")", true, true);

        /*
         * Iterate over the results and for each new workflow create a node and register the
         * subnodes as children.
         */
        for (final MetaDataResult result : metaDataResultList) {
            // extract the MetaDataSet
            MetaDataSet mds = result.getMetaDataSet();
            // extract the id of the workflow
            String contextUUID = mds
                    .getValue(METADATA_COMPONENT_CONTEXT_UUID);
            // extract the name of the workflow
            String contextName = mds
                    .getValue(METADATA_COMPONENT_CONTEXT_NAME);
            // if the workflow already has a node, this node is used to register the child nodes
            DMBrowserNode contextDMObject = encounteredContexts.get(contextUUID);
            // if it's a new workflow, a new workflow node is created as parent node
            final String startTimeValue = mds.getValue(minMetadataHistoryOrdering);
            // fix for mantis #6776: apply "virtual" timestamp value so sorting works again 
            mds.setValue(METADATA_HISTORY_ORDERING, startTimeValue);
            final boolean startTimeSet = startTimeValue != null;
            final Long workflowStart = workflowStarts.get(contextUUID);
            final long startTime;
            if (startTimeSet) {
                startTime = Long.parseLong(startTimeValue);
            } else {
                startTime = 0;
            }
            if (contextDMObject == null) {
                // create the workflow node and set its attributes
                contextDMObject = new DMBrowserNode("Workflow: " + contextName, parent);
                contextDMObject.setMetaData(mds);
                contextDMObject.setType(DMBrowserNodeType.Workflow);
                contextDMObject.setWorkflowUUID(mds.getValue(METADATA_COMPONENT_CONTEXT_UUID));
                // contextDMObject.setAssociatedFilename(contextName);
                // add workflow node to the child node set of the parent (root) node
                parent.addChild(contextDMObject);
                // register as known workflow
                encounteredContexts.put(contextUUID, contextDMObject);
                // save workflow start timestamp
                workflowStarts.put(contextUUID, startTime);
            } else {
                if (workflowStart > startTime) {
                    workflowStarts.put(contextUUID, startTime);
                }
            }
        }
        // update the title of each workflow node to display the start time of the workflow
        for (Map.Entry<String, DMBrowserNode> entry : encounteredContexts.entrySet()) {
            final String uuid = entry.getKey();
            final DMBrowserNode workflowNode = entry.getValue();
            final Long startTime = workflowStarts.get(uuid);
            final String startDateString = dateFormat.format(new Date(startTime));
            workflowNode.setTitle(String.format(NODE_TEXT_FORMAT_TITLE_PLUS_TIMESTAMP,
                workflowNode.getTitle(), startDateString));
        }
        // sort nodes by start time
        parent.sortChildren(DMBrowserNodeUtils.COMPARATOR_BY_HISTORY_TIMESTAMP);
    }

    private void createChildrenForWorkflowNode(final DMBrowserNode workflowNode) {
        // create timeline child
        final DMBrowserNode timelineDMObject = new DMBrowserNode("Timeline");
        timelineDMObject.setType(DMBrowserNodeType.Timeline);
        workflowNode.addChild(timelineDMObject);
        // create components child
        final DMBrowserNode componentsNode = new DMBrowserNode("By Component");
        componentsNode.setType(DMBrowserNodeType.Components);
        workflowNode.addChild(componentsNode);
    }

    private void createChildrenForTimelineNode(final DMBrowserNode timelineNode) {
        // extract the id of the desired workflow
        final String workflowUUID = timelineNode.getParent().getMetaData().getValue(METADATA_COMPONENT_CONTEXT_UUID);
        // get metadata
        final MetaDataResultList metaDataResultList = getMetaDataForTimeline(UUID.fromString(workflowUUID));
        /*
         * Iterate over the results and for each new workflow create a node and register the
         * subnodes as children.
         */
        for (final MetaDataResult result : metaDataResultList) {
            // extract the MetaDataSet
            final MetaDataSet metaDataSet = result.getMetaDataSet();
            // extract the id of the workflow
            final String contextUUID = metaDataSet.getValue(METADATA_COMPONENT_CONTEXT_UUID);
            assert workflowUUID.equals(contextUUID);
            // extract the id of the data reference
            final String dataReferenceId = result.getId().toString();
            final Long startTime = Long.parseLong(metaDataSet.getValue(METADATA_HISTORY_ORDERING));
            final String startDateString = dateFormat.format(new Date(startTime));
            final String nodeName = metaDataSet.getValue(METADATA_HISTORY_USER_INFO_TEXT);
            DMBrowserNode dmoChild = new DMBrowserNode(
                    String.format(NODE_TEXT_FORMAT_TITLE_PLUS_TIMESTAMP, nodeName, startDateString));
            dmoChild.setDataReferenceId(dataReferenceId);
            dmoChild.setMetaData(metaDataSet);
            dmoChild.setType(DMBrowserNodeType.HistoryObject);
            // dmoChild.setAssociatedFilename(nodeName);
            timelineNode.addChild(dmoChild);
        }
        // sort nodes by start time
        timelineNode.sortChildren(DMBrowserNodeUtils.COMPARATOR_BY_HISTORY_TIMESTAMP);
    }

    private DMBrowserNode getParentNodeByType(final DMBrowserNode node, final DMBrowserNodeType type) {
        DMBrowserNode search = node;
        do {
            if (type == search.getType()) {
                return search;
            }
            search = search.getParent();
        } while (search != null);
        return null;
    }

    private void createChildrenForComponentsNode(final DMBrowserNode componentsNode) {
        // extract the id of the desired workflow
        final DMBrowserNode parentWorkflow = getParentNodeByType(componentsNode, DMBrowserNodeType.Workflow);
        final String workflowUUID = parentWorkflow.getMetaData().getValue(METADATA_COMPONENT_CONTEXT_UUID);
        // get metadata
        final MetaDataResultList metaDataResultList = getMetaDataForComponents(UUID.fromString(workflowUUID));
        // the set of unique components (ids)
        final Set<String> componentIds = new HashSet<String>();
        /*
         * Iterate over the results and for each new workflow create a node and register the
         * subnodes as children.
         */
        for (final MetaDataResult result : metaDataResultList) {
            // extract the MetaDataSet
            final MetaDataSet metaDataSet = result.getMetaDataSet();
            // extract the id of the workflow
            final String contextUUID = metaDataSet.getValue(METADATA_COMPONENT_CONTEXT_UUID);
            if (!workflowUUID.equals(contextUUID)) {
                continue;
            }
            final String componentId = metaDataSet.getValue(METADATA_COMPONENT_UUID);
            if (componentIds.contains(componentId)) {
                continue;
            } else {
                componentIds.add(componentId);
            }
            final String componentName = metaDataSet.getValue(METADATA_COMPONENT_NAME);
            final DMBrowserNode componentNode = new DMBrowserNode(componentName);
            componentNode.setType(DMBrowserNodeType.Component);
            componentNode.setMetaData(metaDataSet);
            componentsNode.addChild(componentNode);
        }
        // sort nodes by node title
        componentsNode.sortChildren(DMBrowserNodeUtils.COMPARATOR_BY_NODE_TITLE);
    }

    private void createChildrenForComponentNode(final DMBrowserNode componentsNode) {
        // extract the id of the desired workflow
        final DMBrowserNode parentWorkflow = getParentNodeByType(componentsNode, DMBrowserNodeType.Workflow);
        final String workflowUUID = parentWorkflow.getMetaData().getValue(METADATA_COMPONENT_CONTEXT_UUID);
        // extract the id of the desired component
        final DMBrowserNode parentComponents = getParentNodeByType(componentsNode, DMBrowserNodeType.Component);
        final String componentId = parentComponents.getMetaData().getValue(METADATA_COMPONENT_UUID);
        // get metadata
        final MetaDataResultList metaDataResultList = getMetaDataForComponent(UUID.fromString(workflowUUID), componentId);
        /*
         * Iterate over the results and for each new workflow create a node and register the
         * subnodes as children.
         */
        for (final MetaDataResult result : metaDataResultList) {
            // extract the MetaDataSet
            final MetaDataSet metaDataSet = result.getMetaDataSet();
            // extract the id of the workflow
            final String contextUUID = metaDataSet.getValue(METADATA_COMPONENT_CONTEXT_UUID);
            assert workflowUUID.equals(contextUUID);
            if (!workflowUUID.equals(contextUUID)) {
                throw new AssertionError();
            }
            final String currentComponentId = metaDataSet.getValue(METADATA_COMPONENT_UUID);
            if (!componentId.equals(currentComponentId)) {
                continue;
            }
            // extract the id of the data reference
            final String dataReferenceId = result.getId().toString();

            final Long startTime = Long.parseLong(metaDataSet.getValue(METADATA_HISTORY_ORDERING));
            final String startDateString = dateFormat.format(new Date(startTime));
            final String nodeName = metaDataSet.getValue(METADATA_HISTORY_USER_INFO_TEXT);
            DMBrowserNode dmoChild = new DMBrowserNode(
                  String.format(NODE_TEXT_FORMAT_TITLE_PLUS_TIMESTAMP, nodeName, startDateString));
            dmoChild.setDataReferenceId(dataReferenceId);
            dmoChild.setMetaData(metaDataSet);
            dmoChild.setType(DMBrowserNodeType.HistoryObject);
            // dmoChild.setAssociatedFilename(nodeName);
            componentsNode.addChild(dmoChild);
        }
        // sort nodes by start time
        componentsNode.sortChildren(DMBrowserNodeUtils.COMPARATOR_BY_HISTORY_TIMESTAMP);
    }

    private void createChildrenForHistoryObjectNode(DMBrowserNode node) {
        String className = node.getMetaData().getValue(METADATA_HISTORY_OBJECT_CLASS_NAME);
        HistoryObjectSubtreeBuilder builder = historySubtreeBuilders.get(className);

        if (builder == null) {
            final String errorMessage = "No subtree builder found for history object class " + className;
            // TODO add warning as a tree node for better visibility?
            log.warn(errorMessage);
            throw new RuntimeException(errorMessage);
        } else {
            try {
                Serializable historyObject;
                historyObject = deserializeHistoryObjectFromDataReference(node.getDataReferenceId(), builder);
                builder.buildInitialHistoryObjectSubtree(historyObject, node);
            } catch (IOException e) {
                // TODO add warning as a tree node for better visibility?
                log.warn("Error retrieving data for history entry");
            } catch (ClassNotFoundException e) {
                // TODO add warning as a tree node for better visibility?
                log.warn("Error dehydrating history entry (class not found)");
            }
        }
    }

    private Serializable deserializeHistoryObjectFromDataReference(final String dataReferenceGuid,
        final HistoryObjectSubtreeBuilder builder) throws IOException, ClassNotFoundException {
        return deserializeHistoryObjectFromDataReference(UUID.fromString(dataReferenceGuid), builder);
    }

    private Serializable deserializeHistoryObjectFromDataReference(final UUID dataReferenceGuid, final HistoryObjectSubtreeBuilder builder)
        throws IOException, ClassNotFoundException {
        return deserializeHistoryObjectFromDataReference(queryService.getReference(dataReferenceGuid), builder);
    }

    private Serializable deserializeHistoryObjectFromDataReference(DataReference dr, HistoryObjectSubtreeBuilder builder)
        throws IOException, ClassNotFoundException {
        if (dr == null) {
            return null;
        }
        InputStream dmStream = dataService.getStreamFromDataReference(dr, DataReference.HEAD_REVISION);
        try {
            ObjectInputStream ois = new ObjectInputStream(dmStream);
            return builder.deserializeHistoryObject(ois);
        } finally {
            dmStream.close();
        }
    }

    @Override
    public DMBrowserNode[] getElements(Object inputElement) {
        final DMBrowserNode[] result = getChildren(inputElement);
        return result;
    }

    @Override
    public Object getParent(Object element) {
        DMBrowserNode dmo = (DMBrowserNode) element;
        if (element == null) {
            return null;
        }

        return dmo.getParent();
    }

    @Override
    public boolean hasChildren(Object parent) {
        if (parent instanceof DMBrowserNode) {
            DMBrowserNode dmo = (DMBrowserNode) parent;
            if (!dmo.areChildrenKnown()) {
                // children unknown -> report "yes" to allow unfolding
                return true;
            } else {
                // children known -> report "yes" if children list not empty
                return dmo.getNumChildren() != 0;
            }
        } else {
            return true;
        }
    }

    /**
     * FIXME @weis_cr: javadoc.
     */
    public void clear() {
        metaData = null;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

    /**
     * Adds a {@link DMBrowserNodeContentAvailabilityHandler}.
     * 
     * @param contentAvailabilityHandler the {@link DMBrowserNodeContentAvailabilityHandler}
     */
    public void addContentAvailabilityHandler(
            final DMBrowserNodeContentAvailabilityHandler contentAvailabilityHandler) {
        contentAvailabilityHandlers.add(contentAvailabilityHandler);
    }

    /**
     * Removes a {@link DMBrowserNodeContentAvailabilityHandler}.
     * 
     * @param contentAvailabilityHandler the {@link DMBrowserNodeContentAvailabilityHandler}
     */
    public void removeContentAvailabilityHandler(
            final DMBrowserNodeContentAvailabilityHandler contentAvailabilityHandler) {
        contentAvailabilityHandlers.remove(contentAvailabilityHandler);
    }

    /**
     * {@link Runnable} realizing the logic for retrieving the content (childs) of a
     * {@link DMBrowserNode}.
     * 
     * @author Christian Weiss
     * 
     */
    private class RetrieverTask implements Runnable {

        private final DMBrowserNode node;

        public RetrieverTask(final DMBrowserNode node) {
            this.node = node;
        }

        @Override
        public void run() {
            // avoid duplicate synchronous retrievals
            synchronized (inProgress) {
                if (inProgress.contains(node)) {
                    return;
                }
                inProgress.add(node);
            }
            try {
                switch (node.getType()) {
                case HistoryRoot:
                    createChildrenForHistoryRootNode(node);
                    break;
                case Workflow:
                    createChildrenForWorkflowNode(node);
                    break;
                case Timeline:
                    createChildrenForTimelineNode(node);
                    break;
                case Components:
                    createChildrenForComponentsNode(node);
                    break;
                case Component:
                    createChildrenForComponentNode(node);
                    break;
                case HistoryObject:
                    createChildrenForHistoryObjectNode(node);
                    break;
                default:
                    log.warn("Unexpected node type: " + node.getType().name());
                }
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                for (final DMBrowserNodeContentAvailabilityHandler handler : contentAvailabilityHandlers) {
                    handler.handleContentAvailable(node);
                }
            } catch (RuntimeException e) {
                for (final DMBrowserNodeContentAvailabilityHandler handler : contentAvailabilityHandlers) {
                    handler.handleContentRetrievalError(node, e);
                }
            } finally {
                synchronized (inProgress) {
                    inProgress.remove(node);
                }
            }
        }

    }

}
