/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.thespheres.betula.curriculum.CourseSelection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.curriculum.CourseSelectionValue;
import org.thespheres.betula.curriculum.UnsupportedPropertyTypeException;
import org.thespheres.betula.curriculum.impl.CurriculumSupport;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlCourseSelection implements CourseSelection {

    @XmlAttribute(name = "course", required = true)
    @XmlIDREF
    private XmlCourse course;
    @XmlElement(name = "note")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String note;
    @XmlElementRef
    private CourseSelectionValue value;
    @XmlElementWrapper(name = "client-properities")
    private List<XmlClientProperty> clientProps;

    public XmlCourseSelection() {
    }

    public XmlCourseSelection(final XmlCourse course) {
        this.course = course;
    }

    @Override
    public XmlCourse getCourse() {
        return course;
    }

    @Override
    public CourseSelectionValue getCourseSelectionValue() {
        return value;
    }

    @Override
    public void setCourseSelectionValue(final CourseSelectionValue value) {
        this.value = value;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public final <P extends ClientProperty> Optional<P> getClientProperty(final String key, final Class<P> clz) {
        if (clientProps != null) {
            return clientProps.stream()
                    .filter(o -> key.equals(o.getKey()))
                    .map(XmlClientProperty::getValue)
                    .filter(v -> clz.isAssignableFrom(v.getClass()))
                    .map(clz::cast)
                    .collect(CollectionUtil.singleton());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <P extends ClientProperty> void setClientProperty(String key, P value) throws UnsupportedPropertyTypeException {
        if (clientProps == null && value != null) {
            clientProps = new ArrayList<>();
        }
        if (clientProps != null) {
            final XmlClientProperty prop = clientProps.stream()
                    .filter(o -> key.equals(o.getKey()))
                    .collect(CollectionUtil.requireSingleOrNull());
            if (value != null) {
                if (!Arrays.stream(CurriculumSupport.JAXB_TYPES).anyMatch(value.getClass()::equals)) {
                    throw new UnsupportedPropertyTypeException("Type " + value.getClass().getName() + " is not supported.");
                }
                if (prop != null) {
                    prop.setValue(value);
                } else {
                    clientProps.add(new XmlClientProperty(key, value));
                }
            } else if (prop != null) {
                clientProps.remove(prop);
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class XmlClientProperty {

        @XmlAttribute(name = "key", required = true)
        private String key;
        @XmlElementRef(required = true)
        private ClientProperty value;

        public XmlClientProperty() {
        }

        XmlClientProperty(String key, ClientProperty value) {
            this.key = key;
            this.value = value;
        }

        String getKey() {
            return key;
        }

        Object getValue() {
            return value;
        }

        private <P> void setValue(P value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
