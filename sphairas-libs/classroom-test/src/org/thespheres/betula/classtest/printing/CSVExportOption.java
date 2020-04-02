/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.printing;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringJoiner;
import org.openide.util.NbBundle;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.table2.ClasstestTableModel2;
import org.thespheres.betula.classtest.table2.ClasstestTableSupport;
import org.thespheres.betula.ui.util.ExportToCSVOption;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
public class CSVExportOption implements ExportToCSVOption {

    private final ClasstestTableSupport env;
    private final NumberFormat nf = DecimalFormat.getInstance(Locale.getDefault());

    public CSVExportOption(ClasstestTableSupport env) {
        this.env = env;
    }

    @Override
    public byte[] getCSV() throws IOException {
        final EditableClassroomTest<?, ?, ?> etest = env.getLookup().lookup(EditableClassroomTest.class);
        StringBuilder sb = new StringBuilder();
        StringJoiner header = new StringJoiner(";", "", "\n");
        header.add(NbBundle.getMessage(ClasstestTableModel2.class, "classtest.table.name.col"));
        etest.getEditableProblems().stream()
                .forEach(ep -> header.add(ep.getDisplayName()));
        header.add(NbBundle.getMessage(ClasstestTableModel2.class, "classtest.table.sum.col"))
                .add(NbBundle.getMessage(ClasstestTableModel2.class, "classtest.table.grade.col"))
                .add(NbBundle.getMessage(ClasstestTableModel2.class, "classtest.table.note.col"));
        sb.append(header.toString());
        etest.getEditableStudents().stream()
                .forEach(es -> {
                    StringJoiner sj = new StringJoiner(";", "", "\n");
                    sj.add(es.getStudent().getDirectoryName());
                    etest.getEditableProblems().stream()
                            .map(ep -> es.getStudentScores().get(ep.getId()))
                            .forEach(d -> sj.add(d != null ? nf.format(d) : ""));
                    sj.add(nf.format(es.getStudentScores().sum()));
                    Grade g = es.getStudentScores().getGrade();
                    sj.add(g != null ? g.getLongLabel() : "");
                    final String note = es.getStudentScores().getNote();
                    sj.add(note != null ? note : "");
                    sb.append(sj.toString());
                });
        StringJoiner sj = new StringJoiner(";", "", "\n");
        sj.add("");
        etest.getEditableProblems().stream()
                .map(ep -> ep.getMean())
                .forEach(d -> sj.add(d != null ? nf.format(d) : ""));
        final double am = etest.getAllStudentsScoresMean();
        sj.add(nf.format(am));
        final Double gm = etest.getGradesMean();
        sj.add(gm != null ? nf.format(gm) : "");
        sj.add("");
        sb.append(sj.toString());
        return sb.toString().getBytes("utf-8");
    }

    @Override
    public String createFileNameHint() throws IOException {
        return UIUtilities.findDisplayName(env.getDataObject());
    }

}
