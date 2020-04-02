/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import com.google.common.eventbus.EventBus;
import java.text.Collator;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.ui.ConfigurationPanelLookupHint;
import org.thespheres.betula.util.StudentComparator;
import org.thespheres.ical.VCard;

/**
 *
 * @author boris.heithecker
 */
public class RemoteStudent extends AbstractDataItem implements Student, Comparable<RemoteStudent>, ConfigurationPanelLookupHint {

    public static final String REMOTE_STUDENT_HINT = "RemoteStudent";
    private final Collator coll = Collator.getInstance(Locale.GERMAN);
    private final VCardStudent stud;

    RemoteStudent(String provider, StudentId sid, RemoteStudents parent, EventBus events) {
        super(provider, events);
        final VCardStudent s = parent.students.find(sid);
        this.stud = s == null ? new UnresolvedStudent(sid) : s;
    }

    @Override
    public EventBus getEventBus() {
        return events;
    }

    @Override
    public StudentId getStudentId() {
        return stud.getStudentId();
    }

    public VCard getVCard() {
        return stud.getVCard();
    }

    @Override
    public String getDirectoryName() {
        return stud.getDirectoryName();
    }

    @Override
    public String getGivenNames() {
        return stud.getGivenNames();
    }

    @Override
    public String getFirstName() {
        return stud.getFirstName();
    }

    @Override
    public String getSurname() {
        return stud.getSurname();
    }

    @Override
    public String getFullName() {
        return stud.getFullName();
    }

    public String getGender() {
        return stud.getGender();
    }

    public LocalDate getDateOfBirth() {
        return stud.getDateOfBirth();
    }

    public String getBirthplace() {
        return stud.getBirthplace();
    }

    public String getHtmlDirectoryName() {
        return getDirectoryName();
    }

    public int matches(String search) {
        final boolean m = getSearchValue().contains(search.toLowerCase());
        return m ? 1 : 0;
    }

    private String getSearchValue() {
        return stud.getSurname().toLowerCase() + " " + stud.getGivenNames().toLowerCase();
    }

    private AbstractUnitOpenSupport getPreferredOpenSupport() {
        return getClientProperty(AbstractUnitOpenSupport.class
                .getCanonicalName(), AbstractUnitOpenSupport.class
        );
    }

    @Override
    public String getContentType() {
        return REMOTE_STUDENT_HINT;
    }

    @Override
    public String getDisplayName() {
        return getDirectoryName();
    }

    public void openInEditor() {
        if (getPreferredOpenSupport() != null) {
            getPreferredOpenSupport().edit();
        }
    }

    @Override
    public int compareTo(RemoteStudent o) {
        return coll.compare(StudentComparator.sortStringFromDirectoryName(getDirectoryName()), StudentComparator.sortStringFromDirectoryName(o.getDirectoryName()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return 97 * hash + Objects.hashCode(this.stud);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteStudent other = (RemoteStudent) obj;
        return Objects.equals(this.stud, other.stud);
    }

    class UnresolvedStudent extends VCardStudent {

        UnresolvedStudent(final StudentId id) {
            super(id);
        }

        @Override
        public String getDirectoryName() {
            return getFullName();
        }

        @Override
        public String getGivenNames() {
            return getFullName();
        }

        @Override
        public String getSurname() {
            return getFullName();
        }

        @Override
        public String getFullName() {
            return Long.toHexString(student.getId()) + " (" + student.getAuthority() + ")";
        }

    }

}
