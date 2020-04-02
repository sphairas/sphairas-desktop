/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.List;
import javax.swing.JComponent;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.ToolTipHighlighter;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.jdesktop.swingx.table.TableColumnExt;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.util.*;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.niedersachsen.admin.ui.ZeugnisAngabenColumn;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.ZeugnisAngabenModel;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.validation.ValidationResultSet.ValidationListener;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = "ZensurenschnittColumn.displayLabel=Schnitt")
class ZensurenschnittColumn extends ZeugnisAngabenColumn {

//    private final AlignmentHighlighter centerHighlighter = new AlignmentHighlighter(SwingConstants.CENTER);
    private ZensurenschnittValidationImpl validation;
    private final Listener listener = new Listener();
    private AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ? extends PluggableTableColumn<RemoteReportsModel2, ReportData2>, ? extends AbstractPluggableTableModel.PluggableColumnFactory> tableModel;
    private int modelIndex;

    ZensurenschnittColumn() {
        super("schnitt", 4500, false, 80);
    }

    @Override
    public void initialize(RemoteReportsModel2 model, Lookup context) {
        super.initialize(model, context);
        validation = ZensurenschnittValidationImpl.create(model);
        if (validation != null) {
            validation.addValidationListener(listener);
//            model.getEventBus().register(listener);
        }
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ZensurenschnittColumn.class, "ZensurenschnittColumn.displayLabel");
    }

    @Override
    public String getColumnValue(ReportData2 il) {
        if (validation != null) {
            return validation.stream()
                    .filter(zsr -> zsr.getDocument().getDocumentId().equals(il.getDocumentId()))
                    .collect(CollectionUtil.singleton())
                    .map(ZensurenschnittResultImpl::message)
                    .orElse(null);
        }
        return null;
    }

    @Override
    public void configureTableColumn(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        tableModel = model;
        modelIndex = col.getModelIndex();
        col.addHighlighter(new ResultToolTip());
    }

    private void update2(final DocumentId d) {
        assert EventQueue.isDispatchThread();
        if (tableModel != null) {
            final List<ReportData2> rows = tableModel.getRows();//
            for (int row = 0; row < rows.size(); row++) {
                if (rows.get(row).getDocumentId().equals(d)) {
                    try {
                        tableModel.updateCellValue(row, modelIndex);
                    } catch (Exception e) {
                        PlatformUtil.getCodeNameBaseLogger(ZensurenschnittColumn.class).log(LogLevel.INFO_WARNING, e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

    class ResultToolTip extends ToolTipHighlighter {

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        private ResultToolTip() {
            setHighlightPredicate(HighlightPredicate.ALWAYS);
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            if (initialized && validation != null) {
                int ri = adapter.convertRowIndexToModel(adapter.row);
                final Term term = findCurrentTerm(adapter);
                if (term != null) {
                    final ReportData2 il = getModel().getStudentsForTerm(term.getScheduledItemId()).get(ri);
                    validation.stream()
                            .filter(zsr -> zsr.getDocument().getDocumentId().equals(il.getDocumentId()))
                            .collect(CollectionUtil.singleton())
                            .map(ZensurenschnittResultImpl::toString)
                            .ifPresent(s -> ((JComponent) component).setToolTipText(s));
                }
            }
            return component;
        }

        private Term findCurrentTerm(ComponentAdapter adapter) {
            JComponent cmp = adapter.getComponent();
            if (cmp instanceof JXTable && ((JXTable) cmp).getModel() instanceof ZeugnisAngabenModel) {
                final ZeugnisAngabenModel zsm = (ZeugnisAngabenModel) ((JXTable) cmp).getModel();
                return zsm.getCurrentTerm();
            }
            return null;
        }

    }

    private class Listener implements ValidationListener<ZensurenschnittResultImpl> {

        @Override
        public void onStart(int size, Cancellable cancel) {
        }

        @Override
        public void onStop() {
        }

        @Override
        public void resultAdded(ZensurenschnittResultImpl result) {
            final DocumentId d = result.getDocument().getDocumentId();
            EventQueue.invokeLater(() -> update2(d));
        }

        @Override
        public void resultRemoved(ZensurenschnittResultImpl result) {
            final DocumentId d = result.getDocument().getDocumentId();
            EventQueue.invokeLater(() -> update2(d));
        }

    }

    @MimeRegistration(mimeType = "application/betula-unit-nds-zeugnis-settings", service = ZeugnisAngabenColumn.Factory.class)
    public static class ColFac extends ZeugnisAngabenColumn.Factory {

        @Override
        public PluggableTableColumn<RemoteReportsModel2, ReportData2> createInstance() {
            return new ZensurenschnittColumn();
        }

    }
}
