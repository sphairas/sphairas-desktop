/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.thespheres.betula.curriculum.CourseEntry;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlCourse extends XmlCourseEntry implements CourseEntry {

    public XmlCourse() {
    }

    public XmlCourse(final String id, final XmlCourseGroup parent) {
        super(id, parent);
    }

}
