/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.execute;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;


/**
 * Utils class for sorting the placeholder in the GUI.
 *
 * @author Sascha Zur
 */
public final class PlaceholderSortUtils {
    
    private PlaceholderSortUtils(){
        
    }
    /**
     * This method sorts the given list of component identifier based on their component names.
     * 
     * @param instancesWithPlaceholder : The list to sort
     * @param workflowDescription : the wd with the components, used for getting the names
     * @return Sorted List.
     */
    public static List<String> sortInstancesWithPlaceholderByName(
        List<String> instancesWithPlaceholder, WorkflowDescription workflowDescription) {
        List<String> sortedList = new LinkedList<String>();
        for (String identifier : instancesWithPlaceholder){
            String componentName = workflowDescription.getWorkflowNode(identifier).getName();
            int i = 0;
            while (i < sortedList.size() 
                && workflowDescription.getWorkflowNode(sortedList.get(i)).getName().compareToIgnoreCase(componentName) < 0){
                i++;
            }
            sortedList.add(i, identifier);
        }
        return sortedList;
    }
    
    /**
     * Sorts the global placeholder for the workflowPage and the ClearHistoryDialog.
     * @param placeholderNameKeysOfComponentID :Set of names
     * @param placeholderAttributes : Attributes with priority
     * @return sorted List
     */
    public static List<String> getPlaceholderOrder(Set<String> placeholderNameKeysOfComponentID, 
        Map<String, Map<String, String>> placeholderAttributes) {
        List <String> result = new LinkedList<String>();
        if (placeholderNameKeysOfComponentID != null && placeholderAttributes != null && !placeholderAttributes.isEmpty()){
            for (String componentPlaceholder : placeholderNameKeysOfComponentID){
                Map<String, String> attributes = placeholderAttributes.get(componentPlaceholder);
                String prio = null;
                if (attributes != null){
                    prio = placeholderAttributes.get(componentPlaceholder).get(ComponentConstants.PLACEHOLDER_ATTRIBUTE_PRIORITY);
                }
                if (prio == null || prio.equals("")){
                    result.add(componentPlaceholder);
                } else {
                    int resultIndex = 0;
                    for (int i = 0; i < result.size(); i++){
                        String currentPrio = placeholderAttributes.get(result.get(i)).
                            get(ComponentConstants.PLACEHOLDER_ATTRIBUTE_PRIORITY);
                        if (currentPrio == null || currentPrio.equals("")){
                            resultIndex = i;
                            break;
                        } else {
                            if (Integer.parseInt(currentPrio) > Integer.parseInt(prio)) {
                                resultIndex = i;
                                break;
                            } else if (Integer.parseInt(currentPrio) == Integer.parseInt(prio)){
                                String nameNewPH = placeholderAttributes.get(componentPlaceholder).get(
                                    ComponentConstants.PLACEHOLDER_ATTRIBUTE_GUINAME);
                                String nameOldPH = placeholderAttributes.get(result.get(i)).get(
                                    ComponentConstants.PLACEHOLDER_ATTRIBUTE_GUINAME);
                                if (nameNewPH != null && nameOldPH != null && nameNewPH.compareToIgnoreCase(nameOldPH) < 0){

                                    resultIndex = i;
                                    break;

                                }
                            }
                        }
                    }
                    result.add(resultIndex, componentPlaceholder);
                }
            }
        } else {
            if (placeholderNameKeysOfComponentID != null) {
                result = new LinkedList<String>(placeholderNameKeysOfComponentID);
            } else {
                return null;
            }
        }
        return result;
    }
}
