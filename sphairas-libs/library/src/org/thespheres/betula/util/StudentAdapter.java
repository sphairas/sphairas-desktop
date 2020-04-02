/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thespheres.betula.util;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;

/**
 *
 * @author boris.heithecker
 */
public class StudentAdapter extends XmlAdapter<StudentId, Student> {

    @Override
    public Student unmarshal(StudentId v) throws Exception {
        throw new UnmarshalException("Cannot unmarshal student from id with default adapter.");
    }

    @Override
    public StudentId marshal(Student v) throws Exception {
        return v.getStudentId();
    }

}
