/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.analytics;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.thespheres.betula.Student;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.CollectionChangeEvent.Type;
import org.thespheres.betula.util.GradeEntry;

/**
 *
 * @author boris.heithecker
 */
//TODO: synchonized...
public class RecordMean {

    private final EditableRecord record;
    private final DescriptiveStatistics values = new DescriptiveStatistics(); //SummaryStatistics ??

    @SuppressWarnings("LeakingThisInConstructor")
    public RecordMean(EditableRecord rec) {
        record = rec;
        record.getEditableJournal().getEventBus().register(this);
        reset();
    }

    private void reset() {
        final Map<Student, GradeEntry> m = record.getRecord().getStudentEntries();
        synchronized (values) {
            values.clear();
            m.keySet().stream()
                    .map(m::get)
                    .map(g -> JournalAnalytics.getInstance().valueOf(g.getGrade()))
                    .filter(Objects::nonNull)
                    .forEach(values::addValue);
        }
    }


    public double getMean() {
        synchronized (values) {
            return values.getMean();
        }
    }

    public double getDeviation() {
        synchronized (values) {
            return values.getStandardDeviation();
        }
    }

    @Subscribe
    public void onModelChange(final CollectionChangeEvent event) {
        final String cn = event.getCollectionName();
        if (EditableJournal.COLLECTION_RECORDS.equals(cn)
                || EditableJournal.COLLECTION_PARTICIPANTS.equals(cn)) {
            if (event.getType().equals(Type.ADD)
                    || event.getType().equals(Type.REMOVE)) {
                reset();
            }
        }
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof EditableRecord
                && (EditableRecord.PROP_GRADE.equals(evt.getPropertyName()) || EditableRecord.PROP_WEIGHT.equals(evt.getPropertyName()))) {
            reset();
        }
    }

}
