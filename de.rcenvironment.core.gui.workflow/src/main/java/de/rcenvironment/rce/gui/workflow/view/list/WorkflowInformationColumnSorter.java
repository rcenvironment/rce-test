/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.view.list;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import de.rcenvironment.rce.component.workflow.WorkflowInformation;
import de.rcenvironment.rce.component.workflow.WorkflowState;

/**
 * Provides functionality of sorting a table column. 
 * You can choose which column to sort on and in which direction. 
 * 
 * @author Doreen Seider
 */
public class WorkflowInformationColumnSorter extends ViewerSorter {

    private static final int FIRST_IS_EQUAL = 0;

    private static final int FIRST_IS_GREATER = 1;

    private static final int FIRST_IS_LESS = -1;

    private static final int SORT_ASCENDING = 1;

    private static final int SORT_DESCENDING = 2;

    private int direction;

    private int columnToSort;

    /**
     * Sets the default sorting column and direction.
     */
    public WorkflowInformationColumnSorter() {
        columnToSort = 4;
        direction = SORT_DESCENDING;
    }

    /**
     * Lets set another column than default one to sort on.
     * 
     * @param column The index (beginning with 0) of column to sort.
     */
    public void setColumn(int column) {

        if (column == columnToSort) {
            // same column as last sort: toggle direction
            
            if (SORT_ASCENDING == direction) {
                direction = SORT_DESCENDING;
            } else {
                direction = SORT_ASCENDING;
            }
        } else {
            // new column to sort
            columnToSort = column;
            direction = SORT_ASCENDING;
        }
    }

    @Override
    public int compare(Viewer viewer, Object object1, Object object2) {

        int returnValue = FIRST_IS_EQUAL;

        if (object1 instanceof WorkflowInformation && object2 instanceof WorkflowInformation) {
            WorkflowInformation p1 = (WorkflowInformation) object1;
            WorkflowInformation p2 = (WorkflowInformation) object2;

            switch (columnToSort) {

            // name column
            case 0:
                returnValue = p1.getName().compareTo(p2.getName());
                break;

            // status column
            case 1:
                WorkflowState state1 = WorkflowStateModel.getInstance().getState(((WorkflowInformation) p1).getIdentifier());
                WorkflowState state2 = WorkflowStateModel.getInstance().getState(((WorkflowInformation) p2).getIdentifier());
                returnValue = state1.toString().compareTo(state2.toString());
                break;

            // platform column
            case 2:
                if (p1.getWorkflowDescription().getTargetPlatform() == null) {
                    returnValue = FIRST_IS_GREATER;
                } else if (p2.getWorkflowDescription().getTargetPlatform() == null) {
                    returnValue = FIRST_IS_LESS;
                } else {
                    returnValue = p1.getWorkflowDescription().getTargetPlatform().toString()
                        .compareTo(p2.getWorkflowDescription().getTargetPlatform().toString());
                }
                break;
                
            // user column
            case 3:
                returnValue = p1.getUser().compareTo(p2.getUser());
                break;

            // time column
            case 4:
                if (p1.getInstantiationTime().getTime() == p2.getInstantiationTime().getTime()) {
                    returnValue = FIRST_IS_EQUAL;
                } else if (p1.getInstantiationTime().getTime() > p2.getInstantiationTime().getTime()) {
                    returnValue = FIRST_IS_GREATER;
                } else {
                    returnValue = FIRST_IS_LESS;
                }
                break;

             // add information column
            case 5:
                returnValue = p1.getAdditionalInformation().compareTo(p2.getAdditionalInformation());
                break;
                
            // shouldn't occur
            default:
                break;
            }

            // is DESCENDING? then flip sorting!
            if (SORT_DESCENDING == direction) {
                returnValue = -returnValue;
            }
        }

        return returnValue;
    }
}
