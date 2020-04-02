/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.analytics;

import java.time.LocalDate;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.Student;
import org.thespheres.betula.journal.util.PeriodsList;

/**
 *
 * @author boris.heithecker
 */
public class ParticipantMeanTest extends NbTestCase {

    public ParticipantMeanTest(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration config = NbModuleSuite.createConfiguration(ParticipantMeanTest.class)
                .gui(false)
                .enableModules(".*")
                .clusters(".*");
        return config.suite();
    }

    public void testMean() {

            RecordId id2 = new RecordId("test-authority", LocalDate.of(2016, 1, 3), 5);
        Student s = TestStudent.create("Müller", "Hand");
//       PeriodsList.create(null, null);
//        XmlJournal tj = new XmlJournal();
//        tj.setJournalStart(LocalDate.of(2016, 1, 1));
//        tj.setJournalEnd(LocalDate.of(2016, 1, 31));
//        for (int i = 1; i <= 31; i++) {
//            RecordId id = new RecordId("test-authority", LocalDate.of(2016, 1, i), 5);
//            XmlJournalRecord xjr = new XmlJournalRecord();
//            xjr.setText("day" + Integer.toString(i));
//            Grade g = GradeFactory.find("mitarbeit2", "x");
//            xjr.putStudentEntry(s, g, new Timestamp());
//            tj.getRecords().put(id, xjr);
//        }
//
//        EditableJournal ej = new EditableJournalImpl(tj, Lookup.EMPTY);
//        EditableParticipant ep = ej.findParticipant(s.getStudentId());
//        ParticipantMean pm = new ParticipantMean(ej, ep);
//        assertEquals(pm.getWeightedMean(), 0.0d);
    }
}
