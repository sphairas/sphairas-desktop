/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.awt.EventQueue;
import java.util.Set;
import java.util.WeakHashMap;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbPreferences;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
class HideRTADColumns {

    private final static String PROP_HIDE_IF_EMPTY_FOR_PU = "HideRTADColumns.hide.if.empty.for.primary.unit";
    private final static String PROP_HIDDEN = "rtad.empty.hidden";
    private final WeakHashMap<TableColumnExt, String> columns = new WeakHashMap<>();
    private final TargetsForStudentsModel model;
    private static Boolean hideIfEmptyForPU = true;

    static {
        HiddenColumnAdapter.registerHidingProperty(PROP_HIDDEN);
    }

    HideRTADColumns(TargetsForStudentsModel model) {
        this.model = model;
    }

    //This is a bug in SwingX 1.6.5-1
    void beforeTableStructureChanged() {
        columns.forEach((col, tt) -> {
            try {
                col.setHideable(false);
            } catch (IndexOutOfBoundsException e) {
            }
        });
    }

    void registerColumn(TableColumnExt col, String targetType) {
        columns.putIfAbsent(col, targetType);
    }

    void updateAfterSetCurrentIdentityTargetType() {
        EventQueue.invokeLater(() -> columns
                .forEach(this::update));
    }

    //Called only form TargetsForStudentsModel
    void possiblyUpdateAfterSetGrade(final DocumentId source, final boolean emptyInCurrentTerm) {
        EventQueue.invokeLater(() -> columns.keySet().stream()
                .filter(c -> ((DocumentId) c.getIdentifier()).equals(source))
                .forEach(col -> col.putClientProperty(PROP_HIDDEN, emptyInCurrentTerm)));
    }

    private void update(TableColumnExt col, String tt) {
        if (!model.getCurrentTargetType().equals(tt)) {
            return;
        }
        boolean hidden = false;
        final Term ci = model.getCurrentIndentity();
        if (ci != null) {
            final TermId tid = ci.getScheduledItemId();
//            final int mci = col.getModelIndex();
            final Object id = col.getIdentifier();
            final RemoteTargetAssessmentDocument rtad;
            if (id instanceof DocumentId && (rtad = model.getRemoteTargetAssessmentDocumentForDocumentId((DocumentId) id)) != null) {
                final Set<StudentId> studs = isHideIfEmptyForPU() ? model.getRemoteUnitsModel().getStudentIds() : null;
                hidden = rtad.isEmptyFor(tid, studs);
            }
        }
        col.putClientProperty(PROP_HIDDEN, hidden);
    }

    static boolean isHideIfEmptyForPU() {
        if (hideIfEmptyForPU == null) {
            hideIfEmptyForPU = NbPreferences.forModule(HideRTADColumns.class).getBoolean(PROP_HIDE_IF_EMPTY_FOR_PU, true);
        }
        return hideIfEmptyForPU;
    }
}
