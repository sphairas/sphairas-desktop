/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.model.XmlTargetItem;

/**
 *
 * @author boris.heithecker
 * @param <S> source id
 */
@XmlSeeAlso(ColumnProperty.class)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractLink<S> extends XmlTargetItem {

    @XmlAttribute(name = "clone")
    protected int clone;
    @XmlJavaTypeAdapter(value = MarkerAdapter.XmlMarkerAdapter.class)
    @XmlElement(name = "subject")
    protected Marker subject;
    @XmlJavaTypeAdapter(value = MarkerAdapter.XmlMarkerAdapter.class)
    @XmlElement(name = "source-subject-overridden")
    protected Marker subjectOverridden;
    @XmlElement(name = "source-signee-overridden")
    protected Signee signeeOverridden;
    @XmlElement(name = "source-unit-overridden")
    protected UnitId unitOverridden;
    @XmlElement(name = "target-suffix")
    protected String targetSuffix;
    @XmlElementWrapper(name = "properties")
    @XmlElementRef
    protected List<ColumnProperty> properties;
    //The value that has thrown a SourceValueNotEqualToOverridenException
    @XmlTransient
    private Marker sourceSubjectExceptionValue;
    @XmlTransient
    private Signee sourceSigneeExceptionValue;
    @XmlTransient
    private UnitId sourceUnitExceptionValue;
    //This is necessary to catch all unknown elements during unmarshalling, 
    //so they will be marshalled again back into the document and don't get lost.
    //Loss might otherwise occur if the user unmarshals the document 
    //with specific modules disenabled that would add additional ColumnProperty classes.
    @XmlAnyElement
    public org.w3c.dom.Element[] otherElements;

    protected AbstractLink() {
    }

    protected abstract S getSourceIdentifier();

    public int getClone() {
        return clone;
    }

    protected abstract boolean isCompareMustEqualOverridenSourceValue();

    public boolean hasSubjectMarker() {
        return subject != null || subjectOverridden != null;
    }

    @Deprecated
    static Marker markerOrNull(Marker m) {
        return (m == null || m.equals(Marker.NULL)) ? null : m;
    }

    public Marker getSubjectMarker(final Marker sourceValue) throws SourceTargetLinkException {
        if (hasSubjectMarker()) {
            if (!Objects.equals(sourceValue, subject)) {
                //set to true to avoid setting overrides when the source definition has changed meanwhile
                if (isCompareMustEqualOverridenSourceValue() ? Objects.equals(sourceValue, subjectOverridden) : true) {
                    return markerOrNull(subject);
                }
                String source = sourceValue != null ? sourceValue.getLongLabel() : null;
                String found = subjectOverridden != null ? subjectOverridden.getLongLabel() : null;
                this.sourceSubjectExceptionValue = Marker.isNull(sourceValue) ? Marker.NULL : sourceValue;
                throw new SourceValueNotEqualToOverridenException("subject", source, found);
            }
            //should wie reset override?
        }
        return null;
        //        if (subject != null && !Objects.equals(Utils.markerOrNullMarker(sourceValue), subject)) {
        //            //set to true to avoid setting overrides when the source definition has changed meanwhile
        //            if (compareMustEqualOverridenOriginal ? Objects.equals(Utils.markerOrNullMarker(sourceValue), subjectOverridden) : true) {
        //                return Utils.markerOrNull(subject);
        //            }//else should we delete the definition?
        //        }
        //        return null;
    }

    public boolean setSubjectMarker(Marker old, Marker override) {
        if (hasSubjectMarker() && Objects.equals(override, this.subjectOverridden)) {
            this.subjectOverridden = null;
            this.subject = null;
        } else {
            if (this.subjectOverridden == null) {
                this.subjectOverridden = old;
            } else if (sourceSubjectExceptionValue != null) {//The user has accepted/view the differing source 
                this.subjectOverridden = Marker.isNull(sourceSubjectExceptionValue) ? null : sourceSubjectExceptionValue;
            }
            this.subject = override;
            return true;
        }
        return false;
        //        Marker override = Utils.markerOrNullMarker(newValue);
        //        if (Objects.equals(override, this.subjectOverridden)) {
        //            this.subjectOverridden = null;
        //            this.subject = null;
        //            return false;
        //        } else {
        //            if (this.subjectOverridden == null) {
        //                this.subjectOverridden = Utils.markerOrNullMarker(old);
        //            }
        //            this.subject = override;
        //            return true;
        //        }
    }

    public boolean hasSignee() {
        return signee != null || signeeOverridden != null;
    }

    public Signee getSignee(Signee sourceValue) throws SourceTargetLinkException {
        if (hasSignee()) {
            if (!Objects.equals(sourceValue, signee)) {
                //set to true to avoid setting overrides when the source definition has changed meanwhile
                if (isCompareMustEqualOverridenSourceValue() ? Objects.equals(sourceValue, signeeOverridden) : true) {
                    return getSignee();
                }
                String source = sourceValue != null ? sourceValue.getId() : null;
                String found = signeeOverridden != null ? signeeOverridden.getId() : null;
                this.sourceSigneeExceptionValue = Signee.isNull(sourceValue) ? Signee.NULL : sourceValue;
                throw new SourceValueNotEqualToOverridenException("signee", source, found);
            }
            //should wie reset override?
        }
        return null;
    }

    public boolean setSignee(Signee old, Signee override) {
        if (hasSignee() && Objects.equals(override, this.signeeOverridden)) {
            this.signeeOverridden = null;
            this.signee = null;
        } else {
            if (this.signeeOverridden == null) {
                this.signeeOverridden = old;
            } else if (sourceSigneeExceptionValue != null) {//The user has accepted/view the differing source 
                this.signeeOverridden = Signee.isNull(sourceSigneeExceptionValue) ? null : sourceSigneeExceptionValue;
            }
            this.signee = override;
            return true;
        }
        return false;
    }

    public boolean hasUnit() {
        return unit != null;
    }

    public UnitId getUnit(UnitId sourceValue) throws SourceTargetLinkException {
        if (unit != null && !Objects.equals(sourceValue, unit)) {
            if (!Objects.equals(sourceValue, unit)) {
                //set to true to avoid setting overrides when the source definition has changed meanwhile
                if (isCompareMustEqualOverridenSourceValue() ? Objects.equals(sourceValue, unitOverridden) : true) {
                    return getUnit();
                }
                String source = sourceValue != null ? sourceValue.getId() : null;
                String found = unitOverridden != null ? unitOverridden.getId() : null;
                this.sourceUnitExceptionValue = UnitId.isNull(sourceValue) ? UnitId.NULL : sourceValue;
                SourceValueNotEqualToOverridenException ex = new SourceValueNotEqualToOverridenException("unit", source, found);
                ex.setOverride(unit);
                throw ex;
            } //else should we delete the definition?
        }
        return null;
    }

    public boolean setUnit(UnitId old, UnitId override) {
        if (Objects.equals(override, this.unitOverridden)) {
            this.unitOverridden = null;
            this.unit = null;
            return false;
        } else {
            if (this.unitOverridden == null) {
                this.unitOverridden = old;
            } else if (sourceUnitExceptionValue != null) {//The user has accepted/view the differing source 
                this.unitOverridden = UnitId.isNull(sourceUnitExceptionValue) ? null : sourceUnitExceptionValue;
            }
            this.unit = override;
            return true;
        }
    }

    public String getTargetSuffix() {
        return targetSuffix;
    }

    public boolean setTargetSuffix(String override) {
        String old = this.targetSuffix;
        this.targetSuffix = override;
        return !Objects.equals(old, targetSuffix);
    }

    public boolean hasColumnOverride(String column) {
        return getNonDefaultProperty(column) != null;
    }

    public ColumnProperty getNonDefaultProperty(final String colId) {
        if (properties != null) {
            return properties.stream()
                    .filter(cp -> cp.getColumnId().equals(colId))
                    .collect(CollectionUtil.requireSingleOrNull());
        }
        return null;
    }

    public Map<String, ColumnProperty> getNonDefaultProperties() {
        if (properties != null) {
            return properties.stream()
                    .collect(Collectors.toMap(ColumnProperty::getColumnId, cp -> cp));
        }
        return Collections.EMPTY_MAP;
    }

    public void setNonDefaultProperty(ColumnProperty prop) {
        if (properties != null) {
            final String cid = prop.getColumnId();
            removeProperty(cid);
        } else {
            properties = new ArrayList<>();
        }
        properties.add(prop);
    }

    public void removeNonDefaultProperty(String prop) {
        if (properties != null) {
            removeProperty(prop);
            if (properties.isEmpty()) {
                properties = null;
            }
        }
    }

    private void removeProperty(final String cid) {
        properties.stream()
                .filter(cp -> cp.getColumnId().equals(cid))
                .collect(CollectionUtil.requireSingleton())
                .ifPresent(cp -> properties.remove(cp));
    }

    public static final class SourceValueNotEqualToOverridenException extends SourceTargetLinkException {

        final String property;
        final String foundValue;
        final String sourceValue;
        Object override;

        public SourceValueNotEqualToOverridenException(String property, String sourceValue, String found) {
            super();
            this.property = property;
            this.sourceValue = sourceValue;
            this.foundValue = found;
        }

        public String getProperty() {
            return property;
        }

        public String getFoundValue() {
            return foundValue;
        }

        public String getSourceValue() {
            return sourceValue;
        }

        public Object getOverride() {
            return override;
        }

        public void setOverride(Object override) {
            this.override = override;
        }

    }

}
