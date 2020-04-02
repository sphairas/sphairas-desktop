/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports.module;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.Exceptions;
import org.thespheres.betula.reports.model.EditableReport;
import org.thespheres.betula.reports.model.EditableReportCollection;

/**
 *
 * @author boris.heithecker
 */
public class ReportsFoldsImpl implements FoldManager {

    private FoldOperation foldOperation;

    @Override
    public void init(FoldOperation operation) {
        this.foldOperation = operation;
    }

    @Override
    public void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = foldOperation.getHierarchy().getComponent().getDocument();
        try {
            EditableReportCollection<? extends EditableReport> ercoll = null;
            if (doc != null) {
                ercoll = (EditableReportCollection<? extends EditableReport>) doc.getProperty(EditableReportCollection.class.getCanonicalName());
            }
            if (ercoll != null) {
                for (EditableReport er : ercoll.getReports()) {
                    Position start = er.getStartPosition();
                    Position end = er.getEndPosition();
                    Fold fold = foldOperation.addToHierarchy(FoldType.TAG, start.getOffset(), end.getOffset(), null, FoldTemplate.DEFAULT, er.getMessage(), er, transaction);  
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
//                transaction.commit();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void removeEmptyNotify(Fold epmtyFold) {
    }

    @Override
    public void removeDamagedNotify(Fold damagedFold) {
    }

    @Override
    public void expandNotify(Fold expandedFold) {
    }

    @Override
    public void release() {
    }

}
