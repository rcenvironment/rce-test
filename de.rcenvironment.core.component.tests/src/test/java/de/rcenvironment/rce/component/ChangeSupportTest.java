/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Test;


/**
 * Test cases for {@link ChangeSupport}.
 *
 * @author Doreen Seider
 */
public class ChangeSupportTest {

    private final String invoked = "invoked";
    
    private final String property = "property";
    
    private final Object propertyObject = new Object();
    
    private PropertyChangeListener l = new PropertyChangeListener() {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            throw new RuntimeException(invoked + evt.getNewValue());
        }
    };
    
    /** Test. */
    @Test
    public void testPropertyChange() {
        ChangeSupport changeSupport = new PropertyChanger();
        
        changeSupport.firePropertyChange(property);
        
        changeSupport.addPropertyChangeListener(l);
        
        try {
            changeSupport.firePropertyChange(property);
        } catch (RuntimeException e) {
            assertEquals(invoked + "null", e.getMessage());
        }
        
        try {
            changeSupport.firePropertyChange(property, propertyObject);
        } catch (RuntimeException e) {
            assertEquals(invoked + propertyObject.toString(), e.getMessage());
        }
        
        changeSupport.removePropertyChangeListener(l);

        changeSupport.firePropertyChange(property);
    }
    
    /**
     * Dummy class extending the class under test.
     * @author Doreen Seider
     */
    public class PropertyChanger extends ChangeSupport {
        
    }
}
