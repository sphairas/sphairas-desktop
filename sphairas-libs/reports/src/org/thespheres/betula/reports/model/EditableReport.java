/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.text.Position;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.editor.guards.InteriorSection;
import org.thespheres.betula.reports.ReportsSectionsProvider;

/**
 *
 * @author boris.heithecker
 */
public abstract class EditableReport {

    public static final String PROP_MODIFIED = "modified";
    protected final Report report;
    protected InteriorSection guarded;
    protected String userText;
    protected final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);

    protected EditableReport(Report report) {
        this.report = report;
    }

    public abstract EditableReportCollection getCollection();

    public Report getReport() {
        return report;
    }

    public String getUserText() {
        if (userText == null) {
            userText = StringUtils.trimToEmpty(report.getText()) + ReportsSectionsProvider.NB_ENDOFLINE;
        }
        return userText;
    }

    public Position getStartPosition() {
        return guarded.getStartPosition();
    }

    public Position getEndPosition() {
        return guarded.getEndPosition();
    }

    public Position getBodyStartPosition() {
        return guarded.getBodyStartPosition();
    }

    public Position getBodyEndPosition() {
        return guarded.getBodyEndPosition();
    }

    public abstract String getMessage();

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

}
