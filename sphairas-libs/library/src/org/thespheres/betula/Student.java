/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

import java.time.LocalDate;

/**
 *
 * @author boris.heithecker
 */
public interface Student {

    public String getDirectoryName();

    public String getGivenNames();

    default public String getFirstName() {
        final String given = getGivenNames();
        if (given != null) {
            final String[] sp = given.split(" ");
            return sp[0].trim();
        }
        return null;
    }

    public String getSurname();

    public String getFullName();

    public StudentId getStudentId();

    @Override
    public boolean equals(Object o);

    @Override
    public int hashCode();

    public interface DateOfBirth extends Student {

        public LocalDate getDateOfBirth();
    }

    public interface PrimaryUnit extends Student {

        public UnitId getPrimaryUnit();
    }
}
