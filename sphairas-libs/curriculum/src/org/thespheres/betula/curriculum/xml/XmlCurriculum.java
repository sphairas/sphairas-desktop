/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.xml;

import org.thespheres.betula.curriculum.DefaultCourseSelectionValue;
import java.util.ArrayList;
import org.thespheres.betula.curriculum.Curriculum;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.curriculum.CourseGroup;
import org.thespheres.betula.curriculum.CourseSelection;
import org.thespheres.betula.curriculum.Section;

/**
 *
 * @author boris.heithecker
 */
@XmlSeeAlso({DefaultCourseSelectionValue.class,
    XmlSection.class,
    CourseSelection.ClientProperty.class})
@XmlRootElement(name = "curriculum")
//@XmlType(namespace = "http://www.thespheres.org/xsd/betula/curriculum.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlCurriculum implements Curriculum {

    @XmlAttribute(name = "provider")
    private String provider;
    @XmlElement(name = "general", required = true)
    private final XmlGeneral general = new XmlGeneral();
    @XmlElementWrapper(name = "course-entries")
    @XmlElements({
        @XmlElement(name = "course", type = XmlCourse.class),
        @XmlElement(name = "group", type = XmlCourseGroup.class)
    })
    private final List<CourseEntry> courses = new ArrayList<>();
    @XmlElementWrapper(name = "sections")
    @XmlElementRef(type = XmlSection.class)
    private final List<Section> sections = new ArrayList<>();

    public XmlCurriculum() {
    }

    @Override
    public XmlGeneral getGeneral() {
        return general;
    }

    @Override
    public List<Section> getSections() {
        return sections;
    }

    @Override
    public List<CourseEntry> getEntries() {
        return courses;
    }

    public <C extends CourseEntry> C addEntry(final String id, final Class<C> type) {
        if (id == null
                || courses.stream()
                        .anyMatch(e -> e.getId().equals(id))) {
            throw new IllegalArgumentException();
        }
        if (CourseGroup.class.isAssignableFrom(type)) {
            final XmlCourseGroup ret = new XmlCourseGroup(id, null);
            courses.add(ret);
        } else if (CourseEntry.class.isAssignableFrom(type)) {
            final XmlCourse ret = new XmlCourse(id, null);
            courses.add(ret);
        }
        throw new IllegalArgumentException();
    }

}
