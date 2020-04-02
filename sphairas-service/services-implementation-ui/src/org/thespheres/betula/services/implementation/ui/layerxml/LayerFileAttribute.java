package org.thespheres.betula.services.implementation.ui.layerxml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

//@XmlType(propOrder = {"name",
//    "bytevalue",
//    "shortvalue",
//    "intvalue",
//    "longvalue",
//    "floatvalue",
//    "doublevalue",
//    "boolvalue",
//    "charvalue",
//    "stringvalue",
//    "urlvalue",
//    "methodvalue",
//    "newvalue",
//    "serialvalue"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LayerFileAttribute {

    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(value = NormalizedStringAdapter.class)
    protected String name;
    @XmlAttribute(name = "bytevalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String bytevalue;
    @XmlAttribute(name = "shortvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String shortvalue;
    @XmlAttribute(name = "intvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String intvalue;
    @XmlAttribute(name = "longvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String longvalue;
    @XmlAttribute(name = "floatvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String floatvalue;
    @XmlAttribute(name = "doublevalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String doublevalue;
    @XmlAttribute(name = "boolvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String boolvalue;
    @XmlAttribute(name = "charvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String charvalue;
    @XmlAttribute(name = "stringvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String stringvalue;
    @XmlAttribute(name = "urlvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String urlvalue;
    @XmlAttribute(name = "methodvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String methodvalue;
    @XmlAttribute(name = "newvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String newvalue;
    @XmlAttribute(name = "serialvalue")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String serialvalue;

    //JAXB only
    public LayerFileAttribute() {
    }

    public LayerFileAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getBytevalue() {
        return bytevalue;
    }

    public void setBytevalue(String value) {
        this.bytevalue = value;
    }

    public String getShortvalue() {
        return shortvalue;
    }

    public void setShortvalue(String value) {
        this.shortvalue = value;
    }

    public Integer getIntvalue() {
        return intvalue != null ? Integer.parseInt(intvalue) : null;
    }

    public void setIntvalue(int value) {
        this.intvalue = Integer.toString(value);
    }

    public String getLongvalue() {
        return longvalue;
    }

    public void setLongvalue(String value) {
        this.longvalue = value;
    }

    public String getFloatvalue() {
        return floatvalue;
    }

    public void setFloatvalue(String value) {
        this.floatvalue = value;
    }

    public String getDoublevalue() {
        return doublevalue;
    }

    public void setDoublevalue(String value) {
        this.doublevalue = value;
    }

    public String getBoolvalue() {
        return boolvalue;
    }

    public void setBoolvalue(String value) {
        this.boolvalue = value;
    }

    public String getCharvalue() {
        return charvalue;
    }

    public void setCharvalue(String value) {
        this.charvalue = value;
    }

    public String getStringvalue() {
        return stringvalue;
    }

    public void setStringvalue(String value) {
        this.stringvalue = value;
    }

    public String getUrlvalue() {
        return urlvalue;
    }

    public void setUrlvalue(String value) {
        this.urlvalue = value;
    }

    public String getMethodvalue() {
        return methodvalue;
    }

    public void setMethodvalue(String value) {
        this.methodvalue = value;
    }

    public String getNewvalue() {
        return newvalue;
    }

    public void setNewvalue(String value) {
        this.newvalue = value;
    }

    public String getSerialvalue() {
        return serialvalue;
    }

    public void setSerialvalue(String value) {
        this.serialvalue = value;
    }

}
