/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.xml;

import org.thespheres.betula.curriculum.Section;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.curriculum.CourseSelection;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "section")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlSection implements Section {

    private final static Pattern SEQUENCE_PATTERN = Pattern.compile("\\d+(\\.\\d\\d*)?");
    @XmlAttribute(name = "sequence", required = true)
    private String sequence;
    @XmlElementWrapper(name = "course-selection-entries")
    @XmlElement(name = "course-selection-entry", type = XmlCourseSelection.class)
    private final Set<CourseSelection> selection = new HashSet<>();

    public XmlSection() {
    }

    public XmlSection(String formatted) {
        if (!SEQUENCE_PATTERN.matcher(formatted).matches()) {
            throw new IllegalArgumentException();
        }
        this.sequence = formatted;
    }

    @Override
    public int getSequence() {
        int c = sequence.indexOf(".");
        if (c != -1) {
            return Integer.parseInt(sequence.substring(c + 1));
        } else {
            return 1;
        }
    }

    @Override
    public int getBase() {
        int c = sequence.indexOf(".");
        if (c != -1) {
            return Integer.parseInt(sequence.substring(0, c));
        } else {
            return Integer.parseInt(sequence);
        }
    }

    @Override
    public Set<CourseSelection> getSelection() {
        return selection;
    }

}
