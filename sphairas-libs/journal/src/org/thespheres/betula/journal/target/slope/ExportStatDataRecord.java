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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Student;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.journal.analytics.JournalAnalytics;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.ui.util.ExportToCSVUtil;
import org.thespheres.betula.util.GradeEntry;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.journal.target.slope.ExportStatDataRecord")
@ActionRegistration(
        displayName = "#CTL_ExportStatDataRecord")
@ActionReference(path = "Loaders/text/betula-journal-record-context/Actions", position = 4000)
@Messages("CTL_ExportStatDataRecord=Stat. Datenexport")
public final class ExportStatDataRecord implements ActionListener {

    private final List<EditableRecord> context;
    private final static NumberFormat DF = NumberFormat.getNumberInstance(Locale.getDefault());

    static {
        DF.setMaximumFractionDigits(1);
        DF.setMinimumFractionDigits(1);
        DF.setGroupingUsed(false);
    }

    public ExportStatDataRecord(List<EditableRecord> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (EditableRecord ep : context) {
            try {
                byte[] csv = getCSVImpl(ep);
                ExportToCSVUtil.writeFile(csv, ep.getDate().toString());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private byte[] getCSVImpl(EditableRecord<?> rec) throws IOException {
        final Map<Student, GradeEntry> m = rec.getRecord().getStudentEntries();
        final Map<Grade, Long> sizeMap = m.values().stream()
                .collect(Collectors.groupingBy(GradeEntry::getGrade, Collectors.counting()));

        final Grade[] grades = Optional.ofNullable(JournalConfiguration.getInstance().getJournalEntryPreferredConvention())
                .map(GradeFactory::findConvention)
                .map(c -> Arrays.asList(c.getAllGradesReverseOrder()))
                .orElse((List<Grade>) Collections.EMPTY_LIST)
                .stream()
                .filter(g -> JournalAnalytics.getInstance().valueOf(g) != null)
                .toArray(Grade[]::new);

        final long[] vv = Arrays.stream(grades)
                .mapToLong(de -> sizeMap.getOrDefault(de, 0l))
                .toArray();

        final long n = Arrays.stream(vv)
                .sum();

        final NormalDistribution pdf = new NormalDistribution(rec.getGradesMean().getMean(), rec.getGradesMean().getDeviation());
        final double[] expected = new double[grades.length];
        for (int i = 0; i < expected.length; i++) {
            final double[] bounds = ChiSquareSnapshot.bounds(grades, i);
            expected[i] = pdf.probability(bounds[0], bounds[1]) * n;
        }

        final double expSum = Arrays.stream(expected)
                .sum();

        final StringBuilder sb = new StringBuilder();
        
        final StringJoiner header = new StringJoiner(";", "", "\n");
        header.add("Note");
        header.add("Anzahl");
        header.add("Erwartet");
        sb.append(header.toString());

        for (int i = 0; i < grades.length; i++) {
            final StringJoiner line1 = new StringJoiner(";", "", "\n");
            line1.add(grades[i].getLongLabel());
            line1.add(Long.toString(vv[i]));
            line1.add(DF.format(expected[i]));
            sb.append(line1.toString());
        }

        final StringJoiner footer = new StringJoiner(";", "", "\n");
        footer.add("Summe");
        footer.add(DF.format(n));
        footer.add(DF.format(expSum));
        sb.append(footer.toString());

        return sb.toString().getBytes("utf-8");
    }

}
