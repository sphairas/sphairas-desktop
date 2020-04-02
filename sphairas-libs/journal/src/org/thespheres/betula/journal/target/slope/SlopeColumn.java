/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target.slope;

import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.target.slope.CumulativeSum.PointAt2;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
@Messages({"SlopeColumn.displayName=Kurve"})
public class SlopeColumn extends PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant> {

    SlopeColumn() {
        super("slope", 5100, false, 55);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SlopeColumn.class, "SlopeColumn.displayName");
    }

    @Override
    public Object getColumnValue(final EditableParticipant il) {
//        CumulativeSum.create(il.getEditableJournal(), il.getIndex());
//        HarmonicCurveParams p = HarmonicCurveParams.create(il.getEditableJournal(), il.getIndex());
//        return HarmonicCurveParams.NF.format(p.getPeriod());
        return adjusted(il);
    }

    private double adjusted(final EditableParticipant il) {
        final Map<RecordId, CumulativeSum.PointAt2> m = CumulativeSum.create2(il.getEditableJournal(), il.getIndex());
        for (final Map.Entry<RecordId, CumulativeSum.PointAt2> e : m.entrySet()) {
            final EditableRecord<?> er = il.getEditableJournal().findRecord(e.getKey());
            final ChiSquareSnapshot csq = new ChiSquareSnapshot(er);
            final Grade dg = JournalConfiguration.getInstance().getJournalDefaultGrade();
            while (true) {
                final ChiSquareSnapshot.Result test = csq.test(0.05d);
                double weight = test.deviation;
                double userWeight = er.getWeight();
                double updateWeight = (double) weight * userWeight;
                e.getValue().setWeight(updateWeight);
                if (test.test) {
                    long dSize = csq.sizeMap.get(dg);
                    double diff = dSize - test.expected.get(dg);
                    if (diff > 1.0d) {
                        csq.sizeMap.compute(dg, (g, s) -> s - 1);
                        continue;
                    } else if (diff < -1.0d) {
                        csq.sizeMap.compute(dg, (g, s) -> s + 1);
                    }
                }
                break;
            }
        }
        final int n = m.size();
        final double weightsSum = m.values().stream()
                .mapToDouble(p -> p.getWeight())
                .sum();
//        double cSum = 0d;
//        System.out.println(ep.getSurname() + " " + wdev);
        for (final PointAt2 p : m.values()) {
            p.setAdjustedValue(n, weightsSum);
        }
        return m.values().stream()
                .mapToDouble(pa -> pa.getAdjustedValue())
                .sum() / n;
    }

//    @MimeRegistration(mimeType = "text/betula-journal-file-target-table", service = PluggableTableColumn.Factory.class)
    public static class Fac extends Factory<PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant>> {

        @Override
        public PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant> createInstance() {
            return new SlopeColumn();
        }

    }
}
