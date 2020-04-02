/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;

//We need this to for parallel stream initialization of RTAD
//Calling GradeFactory --> service loader fails in parallel stream (?)
public final class RemoteGradeEntry {

    //TODO: entry timestamps!!
    private String convention;
    private String gradeId;
    private Grade grade;
    private long serverTime;
    private final TermId term;
    private final StudentId student;

    private RemoteGradeEntry(TermId term, StudentId student) {
        this.term = term;
        this.student = student;
    }

    public RemoteGradeEntry(String convention, String id, long ts, TermId term, StudentId student) {
        this(term, student);
        if (convention == null || id == null) {
            throw new IllegalArgumentException("Convention and gradeId cannot be null.");
        }
        this.serverTime = ts;
        this.convention = convention;
        this.gradeId = id;
    }

    public RemoteGradeEntry(Grade g, Timestamp ts, TermId term, StudentId student) {
        this(term, student);
        setServerTime(ts);
        setGrade(g);
    }

    public TermId getTermId() {
        return term;
    }

    public StudentId getStudent() {
        return student;
    }

    private void setGrade(Grade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("Grade cannot be null.");
        }
        this.grade = grade;
    }

    public boolean isUnconfirmed() {
        return this.serverTime == -1;
    }

    private void setServerTime(Timestamp ts) {
        this.serverTime = ts == null ? -1 : ts.getValue().getTime();
    }

    final boolean update(Grade grade, Timestamp ts) {
        boolean ret = !Objects.equals(grade, getGrade());
        boolean wasUnconfirmed = isUnconfirmed() && ts != null;
        setServerTime(ts);
        if (ret) {
            setGrade(grade);
        }
        return wasUnconfirmed || ret;
    }

    @Messages({"RemoteGradeEntry.message.noGradeFound=No convention or grade found: {0}"})
    public synchronized Grade getGrade() {
        if (grade == null && gradeId != null) {
            //Legacy
            if (convention.equals("kgs.ersatzeintrag")) {
                convention = "niedersachsen.ersatzeintrag";
            }
            Grade g = GradeFactory.find(convention, gradeId);//TODO: AbstractGrade if null
            if (g == null) {
                g = new AbstractGrade(convention, gradeId);
                String msg = NbBundle.getMessage(RemoteGradeEntry.class, "RemoteGradeEntry.message.noGradeFound", g.toString());
                Logger.getLogger(RemoteGradeEntry.class.getName()).log(Level.CONFIG, msg);
            }
            this.gradeId = null;
            this.convention = null;
            setGrade(g);
        }
        return grade;
    }

    public Date getTime() {
        if (!isUnconfirmed()) {
            return new Date(serverTime);
        }
        return null;
    }
}
