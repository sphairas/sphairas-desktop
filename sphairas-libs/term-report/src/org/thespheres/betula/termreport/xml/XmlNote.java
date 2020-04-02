/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.termreport.TermReport;

@XmlRootElement(name = "note", namespace = "http://www.thespheres.org/xsd/betula/term-report.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlNote extends TermReport.Note {

    @XmlElement(name = "text")
    protected String text;
    @XmlAttribute(name = "timestamp")
    protected Timestamp timestamp;
    @XmlElement(name = "student")
    private StudentId student;

    public XmlNote() {
    }

    public XmlNote(String text, Timestamp timestamp) {
        this.text = text;
        this.timestamp = timestamp;
    }

    public XmlNote(StudentId key, String value, Timestamp timestamp) {
        this(value, timestamp);
        this.student = key;
    }

    public StudentId getStudent() {
        return student;
    }

    public void setStudent(StudentId student) {
        this.student = student;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

}
