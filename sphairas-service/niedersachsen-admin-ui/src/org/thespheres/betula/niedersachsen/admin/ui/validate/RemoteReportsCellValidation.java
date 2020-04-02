/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import java.awt.Component;
import java.awt.EventQueue;
import javax.swing.JComponent;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.ZeugnisAngabenModel;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.validation.ValidationResult;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.ui.CellIconHighlighterValidation;

/**
 *
 * @author boris.heithecker
 * @param <R>
 * @param <H>
 */
public abstract class RemoteReportsCellValidation<R extends ValidationResult> extends CellIconHighlighterValidation<PrimaryUnitOpenSupport, RemoteReportsModel2, R> {

    protected RemoteReportsCellValidation(String iconBase, Lookup context) {
        super(iconBase, PrimaryUnitOpenSupport.class, context);
    }

    @Override
    protected void init() {
        super.init();
        if (support == null) {
            return;
        }
        final RemoteReportsModel2 model = support.getLookup().lookup(RemoteReportsModel2.class);
        model.getEventBus().register(this);
        initializeValidation(model);
    }

    protected void initializeValidation(final RemoteReportsModel2 model) {
        final ValidationResultSet<RemoteReportsModel2, R> v = createValidation(model);
        setValidation(v);
        //TODO engine start validation now
        change();
    }

//    @Subscribe
//    public void onPropertyChange(PropertyChangeEvent evt) {
//        if (ReportData2.PROP_MARKERS.equals(evt.getPropertyName())) {
//            final VersetzungsValidationImpl vvi = (VersetzungsValidationImpl) getValidationResultSet();
//            if (vvi != null && evt.getSource() instanceof ReportData2) {
//                //TODO move to engine, process only new documents for new term
//                final ReportData2 rd = (ReportData2) evt.getSource();
//                vvi.postRunOneDocument(rd.getTerm(), rd.getStudent());
//            }
//        }
//    }

//    @Subscribe
//    public void on(CollectionChangeEvent cce) {
//        if (RemoteReportsModel2.COLLECTION_TERMS.equals(cce.getCollectionName())) {
//            VersetzungsValidationImpl vvi = (VersetzungsValidationImpl) getValidationResultSet();
//            if (vvi != null) {
//                //TODO move to engine, process only new documents for new term
//                vvi.postRun();
//            }
//        }
//    }

    protected abstract ValidationResultSet<RemoteReportsModel2, R> createValidation(RemoteReportsModel2 model);

    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        int ci = adapter.convertColumnIndexToModel(adapter.column);
        if (ci == 0) {
            Object s = adapter.getValue(ci);
            Term term = findCurrentTerm(adapter);
            if (term != null && s instanceof RemoteStudent) {
                final RemoteStudent student = (RemoteStudent) s;
                return isHighlighted(student, term);
            }
        }
        return false;
    }

    protected abstract boolean isHighlighted(RemoteStudent student, Term term);

    private Term findCurrentTerm(ComponentAdapter adapter) {
        JComponent cmp = adapter.getComponent();
        if (cmp instanceof JXTable && ((JXTable) cmp).getModel() instanceof ZeugnisAngabenModel) {
            ZeugnisAngabenModel zsm = (ZeugnisAngabenModel) ((JXTable) cmp).getModel();
            return zsm.getCurrentTerm();
        }
        return null;
    }

    @Override
    public void onStart(int size, Cancellable cancel) {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void resultAdded(R result) {
        change();
    }

    @Override
    public void resultRemoved(R result) {
        change();
    }

    private void change() {
        EventQueue.invokeLater(this::fireStateChanged);
    }

}
