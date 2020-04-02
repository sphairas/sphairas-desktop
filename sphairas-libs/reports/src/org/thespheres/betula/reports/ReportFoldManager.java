/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports;

import javax.swing.event.DocumentEvent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.thespheres.betula.reports.module.ReportsFoldsImpl;

/**
 *
 * @author boris.heithecker
 */
public class ReportFoldManager implements FoldManager {

    private final ReportsFoldsImpl impl = new ReportsFoldsImpl();

    public ReportFoldManager() {
    }

    @Override
    public void init(FoldOperation operation) {
        impl.init(operation);
    }

    @Override
    public void initFolds(FoldHierarchyTransaction transaction) {
        impl.initFolds(transaction);
    }

    @Override
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        impl.insertUpdate(evt, transaction);
    }

    @Override
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        impl.removeUpdate(evt, transaction);
    }

    @Override
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        impl.changedUpdate(evt, transaction);
    }

    @Override
    public void removeEmptyNotify(Fold epmtyFold) {
        impl.removeEmptyNotify(epmtyFold);
    }

    @Override
    public void removeDamagedNotify(Fold damagedFold) {
        impl.removeDamagedNotify(damagedFold);
    }

    @Override
    public void expandNotify(Fold expandedFold) {
        impl.expandNotify(expandedFold);
    }

    @Override
    public void release() {
        impl.release();
    }

}
