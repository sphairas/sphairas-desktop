/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.document.util.GenericXmlDocument;
import org.thespheres.betula.document.util.GenericXmlTicket;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.document.util.XmlContentValue;
import org.thespheres.betula.util.GradeAdapter;

/**
 *
 * @author boris.heithecker
 * @param <V>
 */
@XmlType(name = "templateType",
        propOrder = {"value"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Template<V extends Object> extends Envelope {

    public static final String PROP_VALUE = "PROP_VALUE";
    @XmlElements(value = {//Content == Text (z.B klassenbucheinträge) & description & note & datum  & marker set...                     
        @XmlElement(name = "text", type = String.class),
        @XmlElement(name = "int-value", type = Integer.class),
        @XmlElement(name = "double-value", type = Double.class),
        @XmlElement(name = "grade", type = GradeAdapter.class, namespace = "http://www.thespheres.org/xsd/betula/betula.xsd"),
        @XmlElement(name = "marker", type = MarkerAdapter.class, namespace = "http://www.thespheres.org/xsd/betula/betula.xsd"),
        @XmlElement(name = "content-value", type = XmlContentValue.class), //, namespace = "http://www.thespheres.org/xsd/betula/betula.xsd"),
        @XmlElement(name = "ticket-value", type = GenericXmlTicket.class), //, namespace = "http://www.thespheres.org/xsd/betula/betula.xsd"),
        @XmlElement(name = "document-value", type = GenericXmlDocument.class)}) // namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")})
    private V value;

//    @XmlElementRef
//    private V value2;
    public Template() {
    }

    public Template(Action action) {
        this.action = action;
    }

    public Template(Action action, V value) {
        this.action = action;
        setValueImpl(value);
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        setValueImpl(value);
    }

    protected final Object setValueImpl(V val) throws IllegalArgumentException {
        if (!(val == null
                || val instanceof String
                || val instanceof Integer
                || val instanceof Double
                || val instanceof GradeAdapter
                || val instanceof MarkerAdapter
                || val instanceof GenericXmlTicket
                || val instanceof XmlContentValue
                || val instanceof GenericXmlDocument)) {
            throw new IllegalArgumentException();
        }
        Object oldValue = value;
        this.value = val;
        return oldValue;
    }

//    protected final Object setValueImpl2(V val) throws IllegalArgumentException {
//        if (val.getClass().getAnnotation(XmlRootElement.class) == null) {
//            throw new IllegalArgumentException("Value of class Template must be XmlRootElement.");
//        }
//        final Object oldValue = value2;
//        value2 = val;
//        return oldValue;
//    }
//    @XmlRootElement(name = "text")
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static class TemplateStringValue {
//
//        @XmlValue
//        String value;
//
//        public TemplateStringValue() {
//        }
//
//        private TemplateStringValue(String v) {
//            value = v;
//        }
//
//        public static class Adapter extends XmlAdapter<TemplateStringValue, String> {
//
//            @Override
//            public String unmarshal(TemplateStringValue v) throws Exception {
//                return v.value;
//            }
//
//            @Override
//            public TemplateStringValue marshal(String v) throws Exception {
//                return new TemplateStringValue(v);
//            }
//
//        }
//    }
//
//    @XmlRootElement(name = "int-value")
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static class TemplateIntValue {
//
//        @XmlValue
//        Integer value;
//
//        public TemplateIntValue() {
//        }
//
//        private TemplateIntValue(Integer v) {
//            value = v;
//        }
//
//        public static class Adapter extends XmlAdapter<TemplateIntValue, Integer> {
//
//            @Override
//            public Integer unmarshal(TemplateIntValue v) throws Exception {
//                return v.value;
//            }
//
//            @Override
//            public TemplateIntValue marshal(Integer v) throws Exception {
//                return new TemplateIntValue(v);
//            }
//
//        }
//    }
//
//    @XmlRootElement(name = "double-value")
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static class TemplateDoubleValue {
//
//        @XmlValue
//        Double value;
//
//        public TemplateDoubleValue() {
//        }
//
//        private TemplateDoubleValue(Double v) {
//            value = v;
//        }
//
//        public static class Adapter extends XmlAdapter<TemplateDoubleValue, Double> {
//
//            @Override
//            public Double unmarshal(TemplateDoubleValue v) throws Exception {
//                return v.value;
//            }
//
//            @Override
//            public TemplateDoubleValue marshal(Double v) throws Exception {
//                return new TemplateDoubleValue(v);
//            }
//
//        }
//    }
}
