/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal;

import java.util.List;
import java.util.Map;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 */
//TODO: implements TargetAssessment, use this instead of JournalRecord
public interface JournalRecord2 {

    public Map<StudentId, GradeValue> getStudentEntries();

    public void submit(StudentId s, Grade g, Timestamp t);

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

    public static interface GradeValue {

        public Grade getGrade();

        public Timestamp getTimestamp();

    }
}
