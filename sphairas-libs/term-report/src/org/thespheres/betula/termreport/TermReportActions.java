/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

import java.awt.dnd.DropTargetListener;
import org.openide.util.Lookup;
import org.thespheres.betula.services.LocalFileProperties;

/**
 *
 * @author boris.heithecker
 * @param <X>
 */
public abstract class TermReportActions<X extends TermReport> implements DropTargetListener {

    protected final X report;

    protected TermReportActions(X report) {
        this.report = report;
    }

    public X getTermReport() {
        return report;
    }

    public abstract Lookup getContext();

    public abstract LocalFileProperties getProperties();

    public abstract void addAssessmentProvider(AssessmentProvider prov);

    public abstract void removeAssessmentProvider(AssessmentProvider prov);
}
