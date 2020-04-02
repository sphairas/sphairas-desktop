/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.model;

import org.thespheres.betula.termreport.AssessmentProviderEnvironment;
import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.thespheres.betula.termreport.AssessmentProvider;

/**
 *
 * @author boris.heithecker
 */
final class XmlAssessmentProviderEnvironment implements AssessmentProviderEnvironment {

    private final Lookup context;
    private final AssessmentProvider provider;

    XmlAssessmentProviderEnvironment(AssessmentProvider ref, Lookup context) {
        this.context = context;
        this.provider = ref;
    }

    @Override
    public AssessmentProvider getProvider() {
        return provider;
    }

    @Override
    public Lookup getContextLookup() {
        return context;
    }

    DataObject getDataObject() {
        return context.lookup(DataObject.class);
    }

    Project getProject() {
        return FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
    }

    @Override
    public XmlTermReportImpl getTermReport() {
        return context.lookup(XmlTermReportImpl.class);
    }

    void remove() throws IOException {
        XmlTermReportImpl tr = getTermReport();
        if (tr != null) {
            try {
                tr.removeAssessmentProvider(provider);
            } catch (IllegalArgumentException illex) {
                throw new IOException(illex);
            }
        } else {
            throw new IOException();
        }
    }

}
