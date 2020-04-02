/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport;

import java.beans.PropertyVetoException;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;

/**
 *
 * @author boris.heithecker
 */
public abstract class ImportStudentItem extends ImportItem {

    public static final String PROP_SOURCE_STATUS = "source.status";
    public static final String PROP_SOURCE_STUDENT_CAREER = "source.student.career";
    protected String sourceStudentCareer;
    protected String sourceStatus;
    private UnitId[] primaryUnits;

    protected ImportStudentItem(String sourceNode) {
        super(sourceNode);
    }

    public abstract StudentId getStudentId();

    public void setPrimaryUnits(UnitId[] pu) {
        this.primaryUnits = pu;
    }

    public UnitId[] getPrimaryUnits() {
        return primaryUnits;
    }

    public void setSourceStatus(String sourceStatus) throws PropertyVetoException {
        String old = this.sourceStatus;
        this.sourceStatus = sourceStatus;
        try {
            vSupport.fireVetoableChange(PROP_SOURCE_STATUS, old, this.sourceStatus);
        } catch (PropertyVetoException vex) {
            this.sourceStatus = old;
            throw vex;
        }
    }

    public String getSourceStatus() {
        return sourceStatus;
    }

    public String getSourceStudentCareer() {
        return sourceStudentCareer;
    }

    public void setSourceStudentCareer(String sourceStudentCareer) throws PropertyVetoException {
        String old = this.sourceStudentCareer;
        this.sourceStudentCareer = sourceStudentCareer;
        try {
            vSupport.fireVetoableChange(PROP_SOURCE_STUDENT_CAREER, old, this.sourceStudentCareer);
        } catch (PropertyVetoException vex) {
            this.sourceStudentCareer = old;
            throw vex;
        }
    }

}
