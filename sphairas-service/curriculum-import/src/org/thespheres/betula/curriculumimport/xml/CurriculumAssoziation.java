/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.xml;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.curriculumimport.StundentafelImportTargetsItem.ID;
import org.thespheres.betula.xmlimport.utilities.AbstractLink;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "association")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class CurriculumAssoziation extends AbstractLink<ID> {

    private static final transient boolean compareMustEqualOverridenSourceValue = true;
    @XmlAttribute(name = "course")
    private String course;

    public CurriculumAssoziation() {
        super();
    }

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    CurriculumAssoziation(final ID id) {
        super();
        this.course = id.getCourse();
        setUnit(id.getUnit());
    }

    @Override
    public ID getSourceIdentifier() {
        return new ID(this.course, this.unit);
    }

    @Override
    protected boolean isCompareMustEqualOverridenSourceValue() {
        return compareMustEqualOverridenSourceValue;
    }

    @Override
    public boolean hasUnit() {
        //unit is fixed
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.course);
        return 97 * hash + Objects.hashCode(this.unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CurriculumAssoziation other = (CurriculumAssoziation) obj;
        if (!Objects.equals(this.course, other.course)) {
            return false;
        }
        return Objects.equals(this.unit, other.unit);
    }

}
