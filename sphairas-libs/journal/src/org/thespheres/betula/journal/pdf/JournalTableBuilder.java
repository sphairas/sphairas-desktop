/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.pdf;

import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.BlockContainer;
import org.plutext.jaxb.xslfo.BorderCollapseType;
import org.plutext.jaxb.xslfo.BorderStyleType;
import org.plutext.jaxb.xslfo.DisplayAlignType;
import org.plutext.jaxb.xslfo.HyphenateType;
import org.plutext.jaxb.xslfo.TableCell;
import org.plutext.jaxb.xslfo.TableColumn;
import org.plutext.jaxb.xslfo.TableRow;
import org.plutext.jaxb.xslfo.TextAlignType;
import org.thespheres.betula.journal.table.FormatUtil;
import org.thespheres.betula.journal.table.JournalTableModel;
import org.thespheres.betula.listprint.builder.TableBuilder;
import org.thespheres.betula.listprint.builder.Util;

/**
 *
 * @author boris.heithecker
 */
class JournalTableBuilder extends TableBuilder<JournalTableModel> {

    JournalTableBuilder(JournalTableModel data) {
        super(data);
        table.setBorderCollapse(BorderCollapseType.COLLAPSE);
    }

    @Override
    protected TableRow createTableHeaderRow() {
        TableRow tr = super.createTableHeaderRow();
        tr.setHeight("0.5cm");
        return tr;
    }

    @Override
    protected TableColumn createTableColumn(int index) {
        TableColumn ret = super.createTableColumn(index);
        final List<String> cw = ret.getColumnWidth();
        switch (index) {
            case 0:
                cw.set(0, "3.0cm");
                break;
            case 1:
                cw.set(0, "11.0cm");
                break;
            case 2:
                cw.set(0, "0.7cm");
                break;
            case 3:
                cw.set(0, "2.3cm");
                break;
        }
//        ret.getBorderStyle().add(BorderStyleType.SOLID);
//        ret.getBorderWidth().add("1pt");
//        ret.getBorderColor().add("black");
        return ret;
    }

    @Override
    protected TableCell createTableCell(int column, int row) {
        TableCell c = super.createTableCell(column, row);
        c.setDisplayAlign(DisplayAlignType.BEFORE);
        c.getBorderStyle().add(BorderStyleType.SOLID);
        c.getBorderWidth().add("0.5pt");
        c.getBorderColor().add("black");
        return c;
    }

    @Override
    protected TableRow createTableRow(int index) {
        TableRow ret = super.createTableRow(index);
        ret.setBackgroundColor("#ffffff");
//        ret.getBorderStyle().add(BorderStyleType.SOLID);
//        ret.getBorderWidth().add("1pt");
//        ret.getBorderColor().add("black");
        return ret;
    }

    @Override
    protected Block createTableCellBlock(int column, int row) {
        Block b = super.createTableCellBlock(column, row);
        b.setFontSize("9pt");
        b.setPadding("2pt");
        b.setTextAlign(TextAlignType.START);
        b.setHyphenate(HyphenateType.TRUE);
        return b;
    }

    @Override
    protected BlockContainer createTableHeaderCellBlockContainer(int column) {
        BlockContainer bc = new BlockContainer();
        Block b = createTableHeaderCellBlock(column);
        bc.getMarkerOrBlockOrBlockContainer().add(b);
        return bc;
    }

    @Override
    protected Block createTableHeaderCellBlock(int column) {
        final String v = getColumnDisplayName(column);
        Block b = Util.createBlock("0.0cm", "0.0cm", "22cm", "10pt", "#000000", TextAlignType.LEFT);
        b.setPadding("2pt");
        b.getContent().add(v);
        return b;
    }

    @Messages({"JournalTable.column.record=Stunde",
        "JournalTable.column.text=Inhalt",
        "JournalTable.column.weight=F.",
        "JournalTable.column.note=Bemerkung"})
    private String getColumnDisplayName(int column) {
        switch (column) {
            case 0:
                return NbBundle.getMessage(JournalTableBuilder.class, "JournalTable.column.record");
            case 1:
                return NbBundle.getMessage(JournalTableBuilder.class, "JournalTable.column.text");
            case 2:
                return NbBundle.getMessage(JournalTableBuilder.class, "JournalTable.column.weight");
            case 3:
                return NbBundle.getMessage(JournalTableBuilder.class, "JournalTable.column.note");
        }
        return "";
    }

    @Override
    protected String getCellValue(int row, int column) {
        if (column == 2) {
            final Double d = (Double) data.getValueAt(row, column);
            return FormatUtil.WEIGHT_NF.format(d);
        }
        return super.getCellValue(row, column);
    }

}
