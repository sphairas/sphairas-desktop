/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.util.IDUtilities;

/**
 *
 * @author boris.heithecker
 */
//@View
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class TimeDoc2 implements Serializable { //, Comparable<TimeDocument> {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    @JsonProperty("_id")
    private String id;
    @JsonProperty("_rev")
    private String rev;
    private String end;
    private int period;
    private int status; //bitweise....
    private String location;
    private String display;//Alternative, in UntisCalendarFacadeImpl.addEntityPropertiesToComponent: add parameter:x-resolved-unitid=true, if SUMMARY is generated from unitid
    private String display2; //Vertretung, Klausur etc....
    private FastMessage[] messages;
    private TimeRecord[] records;
    private Note[] notes;
    private String note;
    private String target;
    private Journal journal; //editable
    @JsonIgnore
    private transient DocumentId targetParsed;

    public TimeDoc2() {
    }

    public TimeDoc2(TargetDoc target, LocalDateTime start, LocalDateTime end, int period, int status, List<TimeRecord> records) {
        this.id = createId(start);
        this.target = target.getId();
        this.end = DTF.format(end);
        this.period = period;
        this.status = status;
        this.records = records.toArray(new TimeRecord[records.size()]);
    }

    public static String createId(LocalDateTime start) {
        return "tm_" + DTF.format(start);
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getStart() {
        return LocalDateTime.parse(id.substring(3), DTF);
    }

    //TODO : not ready
    public RecordId getRecordId() {
//        LocalDateTime date = new Date(getStart()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return new RecordId("www.meine-schule.de/tstest", getStart());
    }

    public LocalDateTime getEnd() {
        return LocalDateTime.parse(end, DTF);
    }

    public void setEnd(LocalDateTime dtEnd) {
        end = DTF.format(dtEnd);
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public DocumentId getTarget() {
        if (targetParsed == null && target != null) {
            targetParsed = IDUtilities.parseDocumentId(target);
        }
        return targetParsed;
    }

    public void setTarget(TargetDoc tdoc) {
        targetParsed = null;
        target = tdoc.getId();
    }

    public TimeRecord[] getRecords() {
        return records;
    }

    public void addRecords(List<TimeRecord> add) {
        records = Stream.concat(Arrays.stream(getRecords()), add.stream())
                .toArray(TimeRecord[]::new);
    }

    public String getDisplaySub() {
        return display2;
    }

    public void setDisplaySub(String display2) {
        this.display2 = display2;
    }

    public FastMessage[] getMessages() {
        return messages;
    }

    public void setMessages(FastMessage[] messages) {
        this.messages = messages;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(String text, Long timestamp) {
        this.journal = new Journal(text, timestamp);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final TimeDoc2 other = (TimeDoc2) obj;
        return Objects.equals(this.id, other.id);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class Journal implements Serializable {

        private String text;
        private Long timestamp;

        public Journal() {
        }

        public Journal(String text, Long timestamp) {
            this.text = text;
            this.timestamp = timestamp;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

    }
}
