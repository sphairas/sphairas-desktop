/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.zgnui;

import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.util.*;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.admin.ui.ZeugnisAngabenColumn;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = "SGLColumn.displayLabel=Schulzweig")
class SGLColumn extends ZeugnisAngabenColumn {
    
    private final StringValue markerStringValue = (java.lang.Object v) -> (v instanceof Marker) ? ((Marker) v).getShortLabel() : null;
    private final AlignmentHighlighter centerHighlighter = new AlignmentHighlighter(SwingConstants.CENTER);
    
    SGLColumn() {
        super("sgl", 4000, false, 40);
    }
    
    @Override
    public void initialize(RemoteReportsModel2 ecal, Lookup context) {
        super.initialize(ecal, context);
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SGLColumn.class, "SGLColumn.displayLabel");
    }
    
    @Override
    public Marker getColumnValue(ReportData2 il) {
        final Marker sgl = il.getRemoteStudent().getClientProperty("sgl", Marker.class);
        if (sgl != null) {
            il.addRuntimeMarker(sgl);
        }
        return sgl;
    }
    
    @Override
    public void configureTableColumn(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        col.setCellRenderer(new DefaultTableRenderer(markerStringValue));
        col.addHighlighter(centerHighlighter);
    }
    
    @MimeRegistration(mimeType = "application/betula-unit-nds-zeugnis-settings", service = ZeugnisAngabenColumn.Factory.class)
    public static class ColFac extends ZeugnisAngabenColumn.Factory {
        
        @Override
        public PluggableTableColumn<RemoteReportsModel2, ReportData2> createInstance() {
            return new SGLColumn();
        }
        
    }
}
