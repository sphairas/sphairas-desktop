/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.curriculum.CourseGroup;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.util.MarkerAdapter.XmlMarkerAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlCourseGroup extends XmlCourseEntry implements CourseGroup {

    @XmlElementWrapper(name = "course-entries")
    @XmlElements({
        @XmlElement(name = "course", type = XmlCourse.class),
        @XmlElement(name = "group", type = XmlCourseGroup.class)
    })
    private List<CourseEntry> children = new ArrayList<>();
    @XmlElement(name = "description")
    @XmlJavaTypeAdapter(value = CollapsedStringAdapter.class)
    protected String description;
    @XmlElementWrapper(name = "definition")
    @XmlElement(name = "marker")
    @XmlJavaTypeAdapter(XmlMarkerAdapter.class)
    private final Set<Marker> definition = new HashSet<>();

    public XmlCourseGroup() {
    }

    public XmlCourseGroup(final String id, final XmlCourseGroup parent) {
        super(id, parent);
    }

    @Override
    public List<CourseEntry> getChildren() {
        return children.stream()
                .collect(Collectors.toList());
    }

    @Override
    public <C extends CourseEntry> List<C> getChildren(Class<C> type) {
        return children.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    @Override
    public <C extends CourseEntry> C addChild(final String id, final Class<C> type) {
        if (id == null
                || children.stream()
                        .anyMatch(e -> e.getId().equals(id))) {
            throw new IllegalArgumentException();
        }
        final XmlCourseEntry ret;
        if (CourseGroup.class.isAssignableFrom(type)) {
            ret = new XmlCourseGroup(id, getGroup());
            children.add(ret);
        } else if (CourseEntry.class.isAssignableFrom(type)) {
            ret = new XmlCourse(id, null);
            children.add(ret);
        } else {
            throw new IllegalArgumentException();
        }
        return (C) ret;
    }

    @Override
    public Set<Marker> getDefinition() {
        return definition;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

}
