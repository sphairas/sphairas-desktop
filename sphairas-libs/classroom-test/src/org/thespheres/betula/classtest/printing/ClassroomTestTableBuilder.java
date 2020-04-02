/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.printing;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import org.openide.util.NbBundle;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.BlockContainer;
import org.plutext.jaxb.xslfo.TableColumn;
import org.plutext.jaxb.xslfo.TextAlignType;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.classtest.table2.ClasstestColumn;
import org.thespheres.betula.classtest.table2.ClasstestTableModel2;
import org.thespheres.betula.listprint.builder.TableBuilder;
import org.thespheres.betula.listprint.builder.Util;

/**
 *
 * @author boris.heithecker
 */
class ClassroomTestTableBuilder extends TableBuilder<ClasstestTableModel2> {

    private final NumberFormat nf = DecimalFormat.getInstance(Locale.getDefault());

    ClassroomTestTableBuilder(ClasstestTableModel2 data) {
        super(data);
        nf.setMaximumFractionDigits(2);
    }

    @Override
    protected TableColumn createTableColumn(int index) {
        TableColumn ret = super.createTableColumn(index);
        final List<String> cw = ret.getColumnWidth();
        if (index == 0 || index > data.getColumnCount() - 3) {
            cw.set(0, "40mm");
        } else {
            cw.set(0, "8mm");
        }
        return ret;
    }

    @Override
    protected Block createTableCellBlock(int column, int row) {
        Block b = super.createTableCellBlock(column, row);
        if (column > data.getColumnCount() - 3) {
            b.setTextAlign(TextAlignType.START);
        }
        return b;
    }

    @Override
    protected BlockContainer createTableHeaderCellBlockContainer(int column) {
        if (column > 0 && column < data.getColumnCount() - 2) {
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
        if (index >= 0 && index < data.getItemsModel().getEditableProblems().size()) {
            Block ret = super.createTableHeaderCellBlock(column);
            String pn = data.getItemsModel().getEditableProblems().get(index).getDisplayName();
            pn = pn.replaceAll("\\s", "\u00A0");
            ret.getContent().set(0, pn);
            return ret;
        }
        String v = null;
        Block b = Util.createBlock("0.0cm", "0.0cm", "22cm", "10pt", "#000000", TextAlignType.LEFT);
        b.setPaddingLeft("2pt");
        if (column == 0) {
            v = NbBundle.getMessage(ClasstestColumn.StudentColumn.class, "StudentColumn.displayName");
        } else if (index == data.getItemsModel().getEditableProblems().size()) {
            v = NbBundle.getMessage(ClasstestColumn.NoteColumn.class, "ScoreSumColumn.displayName");
            b.setPaddingBefore("1pt");
            b.setPaddingAfter("2pt");
        } else if (index == data.getItemsModel().getEditableProblems().size() + 1) {
            v = NbBundle.getMessage(ClasstestColumn.NoteColumn.class, "GradesColumn.displayName");
        } else if (index == data.getItemsModel().getEditableProblems().size() + 2) {
            v = NbBundle.getMessage(ClasstestColumn.NoteColumn.class, "NoteColumn.displayName");
        }
        b.getContent().add(v);
        return b;
    }

    @Override
    protected String getCellValue(int row, int column) {
        final Object v = data.getValueAt(row, column);
        if (v instanceof Grade) {
            return ((Grade) v).getLongLabel();
        } else if (v instanceof Double) {
            return nf.format(v);
        } else if (v instanceof EditableStudent) {
            return ((EditableStudent) v).getStudent().getDirectoryName();
        } else {
            return v != null ? v.toString() : "";
        }
    }
}
