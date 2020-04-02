/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.UnmarshalException;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;

/**
 *
 * @author boris.heithecker
 */
public class StudentUnmarshallAdapter extends StudentAdapter {

    private final Unit unit;
    private final XmlStudents xmlst;

    public StudentUnmarshallAdapter(Unit unit, XmlStudents s) {
        //unit can be null if i.e. we unmarshall from classtest template with no unit in project lookup
        this.unit = unit;
        this.xmlst = s;
    }

    @Override
    public Student unmarshal(StudentId v) throws Exception {
        if (v == null) {
            throw new UnmarshalException("StudentId cannot be null.");
        }
        Student ret = null;
        if (unit != null) {
            try {
                ret = unit.findStudent(v);
            } catch (Exception e) {
                String msg = "An error occurred unmarshalling student " + v.getId() + " [" + v.getAuthority() + "]: " + e.getMessage();
                Logger.getLogger(StudentUnmarshallAdapter.class.getCanonicalName()).log(Level.SEVERE, msg);
            }
        }
        if (ret == null && xmlst != null) {
            ret = xmlst.find(v);
        }
        if (ret != null) {
            return ret;
        }
        return new UnresolveStudent(v);
    }

    private final class UnresolveStudent implements Student {

        private final StudentId id;
        private final String text;

        private UnresolveStudent(StudentId id) {
            this.id = id;
            this.text = Long.toString(id.getId());
        }

        @Override
        public StudentId getStudentId() {
            return id;
        }

        @Override
        public String getDirectoryName() {
            return text;
        }

        @Override
        public String getGivenNames() {
            return text;
        }

        @Override
        public String getFirstName() {
            return text;
        }

        @Override
        public String getSurname() {
            return text;
        }

        @Override
        public String getFullName() {
            return text;
        }

        public String getPrimaryUnitId() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!Student.class.isAssignableFrom(obj.getClass())) {
                return false;
            }
            final Student other = (Student) obj;
            return this.id.equals(other.getStudentId());
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }
    }
}
