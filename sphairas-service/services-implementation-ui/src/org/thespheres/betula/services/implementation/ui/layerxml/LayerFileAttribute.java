package org.thespheres.betula.services.implementation.ui.layerxml;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.adminconfig.layerxml.AbstractLayerFile;

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
public class LayerFileAttribute extends AbstractLayerFile {

    private static final Object[] TYPES = new Object[]{
        "bytevalue", Byte.class,
        "shortvalue", Short.class,
        "intvalue", Integer.class,
        "longvalue", Long.class,
        "floatvalue", Float.class,
        "doublevalue", Double.class,
        "boolvalue", Boolean.class,
        "charvalue", Character.class,
        "stringvalue", String.class,
        "urlvalue", String.class,
        "methodvalue", String.class,
        "newvalue", String.class,
        "serialvalue", String.class
    };
    static final Map<String, Class> ATTRIBUTE_TYPES = new HashMap<>();

    static {
        for (int i = 0; i < TYPES.length;) {
            final String key = (String) TYPES[i++];
            final Class type = (Class) TYPES[i++];
            ATTRIBUTE_TYPES.put(key, type);
        }
    }

//    @XmlAttribute(name = "bytevalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String bytevalue;
//    @XmlAttribute(name = "shortvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String shortvalue;
//    @XmlAttribute(name = "intvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String intvalue;
//    @XmlAttribute(name = "longvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String longvalue;
//    @XmlAttribute(name = "floatvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String floatvalue;
//    @XmlAttribute(name = "doublevalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String doublevalue;
//    @XmlAttribute(name = "boolvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String boolvalue;
//    @XmlAttribute(name = "charvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String charvalue;
//    @XmlAttribute(name = "stringvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String stringvalue;
//    @XmlAttribute(name = "urlvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String urlvalue;
//    @XmlAttribute(name = "methodvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String methodvalue;
//    @XmlAttribute(name = "newvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String newvalue;
//    @XmlAttribute(name = "serialvalue")
//    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
//    protected String serialvalue;
    @XmlAnyAttribute
    private Map<QName, Object> attr;
    @XmlTransient
    private QName qNameOverride;
    @XmlTransient
    private String valueOverride;
    @XmlTransient
    private AbstractLayerFile parent;

    //JAXB only
    public LayerFileAttribute() {
    }

    public LayerFileAttribute(final String name) {
        super(name);
    }

    public void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) throws UnmarshalException {
        if (attr == null || attr.size() != 1) {
            throw new UnmarshalException("Attribute " + getName() + " of " + parent.toString() + "  has no or more than one value");
        }
        this.parent = (AbstractLayerFile) parent;
    }

    public void beforeMarshal(final Marshaller marshaller) {
        final Map.Entry<QName, Object> before = entry();
        if (isModified()) {
            final QName qn = qNameOverride != null ? qNameOverride : before.getKey();
            attr.clear();
            attr.put(qn, valueOverride);
        }
    }

//    public String getBytevalue() {
//        return bytevalue;
//    }
//
//    public void setBytevalue(String value) {
//        this.bytevalue = value;
//    }
//
//    public String getShortvalue() {
//        return shortvalue;
//    }
//
//    public void setShortvalue(String value) {
//        this.shortvalue = value;
//    }
//
//    public Integer getIntvalue() {
//        return intvalue != null ? Integer.parseInt(intvalue) : null;
//    }
//
//    public void setIntvalue(int value) {
//        this.intvalue = Integer.toString(value);
//    }
//
//    public String getLongvalue() {
//        return longvalue;
//    }
//
//    public void setLongvalue(String value) {
//        this.longvalue = value;
//    }
//
//    public String getFloatvalue() {
//        return floatvalue;
//    }
//
//    public void setFloatvalue(String value) {
//        this.floatvalue = value;
//    }
//
//    public String getDoublevalue() {
//        return doublevalue;
//    }
//
//    public void setDoublevalue(String value) {
//        this.doublevalue = value;
//    }
//
//    public String getBoolvalue() {
//        return boolvalue;
//    }
//
//    public void setBoolvalue(String value) {
//        this.boolvalue = value;
//    }
//
//    public String getCharvalue() {
//        return charvalue;
//    }
//
//    public void setCharvalue(String value) {
//        this.charvalue = value;
//    }
//
//    public String getStringvalue() {
//        return stringvalue;
//    }
//
//    public void setStringvalue(String value) {
//        this.stringvalue = value;
//    }
//
//    public String getUrlvalue() {
//        return urlvalue;
//    }
//
//    public void setUrlvalue(String value) {
//        this.urlvalue = value;
//    }
//
//    public String getMethodvalue() {
//        return methodvalue;
//    }
//
//    public void setMethodvalue(String value) {
//        this.methodvalue = value;
//    }
//
//    public String getNewvalue() {
//        return newvalue;
//    }
//
//    public void setNewvalue(String value) {
//        this.newvalue = value;
//    }
//
//    public String getSerialvalue() {
//        return serialvalue;
//    }
//
//    public void setSerialvalue(String value) {
//        this.serialvalue = value;
//    }
    private Map.Entry<QName, Object> entry() {
        final Map.Entry<QName, Object> entry = attr.entrySet().iterator().next();
        return entry;
    }

    public String getType() {
        final Map.Entry<QName, Object> entry = entry();
        return qNameOverride != null ? qNameOverride.getLocalPart() : entry.getKey().getLocalPart();
    }

    public String getValue() {
        final Map.Entry<QName, Object> entry = entry();
        return valueOverride != null ? valueOverride : entry.getValue().toString();
    }

    public void setAttribute(final String input) {
        final String text = StringUtils.stripToNull(input);
        if (text == null) {
            //remove attr
//            parent.setForRemoval(this);
        } else {
            final int index = text.indexOf(':');
            final String value;
            final QName before = entry().getKey();
            QName qno = null;
            if (index != -1) {
                final String type = text.substring(0, index);
                if (!ATTRIBUTE_TYPES.containsKey(type)) {
                    throw new IllegalArgumentException("Type " + type + " is not supported.");
                }
                if (!type.equals(before.getLocalPart())) {
                    qno = new QName(before.getNamespaceURI(), type, before.getPrefix());
                }
                value = text.substring(index + 1);
            } else {
                value = text;
            }
            final String typeKey = qno != null ? qno.getLocalPart() : before.getLocalPart();
            final Class type = ATTRIBUTE_TYPES.get(typeKey);
            if (type != String.class) {
                try {
                    type.getConstructor(String.class).newInstance(value);
                } catch (final Exception ex) {
                    throw new IllegalArgumentException(ex);
                }
            }
//            parent.unsetForRemoval(this);
            this.qNameOverride = qno;
            this.valueOverride = value;
        }
    }

    public boolean isModified() {
        return valueOverride != null;
    }

    public boolean isTemplate() {
        return false;
    }

    @Override
    public String toString() {
        return getType() + ":" + getValue();
    }

}
