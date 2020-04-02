/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.util.IDUtilities;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class TimeRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    private String student;
    @JsonIgnore
    private transient StudentId studentParsed;
    private FastMessage[] messages;
    private List<Note> notes;
    private String note;
//    @JsonSerialize(using = GradeSerializer.class)
//    private Grade[] grades; 
    private String grade;
    private Long timestamp;

    public TimeRecord() {
    }

    public TimeRecord(StudentId stud) {
        this.student = stud.toString();
    }

    public StudentId getStudent() {
        if (studentParsed == null && student != null) {
            studentParsed = IDUtilities.parseStudentId(student);
        }
        return studentParsed;
    }

    public Grade getGrade() {
        return GradeFactory.find(getDefaultConvention(), grade);
    }

    public void setGrade(Grade grade, Long timestamp) {
        this.grade = grade.getId();
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getNote() {
        return note;
    }

    public List<Note> getNotes() {
        return notes != null ? Collections.unmodifiableList(notes) : Collections.EMPTY_LIST;
    }

    public void addNote(final Note note) {
        if (notes == null) {
            notes = new ArrayList<>();
        }
        notes.add(note);
    }

    protected String getDefaultConvention() {
//        return JournalConfiguration.getInstance().getJournalEntryPreferredConvention();
        return "mitarbeit2";
    }

}
