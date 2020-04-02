/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.analytics;

import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;

/**
 *
 * @author boris.heithecker
 */
public class TestStudent implements Student {

    private final String sn;
    private final String gn;
    private final StudentId id;
    private final static Long[] C = new Long[]{1l};

    private TestStudent(String sn, String gn, StudentId id) {
        this.sn = sn;
        this.gn = gn;
        this.id = id;
    }

    public static TestStudent create(String sn, String gn) {
        StudentId id;
        synchronized (C) {
            id = new StudentId("test-authority", C[0]++);
        }
        return new TestStudent(sn, gn, id);
    }

    @Override
    public String getDirectoryName() {
        return sn + ", " + gn;
    }

    @Override
    public String getGivenNames() {
        return gn;
    }

    @Override
    public String getFirstName() {
        return gn;
    }

    @Override
    public String getSurname() {
        return sn;
    }

    @Override
    public String getFullName() {
        return gn + " " + sn;
    }

    @Override
    public StudentId getStudentId() {
        return id;
    }

}
