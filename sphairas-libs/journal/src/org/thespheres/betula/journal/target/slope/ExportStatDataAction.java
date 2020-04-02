/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target.slope;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.ui.util.ExportToCSVUtil;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.journal.target.slope.ExportStatDataAction")
@ActionRegistration(
        displayName = "#CTL_ExportStatDataAction")
@ActionReference(path = "Loaders/text/betula-journal-participant-context/Actions", position = 4000)
@Messages("CTL_ExportStatDataAction=ExportStatData")
public final class ExportStatDataAction implements ActionListener {

    private final List<EditableParticipant> context;
    private final static NumberFormat DF = NumberFormat.getNumberInstance(Locale.getDefault());

    static {
        DF.setMaximumFractionDigits(1);
        DF.setMinimumFractionDigits(1);
        DF.setGroupingUsed(false);
    }

    public ExportStatDataAction(List<EditableParticipant> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (EditableParticipant ep : context) {
            try {
//                List<CumulativeSum.PointAt> l = CumulativeSum.create(ep.getEditableJournal(), ep.getIndex());
//                byte[] csv = getCSVImpl(ep, l);
                final Map<RecordId, CumulativeSum.PointAt2> l = CumulativeSum.create2(ep.getEditableJournal(), ep.getIndex());
                byte[] csv = getCSVImpl2(ep, l);
                ExportToCSVUtil.writeFile(csv, ep.getDirectoryName());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @NbBundle.Messages({"ExportStatDataAction.export.csv.name=Name"})
    private byte[] getCSVImpl2(EditableParticipant part, Map<RecordId, CumulativeSum.PointAt2> l) throws IOException {
        final StringBuilder sb = new StringBuilder();

        final StringJoiner header = new StringJoiner(";", "", "\n");
        header.add(NbBundle.getMessage(ExportStatDataAction.class, "ExportStatDataAction.export.csv.name"));
        header.add("Note");
        header.add("Koorigierter Wert");
        header.add("Gewicht");
        header.add("CUSUM");
        header.add("CUSUM/Gewicht");
        header.add("X2 (95%)");
        sb.append(header.toString());

        for (final EditableRecord<?> record : part.getEditableJournal().getEditableRecords()) {
            final CumulativeSum.PointAt2 pa = l.get(record.getRecordId());
            if (pa == null) {
                continue;
            }
            final StringJoiner line = new StringJoiner(";", "", "\n");
            line.add(record.getDate().toString());
            line.add(pa.getGrade().getShortLabel());
            line.add(DF.format(pa.getAdjustedValue()));
            line.add(DF.format(pa.getWeight()));
            line.add(DF.format(pa.getcSum()));
            line.add(DF.format(pa.ratio));
            line.add(Boolean.toString(pa.chisq));
            sb.append(line.toString());
        }
        return sb.toString().getBytes("utf-8");
    }

    private byte[] getCSVImpl(EditableParticipant part, List<CumulativeSum.PointAt> l) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final StringJoiner header = new StringJoiner(";", "", "\n");
        header.add(NbBundle.getMessage(ExportStatDataAction.class, "ExportStatDataAction.export.csv.name"));
        l.stream()
                .map(p -> part.getEditableJournal().getEditableRecords().get(p.getIndex()))
                .forEachOrdered(er -> header.add(er.getDate().toString()));
        sb.append(header.toString());

        final StringJoiner line1 = new StringJoiner(";", "", "\n");
        line1.add("Note");
        l.stream()
                //                .map(p -> part.getEditableJournal().getEditableRecords().get(p.getIndex()))
                .forEachOrdered(er -> line1.add(er.getGrade().getShortLabel()));
        sb.append(line1.toString());

        final StringJoiner line2 = new StringJoiner(";", "", "\n");
        line2.add("Korrigierter Wert");
        l.stream()
                //                .map(p -> part.getEditableJournal().getEditableRecords().get(p.getIndex()))
                .forEachOrdered(er -> line2.add(DF.format(er.getAdjustedValue())));
        sb.append(line2.toString());

        final StringJoiner line2a = new StringJoiner(";", "", "\n");
        line2a.add("Gewicht");
        l.stream()
                //                .map(p -> part.getEditableJournal().getEditableRecords().get(p.getIndex()))
                .forEachOrdered(er -> line2a.add(DF.format(er.getWeight())));
        sb.append(line2a.toString());

        final StringJoiner line3 = new StringJoiner(";", "", "\n");
        line3.add("CUSUM");
        l.stream()
                //                .map(p -> part.getEditableJournal().getEditableRecords().get(p.getIndex()))
                .forEachOrdered(er -> line3.add(DF.format(er.getcSum())));
        sb.append(line3.toString());

        final StringJoiner line4 = new StringJoiner(";", "", "\n");
        line4.add("CUSUM/Gewicht");
        l.stream()
                //                .map(p -> part.getEditableJournal().getEditableRecords().get(p.getIndex()))
                .forEachOrdered(er -> line4.add(DF.format(er.ratio)));
        sb.append(line4.toString());

        final StringJoiner line5 = new StringJoiner(";", "", "\n");
        line5.add("X2 (95%)");
        l.stream()
                //                .map(p -> part.getEditableJournal().getEditableRecords().get(p.getIndex()))
                .forEachOrdered(er -> line5.add(Boolean.toString(er.chisq)));
        sb.append(line5.toString());

        return sb.toString().getBytes("utf-8");
    }
}
