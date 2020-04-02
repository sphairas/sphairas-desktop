/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.util.XmlMarkerSet;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "unit-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlUnitItem extends XmlItem {

    @XmlElement(name = "unit", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    protected UnitId unit;
    @XmlElement(name = "unit")
    @Deprecated
    protected UnitId unitDeprected;
    @XmlElement(name = "source-unit")
    protected String sourceUnit;
    @XmlElement(name = "source-primary-unit")
    protected String sourcePrimaryUnit;
    @XmlElement(name = "source-level")
    protected String sourceLevel;
    @XmlJavaTypeAdapter(XmlMarkerSet.Adapter.class)
    @XmlElement(name = "marker")
    protected XmlMarkerSet markerSet;
    @XmlElementWrapper(name = "source-markers")
    @XmlElement(name = "marker")
    protected String[] sourcesMarkers;
    @XmlElement(name = "delete")
    protected SourceDateTime deleteDate;
    //termscheduleprovider, namingProvider
    @XmlElementWrapper(name = "students")
    @XmlElementRef
    protected List<XmlStudentItem> students;

    public UnitId getUnit() {
        return unit;
    }

    public void setUnit(UnitId unit) {
        this.unit = unit;
    }

    public String getSourceUnit() {
        return sourceUnit != null ? sourceUnit : getSourcePrimaryUnit();
    }

    public String getSourcePrimaryUnit() {
        return sourcePrimaryUnit;
    }

    public void setSourcePrimaryUnit(String sourcePrimaryUnit) {
        this.sourcePrimaryUnit = sourcePrimaryUnit;
    }

    public XmlMarkerSet getMarkerSet() {
        if (markerSet == null) {
            markerSet = new XmlMarkerSet();
        }
        return markerSet;
    }

    public String[] getSourcesMarkers() {
        return sourcesMarkers;
    }

    public String getSourceLevel() {
        return sourceLevel;
    }

    public void setSourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

    public SourceDateTime getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(SourceDateTime deleteDate) {
        this.deleteDate = deleteDate;
    }

    public List<XmlStudentItem> getStudents() {
        if (students == null) {
            students = new ArrayList<>();
        }
        return students;
    }

}
