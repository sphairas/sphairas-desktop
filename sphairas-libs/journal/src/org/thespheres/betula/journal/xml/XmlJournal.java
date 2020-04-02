/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.xml;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.journal.RecordNote;
import org.thespheres.betula.util.LocalDateAdapter;
import org.thespheres.betula.journal.Journal;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "journal", namespace = "http://www.thespheres.org/xsd/betula/journal.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlJournal implements Journal<XmlJournalRecord> {

    @XmlAttribute
    private Date end;
    @XmlAttribute
    private Date begin;
    @XmlElement(name = "journal-start")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate journalStart;
    @XmlElement(name = "journal-end")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate journalEnd;
    @XmlElement(required = true, name = "journal-records")
    @XmlJavaTypeAdapter(value = RecordSetAdapter.class)
    private final Map<RecordId, XmlJournalRecord> records = new HashMap<>();
    @XmlElementWrapper(name = "notes")
    @XmlElementRef
    private final List<RecordNote> notes = new ArrayList<>();

    @Override
    public Map<RecordId, XmlJournalRecord> getRecords() {
        return records;
    }

    @Override
    public LocalDate getJournalStart() {
        return journalStart;
    }

    @Override
    public void setJournalStart(LocalDate begin) {
        this.journalStart = begin;
    }

    @Override
    public LocalDate getJournalEnd() {
        return journalEnd;
    }

    @Override
    public void setJournalEnd(LocalDate end) {
        this.journalEnd = end;
    }

    @Override
    public List<RecordNote> getNotes() {
        return notes;
    }

    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if (begin != null) {
            journalStart = begin.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (end != null) {
            journalEnd = begin.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }
}
