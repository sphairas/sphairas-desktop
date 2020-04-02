/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports.untis;

import java.util.Optional;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns.DefaultCheckBoxColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn.Factory.Registration;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.utilities.AbstractLink;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides.AbstractItemListener;
import org.thespheres.betula.xmlimport.utilities.ColumnProperty;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SGLFilterNegColumn.columnName=Neg."})
public class SGLFilterNegColumn extends DefaultCheckBoxColumn<ImportedLesson, UntisImportConfiguration, UntisImportData, ImportTableModel<ImportedLesson, UntisImportData>> {

    private AbstractSourceOverrides<ImportedLesson, ? extends AbstractItemListener, ? extends AbstractLink<String>, String, ?, ?> overrides;
    private UntisImportConfiguration target;

    private SGLFilterNegColumn() {
        super("sglfilter-neg", 272);
    }

    @Override
    public void initialize(UntisImportConfiguration configuration, UntisImportData wizard) {
        super.initialize(configuration, wizard);
        this.target = configuration;
        Object p = wizard.getProperty(AbstractSourceOverrides.USER_SOURCE_OVERRIDES);
        if (p instanceof AbstractSourceOverrides) {
            this.overrides = (AbstractSourceOverrides<ImportedLesson, ?, ? extends AbstractLink<String>, String, ?, ?>) p;
        }
    }

    @Override
    public Boolean getColumnValue(ImportedLesson il) {
        if (target != null) {
            final UntisSGLFilter filter = il.getFilter("sglfilter", () -> new UntisSGLFilter(il, target));
            return filter.isNegate();
        }
        return null;
    }

    @Override
    protected void initialize(ImportedLesson il) {
        super.initialize(il);
        if (overrides != null) {
            Optional<? extends AbstractItemListener> o = overrides.findListener(il);
            if (o.isPresent()) {
                AbstractSourceOverrides.AbstractItemListener ll = o.get();
                if (ll.getTargetLink() != null) {
                    final AbstractLink l = ll.getTargetLink();
                    ColumnProperty cp = l.getNonDefaultProperty("sglfilter");
                    if (cp instanceof UntisSGLFilterOverride) {
                        Marker m = ((UntisSGLFilterOverride) cp).getMarker();
                        boolean neg = ((UntisSGLFilterOverride) cp).isNegate();
                        if (target != null) {
                            UntisSGLFilter filter = il.getFilter("sglfilter", () -> new UntisSGLFilter(il, target));
                            filter.setFilterMarkers(new Marker[]{m});
                            filter.setNegate(neg);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean setColumnValue(ImportedLesson il, Object value) {
        final boolean v = (boolean) value;
        if (overrides != null) {
            Optional<? extends AbstractItemListener> o = overrides.findListener(il);
            if (o.isPresent()) {
                AbstractSourceOverrides.AbstractItemListener ll = o.get();
                if (ll.getTargetLink() != null) {
                    final AbstractLink l = ll.getTargetLink();
                    l.removeNonDefaultProperty("sglfilter");
                    if (target != null) {
                        UntisSGLFilter filter = il.getFilter("sglfilter", () -> new UntisSGLFilter(il, target));
                        filter.setNegate(v);
                        l.setNonDefaultProperty(new UntisSGLFilterOverride(filter));
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SGLFilterNegColumn.class, "SGLFilterColumn.columnName");
    }

    @Registration(component = "UntisCreateDocumentsVisualPanel")
    public static final class Factory extends ImportTableColumn.Factory {

        @Override
        public ImportTableColumn createInstance() {
            return new SGLFilterNegColumn();
        }

    }
}
