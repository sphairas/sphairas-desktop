/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

import java.beans.PropertyChangeListener;
import java.util.Set;

/**
 *
 * @author boris.heithecker
 */
public interface Unit {

    public static final String PROP_DISPLAYNAME = "displayName";
    public static final String PROP_STUDENTS = "students";

    public UnitId getUnitId();

    public Set<Student> getStudents();

    public Student findStudent(StudentId id);

    public String getDisplayName();

    public void addPropertyChangeListener(PropertyChangeListener l);

    public void removePropertyChangeListener(PropertyChangeListener l);
}
