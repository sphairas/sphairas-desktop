/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.xml;

import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.RecordNote;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.util.GradeAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "note", namespace = "http://www.thespheres.org/xsd/betula/journal.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRecordNote extends RecordNote {

    @XmlAttribute
    private String scope;
    @XmlElement
    private StudentId student;
    @XmlJavaTypeAdapter(value = GradeAdapter.class)
    @XmlElement
    private Grade grade;
    @XmlElement
    private String text;
    @XmlElement(name = "timestamp")
    private Timestamp time;

    public XmlRecordNote() {
    }

    public XmlRecordNote(final String scope, final StudentId student) {
        this.scope = scope;
        this.student = student;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public StudentId getStudent() {
        return student;
    }

    @Override
    public Grade getGrade() {
        return grade;
    }

    @Override
    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    @Override
    public String getCause() {
        return text;
    }

    @Override
    public void setCause(String cause, Timestamp time) {
        this.text = cause;
        this.time = time;
    }

    @Override
    public Timestamp getTime() {
        return time;
    }

}
