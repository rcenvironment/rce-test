/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.commons.StringUtils;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentUtils;
import de.rcenvironment.rce.component.DistributedComponentRegistry;

/**
 * Writes and reads {@link WorkflowDescription}s to and from {@link java.io.File}s.
 * 
 * @author Doreen Seider
 */
public final class WorkflowDescriptionPersistenceHandler {

    protected static final String INPUT = "input";
    protected static final String TARGET = "target";
    protected static final String OUTPUT = "output";
    protected static final String SOURCE = "source";
    protected static final String CONNECTIONS = "connections";
    protected static final String DEFAULT_CONFIGURATION = "configuration";
    protected static final String CONFIGURATIONS = "configurations";
    protected static final String CURRENT_CONFIGURATION_IDENTIFIER = "currentConfigurationIdentifier";
    protected static final String CONFIGURATION_MAP = "map";
    protected static final String ADD_OUTPUT = "addOutput";
    protected static final String ADD_INPUT = "addInput";
    protected static final String OUTPUT_META_DATA = "outputMetaData";
    protected static final String INPUT_META_DATA = "inputMetaData";
    protected static final String VERSION = "version";
    protected static final String COMPONENT = "component";
    protected static final String LOCATION = "location";
    protected static final String NODES = "nodes";
    protected static final String PLATFORM = "platform";
    protected static final String NAME = "name";
    protected static final String IDENTIFIER = "identifier";
    protected static final String WORKFLOW_VERSION = "workflowVersion";
    protected static final String ADDITIONAL_INFORMATION = "additionalInformation";

    private static DistributedComponentRegistry distrComponentRegistry = ServiceUtils.createNullService(DistributedComponentRegistry.class);

    private static PlatformService platformService = ServiceUtils.createNullService(PlatformService.class);

    public WorkflowDescriptionPersistenceHandler() {}

    protected void bindDistributedComponentRegistry(DistributedComponentRegistry newComponentRegistry) {
        distrComponentRegistry = newComponentRegistry;
    }

    protected void unbindDistributedComponentRegistry(DistributedComponentRegistry oldComponentRegistry) {
        distrComponentRegistry = ServiceUtils.createNullService(DistributedComponentRegistry.class);
    }

    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    protected void unbindPlatformService(PlatformService oldPlatformService) {
        platformService = ServiceUtils.createNullService(PlatformService.class);
    }

    /**
     * Writes the given {@link WorkflowDescription} into an {@link OutputStream}.
     * 
     * @param wd The {@link WorkflowDescription} to write.
     * @return An byte array with the {@link WorkflowDescription}.
     * @throws IOException if writing to {@link java.io.File} failed for some reason.
     */
    public ByteArrayOutputStream writeWorkflowDescriptionToStream(WorkflowDescription wd) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createJsonGenerator(outputStream, JsonEncoding.UTF8);
        g.setPrettyPrinter(new DefaultPrettyPrinter());

        g.writeStartObject();

        g.writeStringField(IDENTIFIER, wd.getIdentifier());
        g.writeStringField(WORKFLOW_VERSION, String.valueOf(wd.getWorkflowVersion()));

        writeOptionalValue(g, NAME, wd.getName());
        if (wd.getTargetPlatform() != null) {
            writeOptionalValue(g, PLATFORM, wd.getTargetPlatform().getNodeId());
        }
        writeOptionalValue(g, ADDITIONAL_INFORMATION, wd.getAdditionalInformation());

        if (wd.getWorkflowNodes().size() > 0) {
            g.writeArrayFieldStart(NODES);
            for (WorkflowNode node : wd.getWorkflowNodes()) {            
                g.writeStartObject();

                g.writeStringField(IDENTIFIER, node.getIdentifier());
                g.writeStringField(NAME, node.getName());
                g.writeStringField(LOCATION, StringUtils.concat(new String[] { String.valueOf(node.getX()), String.valueOf(node.getY()) }));

                ComponentDescription cd = node.getComponentDescription();
                PlatformIdentifier platformIdentifier = cd.getPlatform();
                if (platformIdentifier != null) {
                    g.writeStringField(PLATFORM, platformIdentifier.getNodeId());
                }

                g.writeObjectFieldStart(COMPONENT);
                g.writeStringField(IDENTIFIER, cd.getIdentifier());
                writeOptionalValue(g, VERSION, cd.getVersion());

                g.writeEndObject(); // 'component'

                writeConfiguation(g, node);

                Map<String, Class<? extends Serializable>> addInputs = cd.getDynamicInputDefinitions();
                if (addInputs.size() > 0) {
                    g.writeArrayFieldStart(ADD_INPUT);
                    for (String key : addInputs.keySet()) {
                        g.writeString(StringUtils.concat(new String[] { key, addInputs.get(key).getCanonicalName() }));
                    }
                    g.writeEndArray(); // 'addInputs'
                }

                Map<String, Class<? extends Serializable>> addOutputs = cd.getDynamicOutputDefinitions();
                if (addOutputs.size() > 0) {
                    g.writeArrayFieldStart(ADD_OUTPUT);
                    for (String key : addOutputs.keySet()) {
                        g.writeString(StringUtils.concat(new String[] { key, addOutputs.get(key).getCanonicalName() }));
                    }
                    g.writeEndArray(); // 'addOutputs'
                }

                Set<String> inputNames = new HashSet<String>();
                inputNames.addAll(cd.getInputDefinitions().keySet());
                inputNames.addAll(cd.getDynamicInputDefinitions().keySet());
                boolean objectFieldStartWritten = false;

                for (String inputName : inputNames) {
                    Map<String, Serializable> inputMetaData = cd.getInputMetaData(inputName);
                    if (inputMetaData.size() > 0) {
                        if (!objectFieldStartWritten) {
                            g.writeObjectFieldStart(INPUT_META_DATA);
                            objectFieldStartWritten = true;
                        }
                        g.writeArrayFieldStart(inputName);
                        for (String key : inputMetaData.keySet()) {
                            g.writeString(StringUtils.concat(new String[] { key, inputMetaData.get(key).getClass().getCanonicalName(),
                                inputMetaData.get(key).toString() }));
                        }
                        g.writeEndArray(); // 'inputName'
                    }
                }
                if (objectFieldStartWritten) {
                    g.writeEndObject(); // 'inputMetaData'                    
                }

                Set<String> outputNames = new HashSet<String>();
                outputNames.addAll(cd.getOutputDefinitions().keySet());
                outputNames.addAll(cd.getDynamicOutputDefinitions().keySet());
                objectFieldStartWritten = false;

                for (String outputName : outputNames) {
                    Map<String, Serializable> outputMetaData = cd.getOutputMetaData(outputName);
                    if (outputMetaData.size() > 0) {
                        if (!objectFieldStartWritten) {
                            g.writeObjectFieldStart(OUTPUT_META_DATA);
                            objectFieldStartWritten = true;
                        }
                        g.writeArrayFieldStart(outputName);
                        for (String key : outputMetaData.keySet()) {
                            g.writeString(StringUtils.concat(new String[] { key, outputMetaData.get(key).getClass().getCanonicalName(),
                                outputMetaData.get(key).toString() }));
                        }
                        g.writeEndArray(); // 'outputName'
                    }
                }
                if (objectFieldStartWritten) {
                    g.writeEndObject(); // 'outputMetaData'
                }

                g.writeEndObject();
            }
            g.writeEndArray(); // 'nodes'            
        }

        if (wd.getConnections().size() > 0) {
            g.writeArrayFieldStart(CONNECTIONS);
            for (Connection connection : wd.getConnections()) {
                g.writeStartObject();
                g.writeStringField(SOURCE, connection.getSource().getIdentifier());
                g.writeStringField(OUTPUT, connection.getOutput());
                g.writeStringField(TARGET, connection.getTarget().getIdentifier());
                g.writeStringField(INPUT, connection.getInput());            
                g.writeEndObject();
            }
            g.writeEndArray(); // 'connections'            
        }

        g.writeEndObject();
        g.close();

        return outputStream;
    }

    /**
     * Writes the given {@link WorkflowDescription} into the given {@link OutputStream}.
     * 
     * @param wd The {@link WorkflowDescription} to write.
     * @param output The stream to write to.
     * @throws IOException if writing to {@link java.io.File} failed for some reason.
     */
    public void writeWorkflowDescriptionToStream(WorkflowDescription wd, OutputStream output) throws IOException {
        OutputStream outputStream = writeWorkflowDescriptionToStream(wd);
        output.write(outputStream.toString().getBytes());
    }

    /**
     * Reads workflow version of given persistent workflow description.
     * 
     * @param inputStream given persistent workflow description
     * @return workflow version number (if no version is defined, version 0 is returned)
     * @throws IOException if reading from {@link java.io.File} failed for some reason.
     * @throws ParseException if parsing the {@link java.io.File} failed for some reason.s
     */
    public int readWorkflowVersionNumer(InputStream inputStream) throws ParseException, IOException {

        int workflowVersion = 0;

        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createJsonParser(inputStream);

        jp.nextToken(); // will return JsonToken.START_OBJECT
        jp.nextToken();
        jp.nextToken(); // will return identifier

        // read and parse remaining optional fields
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY

            if (WORKFLOW_VERSION.equals(jp.getCurrentName())) {
                workflowVersion = Integer.valueOf(jp.getText());
                break;
            }
        }

        return workflowVersion;
    }

    /**
     * Reads a {@link WorkflowDescription} from a given {@link java.io.File} and adds the  {@link PlaceHolderDescription} 
     * after that. 
     * @param inputStream The {@link InputStream} to read from.
     * @param user user to use in order to load {@link ComponentDescription}s from the
     *        {@link de.rcenvironment.rce.component.ComponentRegistry}.
     * @return the read {@link WorkflowDescription}.
     * @throws IOException if reading from {@link java.io.File} failed for some reason.
     * @throws ParseException if parsing the {@link java.io.File} failed for some reason.
     */
    public WorkflowDescription readWorkflowDescriptionFromStream(InputStream inputStream, User user) 
        throws IOException, ParseException {
        WorkflowDescription wd = parseWorkflow(inputStream, user);
        return wd;
    }


    /**
     * Reads a {@link WorkflowDescription} from a given {@link java.io.File}.
     * @param inputStream The {@link InputStream} to read from.
     * @param user user to use in order to load {@link ComponentDescription}s from the
     *        {@link de.rcenvironment.rce.component.ComponentRegistry}.
     * @throws IOException 
     * @throws JsonParseException 
     * @throws ParseException 
     * 
     */
    private WorkflowDescription parseWorkflow(InputStream inputStream, User user) throws JsonParseException, IOException, ParseException {
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createJsonParser(inputStream);
        WorkflowDescription wd;

        Map<String, WorkflowNode> nodes = new HashMap<String, WorkflowNode>();

        jp.nextToken(); // will return JsonToken.START_OBJECT
        jp.nextToken();

        // read required field 'identifier'
        if (IDENTIFIER.equals(jp.getCurrentName())) {
            jp.nextToken(); // move to value
            wd = new WorkflowDescription(jp.getText());
            wd.setWorkflowVersion(WorkflowConstants.INITIAL_WORKFLOW_VERSION_NUMBER);
        } else {
            throw new ParseException("No identifier found.", jp.getCurrentLocation().getLineNr());
        }

        // read and parse remaining optional fields
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY

            if (WORKFLOW_VERSION.equals(jp.getCurrentName())) {
                wd.setWorkflowVersion(Integer.valueOf(jp.getText()));
            } else if (NAME.equals(jp.getCurrentName())) {
                wd.setName(jp.getText());
            } else if (ADDITIONAL_INFORMATION.equals(fieldname)) {
                wd.setAdditionalInformation(jp.getText());
            } else if (PLATFORM.equals(fieldname)) {
                wd.setTargetPlatform(PlatformIdentifierFactory.fromNodeId(jp.getText()));
            } else if (NODES.equals(fieldname)) { // contains an array
                nodes = parseNodes(jp, user);
                for (String key : nodes.keySet()) {
                    wd.addWorkflowNode(nodes.get(key));
                }
            } else if (CONNECTIONS.equals(fieldname)) { // contains an array
                List<Connection> connections = parseConnections(jp, nodes);
                for (Connection connection: connections) {
                    wd.addConnection(connection);
                }
            }
        }
        return wd;
    }


    private void writeConfiguation(JsonGenerator g, WorkflowNode node) throws IOException {
        Map<String, Serializable> config = node.getComponentDescription().getConfiguration(ComponentDescription.DEFAULT_CONFIG_ID);
        if (config.size() > 0) {
            g.writeArrayFieldStart(DEFAULT_CONFIGURATION);
            writeConfigurationValues(g, config, node);
            g.writeEndArray(); // 'default configuration'
        }

        if (node.getPropertyMapIds().size() > 1) {
            g.writeArrayFieldStart(CONFIGURATIONS);
            for (String mapId: node.getPropertyMapIds()) {
                if (!mapId.equals(ComponentDescription.DEFAULT_CONFIG_ID)) {
                    g.writeStartObject();
                    g.writeStringField(IDENTIFIER, mapId);
                    config = node.getComponentDescription().getConfiguration(mapId);
                    if (config.size() > 0) {
                        g.writeArrayFieldStart(CONFIGURATION_MAP);
                        writeConfigurationValues(g, config, node);
                        g.writeEndArray(); // 'configuration map'
                    }
                    g.writeEndObject();
                }

            }
            g.writeEndArray(); // 'configurations'
        }

        if (!node.getPropertyMapId().equals(ComponentDescription.DEFAULT_CONFIG_ID)) {
            g.writeStringField(CURRENT_CONFIGURATION_IDENTIFIER, node.getPropertyMapId());                    
        }
    }

    private void writeConfigurationValues(JsonGenerator g, Map<String, Serializable> config, WorkflowNode node) throws IOException {
        for (String key : config.keySet()) {
            final String value;
            final String type;
            if (config.get(key) != null) {
                value = config.get(key).toString();
                type = config.get(key).getClass().getCanonicalName();
            } else {
                if (node.getComponentDescription().getConfigurationDefinitions().get(key) == null) {
                    continue;
                }
                value = "";
                type = node.getComponentDescription().getConfigurationDefinitions().get(key).getCanonicalName();
            }

            g.writeString(StringUtils.concat(new String[] { key, type, value }));
        }
    }

    private void writeOptionalValue(JsonGenerator g, String fieldname, String value) throws IOException {
        if (value != null) {
            g.writeStringField(fieldname, value);
        }
    }

    private String[] parseProperties(JsonParser jp) throws IOException {
        List<String> rawPropList = new ArrayList<String>();
        while (jp.nextToken() != JsonToken.END_ARRAY) {
            rawPropList.add(jp.getText());
        }
        String[] rawPropArray = new String[rawPropList.size()];
        for (int i = 0; i < rawPropList.size(); i++) {
            rawPropArray[i] = rawPropList.get(i);
        }

        return rawPropArray;
    }

    private Map<String, Class<? extends Serializable>> parsePropertiesTypes(JsonParser jp) throws IOException {
        return ComponentUtils.parsePropertyForConfigTypes(parseProperties(jp));
    }

    private Map<String, Serializable> parsePropertiesValues(JsonParser jp) throws IOException {
        String[] rawProperty = parseProperties(jp);
        Map<String, Class<? extends Serializable>> propDef = ComponentUtils.parsePropertyForConfigTypes(rawProperty);
        Map<String, String> rawPropValues = ComponentUtils.parsePropertyForConfigValues(rawProperty);

        return ComponentUtils.convertConfigurationValues(propDef, rawPropValues);

    }

    private Map<String, WorkflowNode> parseNodes(JsonParser jp, User user) throws IOException, ParseException {
        Map<String, WorkflowNode> nodes = new HashMap<String, WorkflowNode>();

        while (jp.nextToken() != JsonToken.END_ARRAY) { // contains an object
            jp.nextToken();
            while (jp.getCurrentToken() != JsonToken.END_OBJECT) {
                // read required fields

                String nodeField = jp.getCurrentName();
                jp.nextToken(); // move to value

                String identifier;
                if (IDENTIFIER.equals(nodeField)) {
                    identifier = jp.getText();
                } else {
                    throw new ParseException("No node identifier found.", jp.getCurrentLocation().getLineNr());
                }

                jp.nextToken();
                nodeField = jp.getCurrentName();
                jp.nextToken(); // move to value

                String name;
                if (NAME.equals(nodeField)) {
                    name = jp.getText();
                } else {
                    throw new ParseException("No node name found.", jp.getCurrentLocation().getLineNr());
                }

                jp.nextToken();
                nodeField = jp.getCurrentName();
                jp.nextToken(); // move to value

                int x;
                int y;
                if (LOCATION.equals(nodeField)) {
                    String[] location = StringUtils.split(jp.getText());
                    try {
                        x = Integer.parseInt(location[0]);
                        y = Integer.parseInt(location[1]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new ParseException("Invalid location definition.", jp.getCurrentLocation().getLineNr());
                    }
                } else {
                    throw new ParseException("No node location found.", jp.getCurrentLocation().getLineNr());
                }  

                jp.nextToken();
                nodeField = jp.getCurrentName();
                jp.nextToken(); // move to value

                PlatformIdentifier pi = null;
                if (PLATFORM.equals(nodeField)) {
                    pi = PlatformIdentifierFactory.fromNodeId(jp.getText());
                    jp.nextToken();
                    nodeField = jp.getCurrentName();
                    jp.nextToken(); // move to START_OBJECT
                }

                ComponentDescription cd;
                if (COMPONENT.equals(nodeField)) { // contains an object
                    cd = parseComponentDescription(jp, pi, user);
                } else {
                    throw new ParseException("No component definition found.", jp.getCurrentLocation().getLineNr());
                }

                WorkflowNode node = new WorkflowNode(cd);
                node.setIdentifier(identifier);
                node.setName(name);
                node.setLocation(x, y);

                // read remaining optional fields
                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    nodeField = jp.getCurrentName();
                    jp.nextToken(); // move to value or start object
                    if (DEFAULT_CONFIGURATION.equals(nodeField)) { // contains an array
                        Map<String, Serializable> config = parsePropertiesValues(jp);
                        for (String key : config.keySet()) {
                            node.setProperty(key, config.get(key));
                        }
                    }
                    if (CONFIGURATIONS.equals(nodeField)) { // contains an array
                        while (jp.nextToken() != JsonToken.END_ARRAY) {
                            jp.nextToken(); // move to node field name 'identifier'
                            jp.nextToken(); // move to value
                            String mapId = jp.getText();
                            jp.nextToken(); // move to node field name 'map'
                            jp.nextToken(); // move to value (array)
                            Map<String, Serializable> config = parsePropertiesValues(jp);
                            node.getComponentDescription().addConfiguration(mapId, config);
                            jp.nextToken(); // move to END_OBJECT
                        }
                    }
                    if (CURRENT_CONFIGURATION_IDENTIFIER.equals(nodeField)) { // contains an string field
                        node.setPropertyMapId(jp.getText());
                    }
                    if (ADD_INPUT.equals(nodeField)) {
                        Map<String, Class<? extends Serializable>> inputsDef = parsePropertiesTypes(jp);
                        for (String def : inputsDef.keySet()) {
                            node.addInput(def, inputsDef.get(def).getCanonicalName());
                        }
                    } else if (ADD_OUTPUT.equals(nodeField)) {
                        Map<String, Class<? extends Serializable>> outputsDef = parsePropertiesTypes(jp);
                        for (String def : outputsDef.keySet()) {
                            node.addOutput(def, outputsDef.get(def).getCanonicalName());
                        }
                    } else if (INPUT_META_DATA.equals(nodeField)) {
                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                            String inputName = jp.getCurrentName();
                            jp.nextToken(); //move to value
                            Map<String, Serializable> inputMetaData = parsePropertiesValues(jp);
                            for (String key : inputMetaData.keySet()) {
                                node.setInputMetaData(inputName, key, inputMetaData.get(key));                           
                            }
                        }
                    } else if (OUTPUT_META_DATA.equals(nodeField)) {
                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                            String outputName = jp.getCurrentName();
                            jp.nextToken(); // move to value
                            Map<String, Serializable> outputMetaData = parsePropertiesValues(jp);
                            for (String key : outputMetaData.keySet()) {
                                node.setOutputMetaData(outputName, key, outputMetaData.get(key));                           
                            }
                        }
                    }
                }
                nodes.put(identifier, node);
            }
        }
        return nodes;
    }

    private ComponentDescription parseComponentDescription(JsonParser jp, PlatformIdentifier pi, User user)
        throws IOException, ParseException {

        String cIdentifier;
        jp.nextToken();
        String cField = jp.getCurrentName();
        jp.nextToken(); // move to value
        if (IDENTIFIER.equals(cField)) {
            cIdentifier = jp.getText();
        } else {
            throw new ParseException("No component identifier found.", jp.getCurrentLocation().getLineNr());
        }

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            cField = jp.getCurrentName();
            jp.nextToken(); // move to value
            if (VERSION.equals(cField)) {
                // nothing to do here for now because version aren't recognized
                jp.getText();
            }
        }

        ComponentDescription cd = getComponentDecription(cIdentifier, pi, user);
        if (cd == null) {
            throw new ParseException("No component with this definition registered: " + cIdentifier,
                jp.getCurrentLocation().getLineNr());
        }
        return cd;
    }

    private ComponentDescription getComponentDecription(String identifier, PlatformIdentifier pi, User user) {
        List<ComponentDescription> knownCds = distrComponentRegistry.getAllComponentDescriptions(user, false);
        List<ComponentDescription> matchingCds = new ArrayList<ComponentDescription>();
        ComponentDescription resultCd = null;

        // get all matching components
        for (ComponentDescription cd : knownCds) {
            if (cd.getIdentifier().equals(identifier)) {
                matchingCds.add(cd);
            }
        }
        if (matchingCds.isEmpty()) {
            String name = identifier.substring(identifier.indexOf(ComponentConstants.COMPONENT_ID_SEPARATOR) + 1, identifier.length());
            resultCd = ComponentUtils.getPlaceholderComponentDescription(name);
        } else {
            // check if one is installed on desired platform
            for (ComponentDescription cd : matchingCds) {
                if (cd.getIdentifier().equals(identifier) && cd.getPlatform() != null && cd.getPlatform().equals(pi)) {
                    resultCd = cd;
                    break;
                }
            }
            // check if one is installed locally
            if (resultCd == null) {
                for (ComponentDescription cd : matchingCds) {
                    if (cd.getIdentifier().equals(identifier) && cd.getPlatform() != null
                        && cd.getPlatform().equals(platformService.getPlatformIdentifier())) {
                        resultCd = cd;
                        break;
                    }
                }
            }
            // take any component
            if (resultCd == null) {
                resultCd = matchingCds.get(0);
            }
        }
        // return a copy of the component instance to facilitate independent changes
        // FIXME: needs null check as JUnit test might fail otherwise
        final ComponentDescription resultCdClone = resultCd.clone();
        if (resultCdClone != null) {
            resultCd = resultCdClone;
        }
        resultCd.setPlatform(pi);
        return resultCd;

    }

    private List<Connection> parseConnections(JsonParser jp, Map<String, WorkflowNode> nodes)
        throws IOException, ParseException {
        List<Connection> connections = new ArrayList<Connection>();

        while (jp.nextToken() != JsonToken.END_ARRAY) {
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                WorkflowNode source = null;
                String output = null;
                WorkflowNode target = null;
                String input = null;
                String connectionField = jp.getCurrentName();
                jp.nextToken(); // move to value
                if (SOURCE.equals(connectionField)) {
                    source = nodes.get(jp.getText());
                } else {
                    throw new ParseException("No source definition.", jp.getCurrentLocation().getLineNr());
                }
                jp.nextToken();
                connectionField = jp.getCurrentName();
                jp.nextToken(); // move to value
                if (OUTPUT.equals(connectionField)) {
                    output = jp.getText();
                } else {
                    throw new ParseException("No output definition.", jp.getCurrentLocation().getLineNr());
                }
                jp.nextToken();
                connectionField = jp.getCurrentName();
                jp.nextToken(); // move to value
                if (TARGET.equals(connectionField)) {
                    target = nodes.get(jp.getText());
                } else {
                    throw new ParseException("No target definition.", jp.getCurrentLocation().getLineNr());
                }
                jp.nextToken();
                connectionField = jp.getCurrentName();
                jp.nextToken(); // move to value
                if (INPUT.equals(connectionField)) {
                    input = jp.getText();
                } else {
                    throw new ParseException("No input definition.", jp.getCurrentLocation().getLineNr());
                }

                if (source != null && output != null && target != null && input != null) {
                    connections.add(new Connection(source, output, target, input));
                } else {
                    throw new ParseException("Invalid connection definition.", jp.getCurrentLocation().getLineNr());
                }
            }
        }
        return connections;
    }
}
