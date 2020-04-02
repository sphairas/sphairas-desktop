/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.xml.rest;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import org.thespheres.betula.document.Signee;

/**
 *
 * @author boris.heithecker
 */
public class UntisSubstitutionData implements Serializable {

    private static final long serialVersionUID = 1L;
    private Date timestamp;
    private LocalDate date;
    private int untisPeriod;
    private int lessonId;
    private int coupling;
    private String location;
    private Signee teacherSubstituting;
    private String teacherSubstitutingUntisName;
    //1. If no lessonFound, create nonRegularUpdate, include signee in message
    //2. If lessonTeacher, signee -> warn if update required, include warning in message
    private Signee teacherLesson;
    private String teacherLessonUntisName;
    private String text;
    private String[] untisClasses;
    private ViewData flags;
    //            uub.update(id, timestamp, date, untisPeriod, lessonId, coupling, location, substitutingSig, substitutingTr.getName(), message, f);

    public UntisSubstitutionData() {
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getUntisPeriod() {
        return untisPeriod;
    }

    public void setUntisPeriod(int untisPeriod) {
        this.untisPeriod = untisPeriod;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public int getCoupling() {
        return coupling;
    }

    public void setCoupling(int coupling) {
        this.coupling = coupling;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Signee getTeacherSubstituting() {
        return teacherSubstituting;
    }

    public void setTeacherSubstituting(Signee teacherSubstituting) {
        this.teacherSubstituting = teacherSubstituting;
    }

    public String getTeacherSubstitutingUntisName() {
        return teacherSubstitutingUntisName;
    }

    public void setTeacherSubstitutingUntisName(String teacherSubstitutingUntisName) {
        this.teacherSubstitutingUntisName = teacherSubstitutingUntisName;
    }

    public Signee getTeacherLesson() {
        return teacherLesson;
    }

    public void setTeacherLesson(Signee teacherLesson) {
        this.teacherLesson = teacherLesson;
    }

    public String getTeacherLessonUntisName() {
        return teacherLessonUntisName;
    }

    public void setTeacherLessonUntisName(String teacherLessonUntisName) {
        this.teacherLessonUntisName = teacherLessonUntisName;
    }

    public String[] getUntisClasses() {
        return untisClasses;
    }

    public void setUntisClasses(String[] untisClasses) {
        this.untisClasses = untisClasses;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ViewData getFlags() {
        return flags;
    }

    public void setFlags(ViewData flags) {
        this.flags = flags;
    }

    public static final class ViewData implements Serializable {

        private final String message;
        private final String sourceFlags;
        private final Integer bitfeld;
        private final char art;

        public ViewData(String message, Integer bitfeld, char art, String flags) {
            this.message = message;
            this.bitfeld = bitfeld;
            this.art = art;
            this.sourceFlags = flags;
        }

        public String getMessage() {
            return message;
        }

        public Integer getBitfeld() {
            return bitfeld;
        }

        public char getArt() {
            return art;
        }

        public String getSourceFlags() {
            return sourceFlags;
        }

    }
}
