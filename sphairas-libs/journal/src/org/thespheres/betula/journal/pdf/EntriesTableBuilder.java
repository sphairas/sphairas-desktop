/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.pdf;

import java.util.List;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.BlockContainer;
import org.plutext.jaxb.xslfo.BorderCollapseType;
import org.plutext.jaxb.xslfo.PageBreakAfterType;
import org.plutext.jaxb.xslfo.TableCell;
import org.plutext.jaxb.xslfo.TableColumn;
import org.plutext.jaxb.xslfo.TableRow;
import org.thespheres.betula.listprint.builder.TableBuilder;

/**
 *
 * @author boris.heithecker
 */
class EntriesTableBuilder extends TableBuilder<EntriesTableItem> {

    EntriesTableBuilder(EntriesTableItem data) {
        super(data);
        table.setBorderCollapse(BorderCollapseType.COLLAPSE);
        table.setPageBreakBefore(PageBreakAfterType.RIGHT);
    }

//    @Override
//    protected TableRow createTableHeaderRow() {
//        TableRow tr = super.createTableHeaderRow();
//        tr.setHeight("0.5cm");
//        return tr;
//    }
    @Override
    protected TableColumn createTableColumn(int index) {
        TableColumn ret = super.createTableColumn(index);
        final List<String> cw = ret.getColumnWidth();
        if (index == 0) {
            cw.set(0, "40mm");
        } else {
            cw.set(0, "4.5mm");
        }
//        ret.getBorderStyle().add(BorderStyleType.SOLID);
//        ret.getBorderWidth().add("1pt");
//        ret.getBorderColor().add("black");
        return ret;
    }

    @Override
    protected TableCell createTableCell(int column, int row) {
        TableCell c = super.createTableCell(column, row);
//        c.setDisplayAlign(DisplayAlignType.BEFORE);
//        c.getBorderStyle().add(BorderStyleType.SOLID);
//        c.getBorderWidth().add("0.5pt");
//        c.getBorderColor().add("black");
        return c;
    }

    @Override
    protected Block createTableCellBlock(int column, int row) {
        Block b = super.createTableCellBlock(column, row);
        b.setFontSize("8pt");
        return b;
    }

    @Override
    protected TableRow createTableRow(int index) {
        TableRow ret = super.createTableRow(index);
        ret.setHeight("4mm");
//        ret.getBorderStyle().add(BorderStyleType.SOLID);
//        ret.getBorderWidth().add("1pt");
//        ret.getBorderColor().add("black");
        return ret;
    }

    @Override
    protected BlockContainer createTableHeaderCellBlockContainer(int column) {
        if (column > 0) {
            return super.createTableHeaderCellBlockContainer(column);
        }
        BlockContainer bc = new BlockContainer();
        Block b = createTableHeaderCellBlock(column);
        bc.getMarkerOrBlockOrBlockContainer().add(b);
        return bc;
    }

    @Override
    protected Block createTableHeaderCellBlock(int column) {
        Block b = super.createTableHeaderCellBlock(column);
        b.setFontSize("8pt");
        b.setKeepTogetherWithinLine("always");
        return b;
    }
//    @Messages({"JournalTable.column.record=Stunde",
//        "JournalTable.column.text=Inhalt",
//        "JournalTable.column.weight=F.",
//        "JournalTable.column.note=Bemerkung"})
//    private String getColumnDisplayName(int column) {
//        switch (column) {
//            case 0:
//                return NbBundle.getMessage(EntriesTableBuilder.class, "JournalTable.column.record");
//            case 1:
//                return NbBundle.getMessage(EntriesTableBuilder.class, "JournalTable.column.text");
//            case 2:
//                return NbBundle.getMessage(EntriesTableBuilder.class, "JournalTable.column.weight");
//            case 3:
//                return NbBundle.getMessage(EntriesTableBuilder.class, "JournalTable.column.note");
//        }
//        return "";
//    }
//    @Override
//    protected String getCellValue(int row, int column) {
////        if (column == 2) {
////            final Double d = (Double) data.getValueAt(row, column);
////            return FormatUtil.WEIGHT_NF.format(d);
////        }
//        return super.getCellValue(row, column);
//    }
}
