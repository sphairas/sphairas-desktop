/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.*;
import org.thespheres.betula.Student;
import org.thespheres.betula.Students;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.util.XmlStudent.XmlStudentExt;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "xml-students", namespace = "http://www.thespheres.org/xsd/betula/xml-students.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlStudents extends Students<XmlStudent> {

    @XmlElements({
        @XmlElement(name = "student", type = XmlStudent.class),
        @XmlElement(name = "student-ext", type = XmlStudentExt.class)
    })
    private XmlStudent[] students;

    public XmlStudents() {
        students = new XmlStudent[0];
    }

    public XmlStudents(Collection<? extends Student> studs) {
        students = studs.stream()
                .map(this::create)
                .toArray(XmlStudent[]::new);
    }

    protected XmlStudent create(Student s) {
        String fn = s.getDirectoryName();
        if (s instanceof Student.PrimaryUnit || s instanceof Student.DateOfBirth) {
            LocalDate dob = null;
            if (s instanceof Student.DateOfBirth) {
                dob = ((Student.DateOfBirth) s).getDateOfBirth();
            }
            UnitId pu = null;
            if (s instanceof Student.PrimaryUnit) {
                pu = ((Student.PrimaryUnit) s).getPrimaryUnit();
            }
            return new XmlStudentExt(s.getStudentId(), fn, dob, pu);
        }
        return new XmlStudent(s.getStudentId(), fn);
    }

    @Override
    public Set<XmlStudent> getStudents() {
        return Arrays.stream(students)
                .collect(Collectors.toSet());
    }
}
