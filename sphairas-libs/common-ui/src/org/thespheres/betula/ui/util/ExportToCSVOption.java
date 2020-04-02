/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringJoiner;
import org.jdesktop.swingx.JXTable;
import org.thespheres.betula.ui.util.PluggableTableColumn.IndexedColumn;

/**
 *
 * @author boris.heithecker
 */
public interface ExportToCSVOption {

    public byte[] getCSV() throws IOException;

    public String createFileNameHint() throws IOException;

    public static byte[] toCSV(JXTable table) {
        AbstractPluggableTableModel<?, ?, ?, ?> aptm;
        try {
            aptm = (AbstractPluggableTableModel<?, ?, ?, ?>) table.getModel();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(e);
        }

        StringBuilder sb = new StringBuilder();
        StringJoiner header = new StringJoiner(";", "", "\n");
        for (int i = 0; i < aptm.getColumnCount(); i++) {
            AbstractPluggableTableModel.ColumnIndex ci = aptm.getColumnsAt(i);
            String h;//TODO IndexColumn
            final PluggableTableColumn col = ci.getColumn();
            if (col instanceof IndexedColumn) {
                h = ((IndexedColumn<?, ?>) col).getDisplayName(ci.getIndexWithinColGroup());
            } else {
                h = col.getDisplayName();
            }
            header.add(h);
        }
        sb.append(header.toString());
        for (int i = 0; i < aptm.getRowCount(); i++) {
            StringJoiner sj = new StringJoiner(";", "", "\n");
            for (int j = 0; j < aptm.getColumnCount(); j++) {
                String v = table.getStringAt(i, j);
                sj.add(v);
            }
            sb.append(sj.toString());
        }
        try {
            return sb.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException();
        }
    }
}
