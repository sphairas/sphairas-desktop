/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.util.Int2;
import org.thespheres.betula.util.Int2Adapter;

/**
 *
 * @author Boris Heithecker
 */
@XmlRootElement(name = "NotenAssessmentContext")
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlType(propOrder = {
    "rangeMaximum",
    "floorValues",
    "marginModel",
    "marginValue",
    "defaultDistribution"
})
public class NotenAssessmentXmlAdapter {

    public static final String LOCALNAME = "NotenAssessmentContext";

    public NotenAssessmentXmlAdapter() {
    }

    public NotenAssessmentXmlAdapter(Int2 rangeMaximum, Int2[] floorValues, String marginModel, Int2 marginValue, String defaultDist) {
        this.rangeMaximum = rangeMaximum;
        this.floorValues = floorValues;
        this.marginModel = marginModel;
        this.marginValue = marginValue;
        this.defaultDistribution = defaultDist;
    }

    public Int2 getRangeMaximum() {
        return rangeMaximum;
    }

    public Int2[] getFloorValues() {
        return floorValues;
    }

    public String getMarginModel() {
        return marginModel;
    }

    public Int2 getMarginValue() {
        return marginValue;
    }
    
    public String getDefaultDistribtution() {
        return defaultDistribution;
    }

    public void setDefaultDistribtution(String defaultDistribtution) {
        this.defaultDistribution = defaultDistribtution;
    }

    @XmlJavaTypeAdapter(value=Int2Adapter.class)
    private Int2 rangeMaximum;
    @XmlList //produziert einen Fehler beim JaXB
    @XmlJavaTypeAdapter(value=Int2Adapter.class)
    private Int2[] floorValues;
    private String marginModel;
    @XmlJavaTypeAdapter(value=Int2Adapter.class)
    private Int2 marginValue;
    private String defaultDistribution;
}
