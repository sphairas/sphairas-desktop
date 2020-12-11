/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.xml;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.gpuntis.xml.Class.ClassRef;

/**
 *
 * @author boris.heithecker@gmx.net
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "studentgroup", namespace = "https://untis.at/untis/XmlInterface")
public class StudentGroup {

    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlElement(name = "subject")
    @XmlIDREF
    protected Subject subject;

    @XmlElementWrapper(name  ="classes")
    @XmlElement(name = "class")
    @XmlIDREF
    protected List<ClassRef> clazz;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class StudentGroupRef {

        @XmlAttribute(name = "id", required = true)
        @XmlIDREF
        @XmlList
        protected List<StudentGroup> studentGroup;

        public List<StudentGroup> get() {
            return studentGroup;
        }

    }
}
