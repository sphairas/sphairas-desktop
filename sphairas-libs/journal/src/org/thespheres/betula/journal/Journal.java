/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.thespheres.betula.RecordId;

/**
 *
 * @author boris.heithecker
 * @param <J>
 */
public interface Journal<J extends JournalRecord> {

    public Map<RecordId, J> getRecords();

    public List<RecordNote> getNotes();

    public LocalDate getJournalStart();

    public void setJournalStart(LocalDate ldt);

    public LocalDate getJournalEnd();

    public void setJournalEnd(LocalDate ldt);
}
