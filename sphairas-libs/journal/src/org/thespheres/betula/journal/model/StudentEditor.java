/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.thespheres.betula.Student;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.util.XmlStudent;

/**
 *
 * @author boris.heithecker
 */
public abstract class StudentEditor {

    private int index;
    private Student student;
    protected PropertyChangeSupport pSupport = new PropertyChangeSupport(this);

    protected StudentEditor(Student student) {
        if (student == null) {
            throw new IllegalArgumentException();
        }
        this.student = student;
    }

    public String getDirectoryName() {
        return student.getDirectoryName();
    }

    public String getGivenNames() {
        return getStudent().getGivenNames();
    }

    public String getFullname() {
        return getStudent().getFullName();
    }

    public String getFirstName() {
        return getStudent().getFirstName();
    }

    public String getSurname() {
        return getStudent().getSurname();
    }

    protected int getIndex() {
        return index;
    }

    public Student getStudent() {
        return student;
    }

    protected void setStudent(XmlStudent s) {
        this.student = s;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

    protected void setIndex(int index) {
        this.index = index;
    }

    protected abstract void setFullName(StringBuilder sb);

    @Override
    public String toString() {
        return getStudent().toString();
    }

    public UnitId getUnit() {
        if (getStudent() instanceof Student.PrimaryUnit) {
            return ((Student.PrimaryUnit) getStudent()).getPrimaryUnit();
        } else {
            return null;
        }
    }
}
