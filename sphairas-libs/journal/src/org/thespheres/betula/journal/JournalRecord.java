/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal;

import org.thespheres.betula.util.GradeEntry;
import java.util.List;
import java.util.Map;
import org.thespheres.betula.Student;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 */
public interface JournalRecord {

    public Map<Student, GradeEntry> getStudentEntries();

    public void putStudentEntry(Student s, Grade g, Timestamp t);

    public Listing getListing();

    public void setListing(String text, Timestamp time);

    public Double getWeight();

    public void setWeight(Double weight);

    public List<RecordNote> getNotes();

    public static interface Listing {

        public String getText();

        default public Timestamp getTimestamp() {
            return null;
        }

    }

}
