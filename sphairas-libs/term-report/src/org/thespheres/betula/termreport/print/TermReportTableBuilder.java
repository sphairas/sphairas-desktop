/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.print;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.BlockContainer;
import org.plutext.jaxb.xslfo.Inline;
import org.plutext.jaxb.xslfo.TableColumn;
import org.plutext.jaxb.xslfo.TextAlignType;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.listprint.builder.RootBuilder;
import org.thespheres.betula.listprint.builder.TableBuilder;
import org.thespheres.betula.listprint.builder.Util;
import org.thespheres.betula.termreport.NumberAssessmentProvider;
import org.thespheres.betula.termreport.NumberAssessmentProvider.ProviderReference;
import org.thespheres.betula.termreport.TableColumnConfiguration;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.TermReportActions;
import org.thespheres.betula.termreport.module.TermReportTableColumn;
import org.thespheres.betula.termreport.module.TermReportTableModel2;
import org.thespheres.betula.ui.AssessmentDecoration;
import org.thespheres.betula.ui.AssessmentDecorationStyle;

/**
 *
 * @author boris.heithecker
 */
class TermReportTableBuilder extends TableBuilder<TermReportTableModel2> {

    private final Lookup context;
    private final List<Footnote> footnotes;
    final NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());

    TermReportTableBuilder(TermReportTableModel2 data, Lookup lookup) {
        super(data);
        this.context = lookup;
        nf.setRoundingMode(RoundingMode.HALF_DOWN);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(1);
        footnotes = context.lookup(TermReport.class).getProviders().stream()
                .filter(NumberAssessmentProvider.class::isInstance)
                .map(NumberAssessmentProvider.class::cast)
                .map(Footnote::new)
                .collect(Collectors.toList());
    }

    void addFootnotes(final RootBuilder rb) {
        for (int i = 0; i < footnotes.size(); i++) {
            final Footnote fn = footnotes.get(i);
            final Block b = Util.createBlock("7pt", TextAlignType.LEFT);
            b.setSpaceBefore("2pt");
            b.setMarginLeft("0.4cm");
            b.getContent().add(formatReference(i) + fn.text);
            rb.addFlow(b);
        }
    }

    String formatReference(int i) {
        return nf.format(i + 1) + ") ";
    }

    @Override
    protected TableColumn createTableColumn(int index) {
        TableColumn ret = super.createTableColumn(index);
        if (index == data.getItemsModel().getProviders().size() + 1) {
            final double size = 26d - (5d + data.getItemsModel().getProviders().size() * 1.5d);
            ret.getColumnWidth().set(0, doubleFormat.format(size) + "cm");  //"5.0cm");
        }
        return ret;
    }

    @Override
    protected Block createTableCellBlock(int column, int row) {
        final Block b = super.createTableCellBlock(column, row);
        final Object d = data.getValueAt(row, column);
        if (d instanceof Grade) {
            final AssessmentDecorationStyle style = AssessmentDecoration.getDefault().getStyle((Grade) d);
            if (style != null) {
                if (style.getForeGround() != null) {
                    b.setColor(style.getForeGround());
                }
                if (style.getFontWeight() != null) {
                    b.setFontWeight(style.getFontWeight());
                }
            }
        }
        if (column == data.getColumnCount() - 1) {
            b.setTextAlign(TextAlignType.LEFT);
        }
        return b;
    }

    @Override
    protected BlockContainer createTableHeaderCellBlockContainer(int column) {
        if (column > 0 && column < data.getColumnCount() - 1) {
            return super.createTableHeaderCellBlockContainer(column);
        }
        BlockContainer bc = new BlockContainer();
        Block b = createTableHeaderCellBlock(column);
        bc.getMarkerOrBlockOrBlockContainer().add(b);
        return bc;
    }

    @Override
    protected Block createTableHeaderCellBlock(int column) {
        int index = column - 1;
        if (index >= 0 && index < data.getItemsModel().getProviders().size()) {
            final Block ret = super.createTableHeaderCellBlock(column);
            String pn = data.getItemsModel().getProviders().get(index).getDisplayName();
            pn = pn.replaceFirst("\\s", "\u00A0");
            ret.getContent().set(0, pn);
            final String pid = context.lookup(TermReport.class).getProviders().get(index).getId();
            for (int i = 0; i < footnotes.size(); i++) {
                final Footnote fn = footnotes.get(i);
                if (fn.id.equals(pid)) {
                    final Inline il = new Inline();
                    il.setFontSize("7");
                    il.setSpaceStart("1pt");
                    il.setBaselineShift("super");
                    final String ref = formatReference(i);
                    il.getContent().add(ref);
                    ret.getContent().add(il);
                }
            }
            return ret;
        }
        String v = null;
        if (column == 0) {
            v = NbBundle.getMessage(TermReportTableColumn.class, "StudentColumn.displayName");
        } else if (index == data.getItemsModel().getProviders().size()) {
            v = NbBundle.getMessage(TermReportTableColumn.class, "NoteColumn.displayName");
        }
        Block b = Util.createBlock("0.0cm", "0.0cm", "22cm", "10pt", "#000000", TextAlignType.LEFT);
        b.setPaddingLeft("2pt");
        b.getContent().add(v);
        return b;
    }

    @Override
    protected String getCellValue(int row, int column) {
        int index = column - 1;
        final Object d = data.getValueAt(row, column);
        if (index >= 0 && index < data.getItemsModel().getProviders().size()) {
            TableColumnConfiguration tcc = data.getItemsModel().getProviders().get(index).getTableColumnConfiguration();
            if (tcc != null) {
                return tcc.getString(d);
            }
        } else if (d instanceof StudentId && column == 0) {
            final StudentId stud = (StudentId) d;
            Student s;
            final Unit unit = context.lookup(TermReportActions.class).getContext().lookup(Unit.class);
            if (unit != null && (s = unit.findStudent(stud)) != null) {
                return s.getDirectoryName();
            }
            return Long.toString(stud.getId());
        }
        return super.getCellValue(row, column);
    }

    class Footnote {

        final String text;
        private final String id;

        Footnote(final NumberAssessmentProvider nap) {
            text = nap.getProviderReferences().stream()
                    .map(this::toString)
                    .collect(Collectors.joining(", "));
            id = nap.getId();
        }

        String toString(final ProviderReference pr) {
            return pr.getReferenced().getDisplayName() + ": " + nf.format(pr.getWeight() * 100d) + "%";
        }
    }
}
