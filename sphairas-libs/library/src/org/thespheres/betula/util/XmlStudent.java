package org.thespheres.betula.util;

import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;

/**
 *
 * @author Boris Heithecker
 */
@XmlRootElement(name = "student", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlStudent implements Student {

    @XmlElement(name = "student")
    protected StudentId student;
    @XmlElement(name = "directory-name")
    protected String fullname;

    public XmlStudent() {
    }

    public XmlStudent(StudentId id, String dir) {
        this.fullname = dir;
        this.student = id;
    }

    @Override
    public StudentId getStudentId() {
        return student;
    }

    @Override
    public String getDirectoryName() {
        return fullname;
    }

    @Override
    public String getGivenNames() {
        String[] sp = getDirectoryName().split(",");
        return sp[1].trim();
    }

    @Override
    public String getSurname() {
        String[] sp = getDirectoryName().split(",");
        return sp[0].trim();
    }

    @Override
    public String getFirstName() {
        String[] sp = getGivenNames().split(" ");
        return sp[0].trim();
    }

    @Override
    public String getFullName() {
        return getGivenNames().trim() + " " + getSurname().trim();
    }

    @Override
    public String toString() {
        String ret = getDirectoryName();
//        if (!"".equals(getPrimaryUnit())) {
//            ret += " (" + getPrimaryUnit().getId() + ")";
//        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Student)) {
            return false;
        }
        Student s = (Student) o;
        return s.getStudentId().equals(this.student);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.fullname != null ? this.fullname.hashCode() : 0);
        return hash;
    }

    @XmlRootElement(name = "student-ext", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlStudentExt extends XmlStudent implements Student.DateOfBirth, Student.PrimaryUnit {

        @XmlElement(name = "primary-unit")
        private UnitId primaryUnit;
        @XmlElement(name = "date-of-birth")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate dateOfBirth;

        public XmlStudentExt() {
        }

        public XmlStudentExt(StudentId id, String fullname, LocalDate dateOfBirth, UnitId primaryUnit) {
            super(id, fullname);
            this.primaryUnit = primaryUnit;
            this.dateOfBirth = dateOfBirth;
        }

        @Override
        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        @Override
        public UnitId getPrimaryUnit() {
            return primaryUnit;
        }
    }

}
