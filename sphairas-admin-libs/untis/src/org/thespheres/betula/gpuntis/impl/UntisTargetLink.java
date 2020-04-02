/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.impl;

import org.thespheres.betula.xmlimport.utilities.AbstractLink;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "untis-target-import-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class UntisTargetLink extends AbstractLink<String> {

    protected static final transient boolean compareMustEqualOverridenSourceValue = true;
    @XmlAttribute(name = "untis-lesson-id")
    private String lessonId;

    public UntisTargetLink() {
    }

    public UntisTargetLink(String lessonId) {
        this(lessonId, 0);
    }

    public UntisTargetLink(String lessonId, int clone) {
        this.lessonId = lessonId;
        this.clone = clone;
    }

    @Override
    public String getSourceIdentifier() {
        return lessonId;
    }

    @Override
    protected boolean isCompareMustEqualOverridenSourceValue() {
        return compareMustEqualOverridenSourceValue;
    }

//    @Deprecated
//    UnitId getUnitDeprected() {
//        return unitDeprected;
//    }
//
//    @Deprecated
//    void setUnitDeprected(UnitId unitDeprected) {
//        this.unitDeprected = unitDeprected;
//    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.lessonId);
        return 41 * hash + this.clone;
    }

    @Override
    public boolean equals(Object obj
    ) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UntisTargetLink other = (UntisTargetLink) obj;
        if (!Objects.equals(this.lessonId, other.lessonId)) {
            return false;
        }
        return this.clone == other.clone;
    }

}
