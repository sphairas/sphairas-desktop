/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.analytics;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.FastMath;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 *///TODO: synchonized .....
public class ParticipantMean {

    protected final EditableJournal<?, ?> journal;
    protected final EditableParticipant part;
    private final DescriptiveStatistics values = new DescriptiveStatistics();
    private final DescriptiveStatistics weights = new DescriptiveStatistics();

    @SuppressWarnings("LeakingThisInConstructor")
    public ParticipantMean(EditableJournal cal, EditableParticipant participant) {
        this.journal = cal;
        this.part = participant;
        journal.getEventBus().register(this);
        reset();
    }

    private void reset() {
        synchronized (values) {
            values.clear();
            weights.clear();
            journal.getEditableRecords().stream()
                    .forEach(r -> {
                        final Grade g = r.getGradeAt(part.getIndex());
                        final Double v = JournalAnalytics.getInstance().valueOf(g);
                        if (v != null) {
                            double w = JournalAnalytics.getInstance().calculateRealWeight(r);
                            weights.addValue(w);
                            values.addValue(v);
                        }
                    });
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

    public double getWeightedMean() {
        synchronized (values) {
            final Mean m = new Mean();
            try {

                return m.evaluate(values.getValues(), weights.getValues());
            } catch (MathIllegalArgumentException maillex) {
                System.out.println(part.getDirectoryName() + " " + maillex.getMessage());
                return Double.NaN;
            }
        }
    }

    public double getWeightedDeviation() {
        synchronized (values) {
            final Variance m = new Variance(); //new Variance(false);
            try {
                final double var = m.evaluate(values.getValues(), weights.getValues(), getWeightedMean());
                return FastMath.sqrt(var);
            } catch (MathIllegalArgumentException maillex) {
                System.out.println(part.getDirectoryName() + " " + maillex.getMessage());
                return Double.NaN;
            }
        }
    }

    @Subscribe
    public void onModelChange(CollectionChangeEvent event) {
        final String cn = event.getCollectionName();
        if (EditableJournal.COLLECTION_RECORDS.equals(cn)
                || EditableJournal.COLLECTION_PARTICIPANTS.equals(cn)) {
            reset();
        }
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        final Object s = evt.getSource();
        if (s instanceof EditableJournal
                || s instanceof EditableParticipant
                || s instanceof EditableRecord) {
            reset();
        }
    }

}
