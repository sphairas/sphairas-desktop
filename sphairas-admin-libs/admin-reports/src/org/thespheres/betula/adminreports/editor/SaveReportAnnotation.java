/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.editor;

import org.thespheres.betula.adminreports.impl.RemoteEditableReport;
import org.thespheres.betula.adminreports.impl.RemoteReportsModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Objects;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.LineCookie;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author boris.heithecker
 */
public class SaveReportAnnotation extends Annotation implements PropertyChangeListener {

    private final RemoteEditableReport editableReport;

    public SaveReportAnnotation(RemoteEditableReport report) {
        editableReport = report;
    }

    @Override
    public String getAnnotationType() {
        return "betula-remote-reports-save-report";
    }

    @Override
    public String getShortDescription() {
        return "betula-remote-reports-save-report short desc";
    }

    @Override
    protected void notifyDetached(Annotatable fromAnno) {
//        editableReport.getCollection().registerSaveReportAnnotation(this);
    }

    @Override
    protected void notifyAttached(Annotatable toAnno) {
//        editableReport.getCollection().unregisterSaveReportAnnotation(this);
    }

    boolean lineNumberEquals(Line other) {
        if (other != null && getAttachedAnnotatable() instanceof Line) {
            return ((Line) getAttachedAnnotatable()).getLineNumber() == other.getLineNumber();
        }
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RemoteReportsModel.PROP_MODIFIED)) {
            boolean modif = (boolean) evt.getNewValue();
            StyledDocument doc = editableReport.getCollection().getDocument();
            Lookup.Provider lkp = (Lookup.Provider) doc.getProperty(Document.StreamDescriptionProperty);
//            getLineSet().getCurrent(1).
            Line.Set lineSet = lkp.getLookup().lookup(LineCookie.class).getLineSet();
//            NbEditorUtilities.getLine(docLoadingSaving, offset, true)
            boolean original = false;
            Line line = null;
            if (lineSet != null) {
                int offset = editableReport.getStartPosition().getOffset();
                Element lineRoot = (doc instanceof AbstractDocument)
                        ? ((AbstractDocument) doc).getParagraphElement(0).getParentElement()
                        : doc.getDefaultRootElement();
                int lineIndex = lineRoot.getElementIndex(offset);
                line = original ? lineSet.getOriginal(lineIndex) : lineSet.getCurrent(lineIndex);
            }
            if (modif) {
                attach(line);
            } else {
                detach();
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.editableReport);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SaveReportAnnotation other = (SaveReportAnnotation) obj;
        return Objects.equals(this.editableReport, other.editableReport);
    }

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.admin.units.reportsui.SaveReportAction")
    @ActionRegistration(
            displayName = "#SaveReportAction.displayName")
    @ActionReferences({
        @ActionReference(path = "Editors/text/betula-remote-reports/Popup", position = 400),
        @ActionReference(path = "Editors/AnnotationTypes/SaveReportActions", position = 400)})
    @NbBundle.Messages("SaveReportAction.displayName=Bericht speichern")
    public static final class SaveReportAction implements ActionListener {

        private final SaveReportAnnotation annot;

        public SaveReportAction(Annotation context) {
            this.annot = (SaveReportAnnotation) context;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            if (annot != null) {
                try {
                    annot.editableReport.saveReport();
                } catch (IOException ex) {
                }
            }
        }
    }
}
