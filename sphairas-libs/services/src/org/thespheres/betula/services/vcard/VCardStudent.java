/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.vcard;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.ical.CardComponentProperty;
import org.thespheres.ical.VCard;
import org.thespheres.ical.util.VCardHolder;

/**
 *
 * @author boris.heithecker
 */
public class VCardStudent implements Student.DateOfBirth {

    protected final StudentId student;
    private final VCardHolder[] vch = new VCardHolder[]{null};

    public VCardStudent(StudentId id) {
        this.student = id;
    }

    public static StudentId extractStudentId(VCard vc) throws IOException {
        final CardComponentProperty ccp = vc.getAnyProperty("X-STUDENT");
        if (ccp != null) {
            final String sid = ccp.getValue();
            final String auth = ccp.getAnyParameter("x-authority")
                    .orElse(null);
            if (auth != null && sid != null) {
                try {
                    long id = Long.parseLong(sid);
                    return new StudentId(auth, id);
                } catch (NumberFormatException nex) {
                }
            }
        }
        throw new IOException();
    }

    @Override
    public StudentId getStudentId() {
        return student;
    }

    public VCard getVCard() {
        final VCardHolder h;
        synchronized (vch) {
            h = vch[0];
        }
        return h != null ? h.getVCard() : null;
    }

    private Optional<VCardHolder> getVCardHolder() {
        synchronized (vch) {
            return Optional.ofNullable(vch[0]);
        }
    }

    public void setVCard(VCard vCard) {
        synchronized (vch) {
            vch[0] = new VCardHolder(vCard);
        }
    }

    @Override
    public String getDirectoryName() {
        return getVCardHolder()
                .map(VCardHolder::getDirectoryName)
                .orElse(Long.toString(student.getId()));
    }

    @Override
    public String getGivenNames() {
        return getVCardHolder()
                .map(VCardHolder::getGivenNames)
                .orElse(Long.toString(student.getId()));
    }

    @Override
    public String getFirstName() {
        return getVCardHolder()
                .map(VCardHolder::getFirstName)
                .orElse(getDirectoryName());
    }

    @Override
    public String getSurname() {
        return getVCardHolder()
                .map(VCardHolder::getSurname)
                .orElse(getDirectoryName());
    }

    @Override
    public String getFullName() {
        return getVCardHolder()
                .map(VCardHolder::getFullName)
                .orElse(getDirectoryName());
    }

    public String getGender() {
        return getVCardHolder()
                .map(VCardHolder::getGender)
                .orElse("");
    }

    @Override
    public LocalDate getDateOfBirth() {
        return getVCardHolder()
                .map(VCardHolder::getBDay)
                .orElse(null);
    }

    public String getBirthplace() {
        return getVCardHolder()
                .map(VCardHolder::getBirthplace)
                .orElse("");

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.student);
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
        final Student other = (Student) obj;
        return Objects.equals(this.student, other.getStudentId());
    }
}
