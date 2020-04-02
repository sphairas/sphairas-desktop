/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.xmlimport.utilities.AbstractLink;

/**
 *
 * @author boris.heithecker
 */
//TODO: same as unitis, create superclass
@XmlRootElement(name = "sibank-target-import-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiBankAssoziation extends AbstractLink<UniqueSatzDistinguisher> {

    private static final transient boolean compareMustEqualOverridenSourceValue = true;
    @XmlElement(name = "satz-distinguishing-elements")
    private UniqueSatzDistinguisher distinguisher;

    public SiBankAssoziation() {
    }

    public SiBankAssoziation(UniqueSatzDistinguisher lessonId) {
        this(lessonId, 0);
    }

    public SiBankAssoziation(UniqueSatzDistinguisher lessonId, int clone) {
        this.distinguisher = lessonId;
        this.clone = clone;
    }

    @Override
    public UniqueSatzDistinguisher getSourceIdentifier() {
        return distinguisher;
    }

    @Override
    protected boolean isCompareMustEqualOverridenSourceValue() {
        return compareMustEqualOverridenSourceValue;
    }

//    @Deprecated
//    UnitId getUnitDeprected() {
//        return unitDeprected;
//    }
//    @Deprecated
//     void setUnitDeprected(UnitId unitDeprected) {
//        this.unitDeprected = unitDeprected;
//    }
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.distinguisher);
        return 41 * hash + this.clone;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SiBankAssoziation other = (SiBankAssoziation) obj;
        if (!Objects.equals(this.distinguisher, other.distinguisher)) {
            return false;
        }
        return this.clone == other.clone;
    }

}
