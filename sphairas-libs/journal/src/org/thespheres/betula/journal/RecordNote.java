/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class RecordNote {

    public abstract String getScope();

    public abstract StudentId getStudent();

    public abstract Grade getGrade();

    public abstract void setGrade(Grade grade);

    public abstract String getCause();

    public abstract void setCause(String cause, Timestamp time);

    public abstract Timestamp getTime();
}
