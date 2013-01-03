/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import de.rcenvironment.rce.component.ComponentUtils;
import de.rcenvironment.rce.configuration.PersistentSettingsService;

/**
 *
 * Class for placeholder management and parsing. Here, all information about the placeholders are stored.
 *  
 * @author Sascha Zur
 */
public class WorkflowExecutionPlaceholderHelper implements Serializable{

    private static final long serialVersionUID = 1L;

    private static final String PLACEHOLDERCOMPONENT_HISTORYFILE = "placeholderComponentHistory.json";
    private static final String PLACEHOLDERINSTANCE_HISTORYFILE = "placeholderInstanceHistory.json";

    private static PersistentSettingsService persistentSettingsService;
    private Map<String, List<String>> componentTypeHistory;
    private Map<String, List<String>> componentInstanceHistory;

    private Map<String, Map<String, Serializable>> componentInstancePlaceholders;
    private Map<String, Map<String, Serializable>> componentTypePlaceholders;

    // history of a placeholder is saved in componentID.phname -> list of history way with the last entry being the last used 

    private Map<String, List<String>> componentInstancesOfType;
    private List<String> encryptedPlaceholder;
    private final String dot = ".";

    @Deprecated 
    /**
     * Because of OSGi.
     */
    public WorkflowExecutionPlaceholderHelper(){

    }

    /**
     * Creates a {@link WorkflowExecutionPlaceholderHelper} from the given {@link WorkflowDescription}.
     * @param wd : {@link WorkflowDescription} as source
     * @return new {@link WorkflowExecutionPlaceholderHelper}
     */
    public static WorkflowExecutionPlaceholderHelper createPlaceholderDescriptionFromWorkflowDescription(WorkflowDescription wd) {

        WorkflowExecutionPlaceholderHelper weph = new WorkflowExecutionPlaceholderHelper();
        weph.setComponentInstancePlaceholders(new HashMap<String, Map<String, Serializable>>());
        weph.setComponentTypePlaceholders(new HashMap<String, Map<String, Serializable>>());
        weph.setComponentInstancesOfType(new HashMap<String, List<String>>());
        weph.setComponentTypeHistory(new HashMap<String, List<String>>());
        weph.setComponentInstanceHistory(new HashMap<String, List<String>>());
        weph.setEncryptedPlaceholder(new LinkedList<String>());
        for (WorkflowNode node : wd.getWorkflowNodes()){
            for (String key : node.getComponentDescription().getConfigurationDefinitions().keySet()){
                if (key != null && node.getComponentDescription().getConfiguration().get(key) != null){
                    String possiblePlaceholder = node.getComponentDescription().getConfiguration().get(key).toString();
                    if (ComponentUtils.isPlaceholder(possiblePlaceholder)){
                        weph.addPlaceholder(possiblePlaceholder, node.getComponentDescription().getIdentifier(), node.getIdentifier());
                    }
                }
            }
        }
        weph.setComponentInstanceHistory(new HashMap<String, List<String>>());
        weph.setComponentTypeHistory(new HashMap<String, List<String>>());
        weph.setComponentInstanceHistory(persistentSettingsService.readMapWithStringList(PLACEHOLDERINSTANCE_HISTORYFILE)); //  load
        weph.setComponentTypeHistory(persistentSettingsService.readMapWithStringList(PLACEHOLDERCOMPONENT_HISTORYFILE)); // load
        return weph;
    }

    /**
     * Automatically add the given placeholder to the right map and adds an instance to the componentype if necessary.
     * 
     * @param placeholder : the placeholder
     * @param componentID : the component type id
     * @param componentUUID : the instance ID of the component
     */
    public void addPlaceholder(String placeholder, String componentID, String componentUUID){
        Matcher matcher = ComponentUtils.getMatcherForPlaceholder(placeholder);
        if (matcher.matches()){
            // Add component instance to list for component group
            List<String> componentInstanceList = componentInstancesOfType.get(componentID);
            if (componentInstanceList != null){
                if (!componentInstanceList.contains(componentUUID)){
                    componentInstanceList.add(componentUUID);
                }
            } else {
                componentInstanceList = new LinkedList<String>();
                componentInstanceList.add(componentUUID);
                componentInstancesOfType.put(componentID, componentInstanceList);
            }
            // Add placeholder key to right map.
            if (isGlobalPlaceholder(matcher)){
                addPlaceholderKeyToMap(componentTypePlaceholders, componentID, matcher.group(ComponentUtils.PLACEHOLDERNAME));
            } else {
                addPlaceholderKeyToMap(componentInstancePlaceholders, componentUUID, matcher.group(ComponentUtils.PLACEHOLDERNAME));
            }
            if ((matcher.group(ComponentUtils.ATTRIBUTE1) !=  null 
                && matcher.group(ComponentUtils.ATTRIBUTE1).equals(ComponentUtils.ENCODEDATTRIBUTE))
                || (matcher.group(ComponentUtils.ATTRIBUTE2) !=  null 
                && matcher.group(ComponentUtils.ATTRIBUTE2).equals(ComponentUtils.ENCODEDATTRIBUTE))){
                encryptedPlaceholder.add(componentID + dot + matcher.group(ComponentUtils.PLACEHOLDERNAME));
            }
        }

    }


    private boolean isGlobalPlaceholder(Matcher matcherOfPlaceholder){
        return (matcherOfPlaceholder.group(ComponentUtils.ATTRIBUTE1) != null 
            && (matcherOfPlaceholder.group(ComponentUtils.ATTRIBUTE1).equals(ComponentUtils.GLOBALATTRIBUTE) 
                ||  (matcherOfPlaceholder.group(ComponentUtils.ATTRIBUTE2) != null 
                && matcherOfPlaceholder.group(ComponentUtils.ATTRIBUTE2).equals(ComponentUtils.GLOBALATTRIBUTE))));
    }

    private void addPlaceholderKeyToMap(Map<String, Map<String, Serializable>> map, String key, String placeholderName){
        Map<String, Serializable> placeholderMap = map.get(key);
        if (placeholderMap == null){
            placeholderMap = new HashMap<String, Serializable>();
            map.put(key, placeholderMap);
        }
        if (!placeholderMap.containsKey(placeholderName)){
            placeholderMap.put(placeholderName, null);
        }
    }


    /**
     * Adds a value to the given placeholder in the right map.
     * 
     * @param placeholder : the placeholder with the new value.
     * @param componentID : the component type id
     * @param componentUUID : if placeholder not global, UUID is necessary
     * @param value : the new value
     * @param wfID : ID
     * @param addToHistory : True, if it should be added to history 
     */
    public void setPlaceholderValue(String placeholder, String componentID, String componentUUID, Serializable value, String wfID, 
        boolean addToHistory){
        if (placeholder.matches(ComponentUtils.PLACEHOLDER_REGEX)){
            Matcher matcher = ComponentUtils.getMatcherForPlaceholder(placeholder);
            if (isGlobalPlaceholder(matcher)){
                if (componentTypePlaceholders.get(componentID) != null){
                    componentTypePlaceholders.get(componentID).put(matcher.group(ComponentUtils.PLACEHOLDERNAME), value);
                }
            } else {
                if (componentInstancePlaceholders.get(componentUUID) != null){
                    componentInstancePlaceholders.get(componentUUID).put(matcher.group(ComponentUtils.PLACEHOLDERNAME), value);
                }
            }
            if (addToHistory){ 
                String tail = dot + matcher.group(ComponentUtils.PLACEHOLDERNAME);
                String placeholderCompHistory = wfID + dot + componentID + tail;
                List<String> placeholderCompHistoryList = null;
                if (componentTypeHistory != null && componentTypeHistory.get(placeholderCompHistory.toString()) != null){
                    placeholderCompHistoryList = componentTypeHistory.get(placeholderCompHistory.toString());
                    if (placeholderCompHistoryList.contains(value)){
                        placeholderCompHistoryList.remove(value);
                    } 
                } else {
                    placeholderCompHistoryList = new LinkedList<String>();
                }
                placeholderCompHistoryList.add(value.toString());
                if (componentTypeHistory != null){
                    componentTypeHistory.put(placeholderCompHistory, placeholderCompHistoryList);
                }
                String placeholderInstanceHistory = componentUUID + tail;
                List<String> placeholderHistoryList = null;
                if (componentInstanceHistory != null && componentInstanceHistory.get(placeholderInstanceHistory) != null){
                    placeholderHistoryList = componentInstanceHistory.get(placeholderInstanceHistory);
                    if (placeholderHistoryList.contains(value)){
                        placeholderHistoryList.remove(value);
                    } 

                } else {
                    placeholderHistoryList  = new LinkedList<String>();
                }
                placeholderHistoryList.add(value.toString());
                if (componentInstanceHistory != null){
                    componentInstanceHistory.put(placeholderInstanceHistory, placeholderHistoryList);
                }
            }
        }

    }

    /**
     * Adds a value to the given placeholder in the right map. Use only if you are sure the placeholder is global.
     * 
     * @param placeholder : the placeholder with the new value.
     * @param componentID : the component type id
     * @param value : the new value
     * @param wfID : ID
     * @param addToHistory : True, if it should be added to history 
     */
    public void setGlobalPlaceholderValue(String placeholder, String componentID, Serializable value, String wfID, boolean addToHistory){
        if (placeholder.matches(ComponentUtils.PLACEHOLDER_REGEX)){
            Matcher matcher = ComponentUtils.getMatcherForPlaceholder(placeholder);
            if (isGlobalPlaceholder(matcher)){
                if (componentTypePlaceholders.get(componentID) != null){
                    componentTypePlaceholders.get(componentID).put(matcher.group(ComponentUtils.PLACEHOLDERNAME), value);
                }
            }
            if (addToHistory){ 
                String placeholderCompHistory = wfID + dot + componentID + dot + matcher.group(ComponentUtils.PLACEHOLDERNAME);
                List<String> placeholderCompHistoryList = null;
                if (componentTypeHistory != null && componentTypeHistory.get(placeholderCompHistory) != null){
                    placeholderCompHistoryList = componentTypeHistory.get(placeholderCompHistory);
                    if (placeholderCompHistoryList.contains(value)){
                        placeholderCompHistoryList.remove(value);
                    } 
                } else {
                    placeholderCompHistoryList = new LinkedList<String>();
                }
                placeholderCompHistoryList.add(value.toString());
                if (componentTypeHistory != null){
                    componentTypeHistory.put(placeholderCompHistory, placeholderCompHistoryList);
                }
            }

        }
    }
    /**
     * 
     * Returns the value for the given placeholder. If it is not found, the value is null.
     * 
     * @param placeholderName : the placeholder to look for.
     * @param componentUUID : The instance id from the component if it is not a global placeholder.
     * @return the value of the placeholder, if found, else null
     */
    public Serializable getValueByPlaceholderName(String placeholderName, String componentUUID){
        Serializable result = null;
        if (componentInstancePlaceholders.containsKey(componentUUID)){
            result = componentInstancePlaceholders.get(componentUUID).get(placeholderName);
        }
        return result;
    }
    /**
     * 
     * Returns the value for the given placeholder. If it is not found, the value is null.
     * Use this method if you are sure the placeholder is global.
     * 
     * 
     * @param placeholderName : the placeholder to look for.
     * @param componentId : id of the component type to look for.
     * @return the value of the placeholder, if found, else null
     */
    public Serializable getGlobalValueByPlaceholderName(String placeholderName, String componentId){
        Serializable result = null;
        if (componentTypePlaceholders.get(componentId) != null){
            result = componentTypePlaceholders.get(componentId).get(placeholderName);
        }
        return result;
    }

    /**
     * 
     * Returns the value for the given placeholder. If it is not found, the value is null.
     * 
     * @param placeholder : the placeholder to look for.
     * @param componentID : the component type id
     * @param componentUUID : The instance id from the component if it is not a global placeholder.
     * @return the value of the placeholder, if found, else null
     */
    public Serializable getValueByPlaceholder(String placeholder, String componentID, String componentUUID){
        Serializable result = null;

        if (placeholder.matches(ComponentUtils.PLACEHOLDER_REGEX)){
            Matcher matcher = ComponentUtils.getMatcherForPlaceholder(placeholder);
            if (isGlobalPlaceholder(matcher)){
                if (componentTypePlaceholders.get(componentID) != null){
                    result = componentTypePlaceholders.get(componentID).get(matcher.group(ComponentUtils.PLACEHOLDERNAME));
                }
            } else {
                if (componentInstancePlaceholders.get(componentUUID) != null){
                    result = componentInstancePlaceholders.get(componentUUID).get(matcher.group(ComponentUtils.PLACEHOLDERNAME));
                }
            }
        }
        return result;
    }
    /**
     * 
     * Returns the value for the given placeholder. If it is not found, the value is null.
     * Use this method if you are sure the placeholder is global.
     * 
     * 
     * @param placeholder : the placeholder to look for.
     * @param componentID : the component type id
     * @return the value of the placeholder, if found, else null
     */
    public Serializable getValueByPlaceholder(String placeholder, String componentID){
        Serializable result = null;
        if (placeholder.matches(ComponentUtils.PLACEHOLDER_REGEX)){
            Matcher matcher = ComponentUtils.getMatcherForPlaceholder(placeholder);
            if (isGlobalPlaceholder(matcher)){
                if (componentTypePlaceholders.get(componentID) != null){
                    result = componentTypePlaceholders.get(componentID).get(matcher.group(ComponentUtils.PLACEHOLDERNAME));
                }
            }
        }
        return result;
    }

    /**
     * Returns all instances with placeholder of one component type.
     * @param componentID : type of the component
     * @return a list of all registered component instances, null if there are none
     */
    public List<String> getComponentInstances(String componentID){
        if (componentInstancesOfType != null){
            return componentInstancesOfType.get(componentID);
        }
        return null;
    }
    /**
     * Returns a keyset with all placeholdernames of one registered instance. 
     * @param componentUUID : the instance id
     * @return set of placeholders
     */
    public Set<String> getPlaceholderNameSetOfComponentInstance(String componentUUID){
        if (componentInstancePlaceholders != null){
            return componentInstancePlaceholders.get(componentUUID).keySet();
        } 
        return null;
    }

    /**
     * Getter.
     * @param componentID :     
     * @return :
     */
    public Map <String, Serializable> getPlaceholderOfComponentType(String componentID){
        return componentTypePlaceholders.get(componentID);
    }

    /**
     * Getter.
     * @param componentUUID :     
     * @return :
     */
    public Map <String, Serializable> getPlaceholderOfComponentInstance(String componentUUID){
        return componentInstancePlaceholders.get(componentUUID);
    }
    /**
     * Returns a keyset with all global placeholdernames of one component type. 
     * @param componentID : the compoent id
     * @return set of placeholders
     */
    public Set<String> getPlaceholderNameSetOfComponentID(String componentID){
        if (componentTypePlaceholders.get(componentID) != null){
            return componentTypePlaceholders.get(componentID).keySet();
        } 
        return null;
    }
    /**
     * Returns a set with all placeholder of one registered instance. 
     * @param componentUUID : the instance id
     * @return set of placeholders
     */
    public Set<String> getPlaceholderNamesOfComponentInstance(String componentUUID){
        Set <String> result = new HashSet<String>();
        if (componentInstancePlaceholders != null){
            String compKey = "";
            for (String compKeyIt : componentInstancesOfType.keySet()){
                List<String> instances = componentInstancesOfType.get(compKeyIt);
                if (instances.contains(componentUUID)){
                    compKey = compKeyIt;
                }
            }
            for (String placeholderName : componentInstancePlaceholders.get(componentUUID).keySet()){
                final String closeBracket = "}";
                if (isEncryptedPlaceholder(compKey + dot + placeholderName)){
                    result.add("${*." + placeholderName + closeBracket);
                } else {
                    result.add("${" + placeholderName + closeBracket);
                }
            }
        } 
        return result;
    }
    /**
     * Returns a set with all placeholder of one component type. 
     * @param componentID : the component type id
     * @return set of placeholders
     */
    public Set<String> getPlaceholderOfComponent(String componentID){
        Set <String> result = new HashSet<String>();
        if (componentTypePlaceholders != null){
            for (String placeholderName : componentTypePlaceholders.get(componentID).keySet()){
                final String closeBracket = "}";
                if (isEncryptedPlaceholder(componentID + dot + placeholderName)){
                    result.add("${global.*." +  placeholderName + closeBracket);
                } else {
                    result.add("${global." + placeholderName + closeBracket);

                }
            }
        } 
        return result;
    }


    /**
     * Returns all types of components with placeholders.
     * @return Set of component IDs
     */
    public Set<String> getIdentifiersOfPlaceholderContainingComponents(){
        if (componentInstancesOfType != null){
            return componentInstancesOfType.keySet();
        }
        return null;
    }
    /**
     * Checks whether the given placeholder is encrypted.
     * @param placeholder : to check.
     * @return true if it is
     */
    public boolean isEncryptedPlaceholder(String placeholder) {
        return encryptedPlaceholder.contains(placeholder);
    }



    /**
     * Returns the regex name value.
     * 
     * @param fullPlaceholder : to check
     * @return the name
     */
    public static String getNameOfPlaceholder(String fullPlaceholder) {
        return ComponentUtils.getMatcherForPlaceholder(fullPlaceholder).group(ComponentUtils.PLACEHOLDERNAME);
    }

    protected void bindPersistentSettingsService(PersistentSettingsService newPersistentSettingsService) {
        persistentSettingsService = newPersistentSettingsService;
    }

    /**
     * Loads the history for the given placeholder.
     * 
     * @param placeholder : to look for
     * @param componentUUID : the UUID
     * @return list of history
     */
    public String[] getInstancePlaceholderHistory(String placeholder, String componentUUID) {
        String[] result = new String[0];
        if (componentInstanceHistory.containsKey(componentUUID + dot + placeholder)){
            List<String> history = componentInstanceHistory.get(componentUUID + dot + placeholder);
            result = history.toArray(result);
        }

        return result;
    }

    /**
     * Loads the history for the given placeholder.
     * 
     * @param placeholder : to look for
     * @param componentID : the ID
     * @param wfID : ID of the current workflow
     * @return list of history
     */
    public String[] getComponentPlaceholderHistory(String placeholder, String componentID, String wfID) {
        String[] result = new String[0];
        if (componentTypeHistory.containsKey(wfID + dot + componentID + dot + placeholder)){
            List<String> history = componentTypeHistory.get(wfID + dot + componentID + dot + placeholder);
            result = history.toArray(result);
        }
        return result;
    }
    /**
     * Saves the new placeholders history.
     */
    public void saveHistory() {
        persistentSettingsService.saveMapWithStringList(componentTypeHistory, PLACEHOLDERCOMPONENT_HISTORYFILE);
        persistentSettingsService.saveMapWithStringList(componentInstanceHistory, PLACEHOLDERINSTANCE_HISTORYFILE);
    }
    /**
     * Deletes the history of the given placeholder.
     * @param componentDescriptionIdentifier : the component identifier of the placeholder
     * @param placeholderName : name of the placeholder to delete history from
     */
    public void deletePlaceholderHistory(String componentDescriptionIdentifier, String placeholderName) {
        Set<String> setToDelete = new HashSet<String>();
        for (String key : componentInstanceHistory.keySet()){
            if (key.endsWith(placeholderName)){
                setToDelete.add(key);
            }
        }
        for (String key : setToDelete){
            componentInstanceHistory.remove(key);
        }
        setToDelete = new HashSet<String>();
        for (String key : componentTypeHistory.keySet()){
            if (key.endsWith(componentDescriptionIdentifier + dot + placeholderName)){
                setToDelete.add(key);
            }
        }
        for (String key : setToDelete){

            componentTypeHistory.remove(key);
        }
        saveHistory();
    }


    /**
     * Returns a list with all proposals the given placeholder has in its history.
     * 
     * @param placeholderName : the placeholder to look for.
     * @return proposals of this placeholder
     */
    public String[] getOtherPlaceholderHistoryValues(String placeholderName) {
        List<String> proposals = new LinkedList<String>();
        for (String key : componentTypeHistory.keySet()){
            if (key.endsWith(placeholderName)){
                for (String newValue : componentTypeHistory.get(key)){
                    if (!proposals.contains(newValue)){
                        proposals.add(newValue);
                    } 
                }
            }
        }
        return proposals.toArray(new String[proposals.size()]);
    }

    /**
     * Searches for another value for the given placeholder in same workflow file.
     * @param placeholderName : placeholder to look for
     * @param identifier : workflow identifier
     * @return a value, if exists, else null
     */
    public String getValueFromOtherComponentInWorkflow(String placeholderName, String identifier) {
        String resultValue = null;
        for (String key : componentTypeHistory.keySet()){
            if (key.endsWith(placeholderName) && key.contains(identifier)){
                List<String> allValues = componentTypeHistory.get(key);
                resultValue = allValues.get(allValues.size() - 1);
            }
        }
        return resultValue;
    }

    protected static void setPersistentSettingsService(PersistentSettingsService incPersistentSettingsService) {
        persistentSettingsService = incPersistentSettingsService;
    }


    public Map<String, List<String>> getComponentTypeHistory() {
        return componentTypeHistory;
    }


    public void setComponentTypeHistory(Map<String, List<String>> componentTypeHistory) {
        this.componentTypeHistory = componentTypeHistory;
    }


    public Map<String, List<String>> getComponentInstanceHistory() {
        return componentInstanceHistory;
    }


    public void setComponentInstanceHistory(Map<String, List<String>> componentInstanceHistory) {
        this.componentInstanceHistory = componentInstanceHistory;
    }


    public Map<String, Map<String, Serializable>> getComponentInstancePlaceholders() {
        return componentInstancePlaceholders;
    }


    public void setComponentInstancePlaceholders(Map<String, Map<String, Serializable>> componentInstancePlaceholders) {
        this.componentInstancePlaceholders = componentInstancePlaceholders;
    }


    public Map<String, Map<String, Serializable>> getComponentTypePlaceholders() {
        return componentTypePlaceholders;
    }


    public void setComponentTypePlaceholders(Map<String, Map<String, Serializable>> componentTypePlaceholders) {
        this.componentTypePlaceholders = componentTypePlaceholders;
    }


    public Map<String, List<String>> getComponentInstancesOfType() {
        return componentInstancesOfType;
    }


    public void setComponentInstancesOfType(Map<String, List<String>> componentInstancesOfType) {
        this.componentInstancesOfType = componentInstancesOfType;
    }

    public List<String> getEncryptedPlaceholder() {
        return encryptedPlaceholder;
    }

    public void setEncryptedPlaceholder(List<String> encryptedPlaceholder) {
        this.encryptedPlaceholder = encryptedPlaceholder;
    }

}
