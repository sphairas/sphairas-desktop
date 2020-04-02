/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.thespheres.betula.Student;
import org.thespheres.betula.Students;

/**
 *
 * @author boris.heithecker
 */
public class StudentsList extends Students {

    private final SortedSet<Student> students;

    public StudentsList() {
        students = new TreeSet<>(new StudentComparator());
    }

    public StudentsList(Collection<Student> studs) {
        this();
        students.addAll(studs);
    }

    public boolean add(Student s) {
        return students.add(s);
    }

    @Override
    public Set<Student> getStudents() {
        return students;
    }
}
