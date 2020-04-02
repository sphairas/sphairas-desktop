/*
 * AssessorMapEntry.java
 *
 * Created on 16. November 2007, 22:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.beans.PropertyChangeListener;

/**
 *
 * @author Boris Heithecker
 * @param <T>
 */
public interface AssessorMapEntry<T extends Comparable> {

    public static final String PROP_VALUE = "value";
    public static final String PROP_GRADE = "grade";

    public void setValue(T value);

    public T getValue();

    public void setGrade(Grade grade);

    public Grade getGrade();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);
}
