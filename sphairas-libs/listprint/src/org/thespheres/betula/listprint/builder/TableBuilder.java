/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.listprint.builder;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import org.openide.util.NbBundle;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.BlockContainer;
import org.plutext.jaxb.xslfo.BorderStyleType;
import org.plutext.jaxb.xslfo.DisplayAlignType;
import org.plutext.jaxb.xslfo.Leader;
import org.plutext.jaxb.xslfo.LeaderPatternType;
import org.plutext.jaxb.xslfo.RuleStyleType;
import org.plutext.jaxb.xslfo.Table;
import org.plutext.jaxb.xslfo.TableBody;
import org.plutext.jaxb.xslfo.TableCell;
import org.plutext.jaxb.xslfo.TableColumn;
import org.plutext.jaxb.xslfo.TableFooter;
import org.plutext.jaxb.xslfo.TableHeader;
import org.plutext.jaxb.xslfo.TableRow;
import org.plutext.jaxb.xslfo.TextAlignLastType;
import org.plutext.jaxb.xslfo.TextAlignType;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
@NbBundle.Messages({"table.end.version.message={0} ({1})"})
public class TableBuilder<I extends TableItem> {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("EE., d. MMMM yyyy HH':'mm");
    protected final I data;
    protected final Table table = new Table();
    protected final DecimalFormat doubleFormat;

    public TableBuilder(I data) {
        this.data = data;
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        this.doubleFormat = new DecimalFormat("#0.0", dfs);
    }

    public static void addVersion(final RootBuilder rb, final String path) throws MissingResourceException {
        final String time = LocalDateTime.now().format(DTF);
        final String msg = NbBundle.getMessage(TableBuilder.class, "table.end.version.message", path, time);
        final Block b = Util.createBlock("7pt", TextAlignType.RIGHT);
        b.setSpaceBefore("2pt");
        b.setSpaceAfter("0.1cm");
        b.getContent().add(msg);
        final Block r = new Block();
        r.setTextAlignLast(TextAlignLastType.JUSTIFY);
        r.setLineHeight("2pt");
        r.setSpaceAfter("0.1cm");
        final Leader l = new Leader();
        l.setLeaderPattern(LeaderPatternType.RULE);
        l.setColor("#ff9a33");
        l.setRuleThickness("2.0pt");
        l.setRuleStyle(RuleStyleType.SOLID);
        r.getContent().add(l);
        rb.addFlow(b);
        rb.addFlow(r);
    }

    public Table build() {
        for (int i = 0; i < data.getColumnCount(); i++) {
            TableColumn tc = createTableColumn(i);
            table.getTableColumn().add(tc);
        }
        TableHeader header = createTableHeader();
        table.setTableHeader(header);
        createTableBodies().stream().forEach(b -> table.getTableBody().add(b));
        TableFooter footer = createTableFooter();
        table.setTableFooter(footer);
        return table;
    }

    protected TableHeader createTableHeader() {
        TableHeader header = new TableHeader();
        TableRow row = createTableHeaderRow();
        header.getTableRow().add(row);
        TableRow ruler = new TableRow();
        ruler.setBackgroundColor("#ffffff");
        ruler.setHeight("0.3cm");
        TableCell rc = new TableCell();
        rc.setNumberColumnsSpanned(Integer.toString(data.getColumnCount()));
        Block b = Util.createRule("1pt", "1.0pt");
        rc.getMarkerOrBlockOrBlockContainer().add(b);
        ruler.getTableCell().add(rc);
        header.getTableRow().add(ruler);
        header.getBorderStyle().add(BorderStyleType.NONE);
        return header;
    }

    protected TableFooter createTableFooter() {
        return null;
    }

    protected TableRow createTableHeaderRow() {
        TableRow row = new TableRow();
        row.setHeight("2.2cm");
        row.getBorderStyle().add(BorderStyleType.NONE);
        for (int i = 0; i < data.getColumnCount(); i++) {
            TableCell tc = createTableHeaderCell(i);
            row.getTableCell().add(tc);
        }
        return row;
    }

    protected TableCell createTableHeaderCell(int i) {
        TableCell ret = new TableCell();
        ret.setNumberColumnsSpanned("1");
        ret.setLineHeight("0.85");
        ret.setDisplayAlign(DisplayAlignType.AFTER);
        BlockContainer bc = createTableHeaderCellBlockContainer(i);
        ret.getMarkerOrBlockOrBlockContainer().add(bc);
        return ret;
    }

    protected BlockContainer createTableHeaderCellBlockContainer(int column) {
        BlockContainer bc = new BlockContainer();
        bc.setReferenceOrientation("90");
        bc.setDisplayAlign(DisplayAlignType.CENTER);
        Block b = createTableHeaderCellBlock(column);
        bc.getMarkerOrBlockOrBlockContainer().add(b);
        return bc;
    }

    protected Block createTableHeaderCellBlock(int column) {
        Block b = new Block();
        b.setPaddingBefore("1pt");
        b.setPaddingAfter("2pt");
        b.getContent().add(data.getColumnName(column));
        return b;
    }

    protected TableColumn createTableColumn(int index) {
        TableColumn ret = new TableColumn();
        if (index == 0 || index > data.getColumnCount() - 1) {
            ret.getColumnWidth().add("5.0cm");
        } else {
            ret.getColumnWidth().add("1.5cm");
        }
        return ret;
    }

    private List<TableBody> createTableBodies() {
        TableBody ret = new TableBody();
        for (int i = 0; i < data.getRowCount(); i++) {
            TableRow tr = createTableRow(i);
            ret.getTableRow().add(tr);
        }
        return Collections.singletonList(ret);
    }

    protected TableRow createTableRow(int index) {
        TableRow ret = new TableRow();
        ret.setHeight("0.5cm");
        ret.setDisplayAlign(DisplayAlignType.AFTER);
        if (index % 2 == 0) {
            ret.setBackgroundColor("#FFE0C0");
        } else {
            ret.setBackgroundColor("#ffffff");
        }
        for (int i = 0; i < data.getColumnCount(); i++) {
            TableCell tc = createTableCell(i, index);
            ret.getTableCell().add(tc);
        }
        return ret;
    }

    protected TableCell createTableCell(int column, int row) {
        TableCell tc = new TableCell();
        tc.setNumberColumnsSpanned("1");
        Block b = createTableCellBlock(column, row);
        b.getContent().add(getCellValue(row, column));
        tc.getMarkerOrBlockOrBlockContainer().add(b);
        return tc;
    }

    protected Block createTableCellBlock(int column, int row) {
        final TextAlignType alignment = column == 0 ? TextAlignType.LEFT : TextAlignType.CENTER;
        Block b = Util.createBlock("0.0cm", "0.0cm", "0.0cm", "10pt", "#000000", alignment);
        b.setPaddingLeft("2pt");
        return b;
    }

    protected String getCellValue(int row, int column) {
        final Object v = data.getValueAt(row, column);
        return v != null ? v.toString() : "";
    }

}
