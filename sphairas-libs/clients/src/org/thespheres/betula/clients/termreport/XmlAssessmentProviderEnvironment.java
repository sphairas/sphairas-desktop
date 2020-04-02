/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.termreport;

import org.thespheres.betula.termreport.AssessmentProviderEnvironment;
import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.TermReportActions;

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

    LocalFileProperties getProperties() {
        return getProject().getLookup().lookup(LocalFileProperties.class);
    }

    String getServiceProvider() {
        return getProperties().getProperty("providerURL");
    }

    TermSchedule getTermSchedule() {
        final String termProvider = getProperties().getProperty("termSchedule.providerURL");
        SchemeProvider sp = SchemeProvider.find(termProvider);
        return sp.getScheme(getProperties().getProperty("termSchemeId", TermSchedule.DEFAULT_SCHEME), TermSchedule.class);
    }

    @Override
    public TermReport getTermReport() {
        return context.lookup(TermReport.class);
    }

    void remove() throws IOException {
        TermReportActions ac = getContextLookup().lookup(TermReportActions.class);
        if (ac != null) {
            try {
                ac.removeAssessmentProvider(provider);
            } catch (IllegalArgumentException illex) {
                throw new IOException(illex);
            }
        } else {
            throw new IOException();
        }
    }

}
