/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.time.LocalDate;
import java.util.Objects;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;

/**
 *
 * @author boris.heithecker
 */
public class TransientStudent implements Student.DateOfBirth, Student.PrimaryUnit {

    private final StudentId id;
    private final String given;
    private final String surname;
    private LocalDate dateOfBirth;
    private UnitId unit;

    public TransientStudent(String surname, String given, StudentId id) {
        this.id = id;
        this.surname = surname;
        this.given = given;
    }

    @Override
    public String getDirectoryName() {
        if (names()) {
            return surname + ", " + given;
        } else {
            return null;
        }
    }

    @Override
    public String getGivenNames() {
        if (names()) {
            return given;
        } else {
            return null;
        }
    }

    @Override
    public String getSurname() {
        if (names()) {
            return surname;
        } else {
            return null;
        }
    }

    @Override
    public String getFirstName() {
        if (names()) {
            return getGivenNames().trim().split(" ")[0];
        } else {
            return null;
        }
    }

    @Override
    public String getFullName() {
        if (names()) {
            return getGivenNames().trim() + " " + getSurname().trim();
        } else {
            return null;
        }
    }

    private boolean names() {
        return this.given != null && this.surname != null;
    }

    @Override
    public StudentId getStudentId() {
        return id;
    }

    @Override
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate d) {
        this.dateOfBirth = d;
    }

    @Override
    public UnitId getPrimaryUnit() {
        return unit;
    }

    public void setPrimaryUnit(UnitId unit) {
        this.unit = unit;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TransientStudent other = (TransientStudent) obj;
        return Objects.equals(this.id, other.id);
    }
}
