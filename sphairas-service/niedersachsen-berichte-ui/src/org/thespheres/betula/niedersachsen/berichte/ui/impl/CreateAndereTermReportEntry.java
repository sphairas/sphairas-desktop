/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.berichte.ui.impl;

import org.thespheres.betula.adminreports.TextKursUpdater;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.adminreports.BerichteImport;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.niedersachsen.FoerderungsBerichte;
import org.thespheres.betula.niedersachsen.berichte.ui.NdsBerichteImport;
import org.thespheres.betula.niedersachsen.berichte.ui.NdsTextKursImportItem;
import org.thespheres.betula.services.implementation.naming.Naming;
import org.thespheres.betula.services.scheme.Terms;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.niedersachsen.berichte.ui.impl.CreateAndereTermReportEntry")
@ActionRegistration(displayName = "#CreateAndereTermReportEntry.display", asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 105100, separatorBefore = 100000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({
    "CreateAndereTermReportEntry.display=Neuer Andere-Bericht"
})
public class CreateAndereTermReportEntry implements ActionListener {

    final Marker other = MarkerFactory.find(FoerderungsBerichte.CONVENTION_NAME, "andere", null);
    private final TermId termId;
    private final NdsTextKursImportItem textkurs;
    private final NamingResolver.Result nr;
    private final WebServiceProvider provider;
    private ConfigurableImportTarget importTarget;
    private final NdsBerichteImport support;
    private final StudentId student;

    public CreateAndereTermReportEntry(TargetAssessmentSelectionProvider sp) throws IOException {
        termId = sp.getTerm();
        final RemoteStudent rs = sp.getLookup().lookup(RemoteStudent.class);
        student = rs != null ? rs.getStudentId() : null;
        final PrimaryUnitOpenSupport uos = sp.getLookup().lookup(PrimaryUnitOpenSupport.class);
        String idbase = null;
        if (uos != null) {
            idbase = uos.getUnitId().getId().replace("klasse", "andere");
        }
        support = (NdsBerichteImport) BerichteImport.find();
        if (termId == null || student == null || idbase == null || support == null) {
            throw new IOException();
        }
        assert uos != null;
        final DocumentsModel dm = uos.findDocumentsModel();
        provider = uos.findWebServiceProvider();
        importTarget = ConfigurableImportTarget.find(provider.getInfo().getURL());
        nr = uos.findNamingResolverResult();
        DocumentId t = new DocumentId(importTarget.getAuthority(), idbase + "-" + support.getBerichteSuffix(), DocumentId.Version.LATEST);
        final UnitId unit = dm.convertToUnitId(t);
        textkurs = support.createTextKurs(unit, t);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Term term = Terms.forTermId(termId);
        int s = Integer.parseInt(nr.getResolvedElement(Naming.STUFE));
        int dJahr = Integer.parseInt(nr.getResolvedElement(Naming.START_JAHR));
        int jahr = (Integer) term.getParameter(org.thespheres.betula.niedersachsen.NdsTerms.JAHR);
        int stufe = jahr - dJahr + s;
        LocalDate dDate = ImportUtil.calculateDeleteDate(stufe, 5, Month.JULY);
        textkurs.setDeleteDate(dDate);
        textkurs.setOtherMarker(other);
        textkurs.setPreferredSectionConventionName(null);
        final Timestamp timestamp = new Timestamp(term.getBegin());
        final String empty = NbBundle.getMessage(CreateBerichteTermReportEntriesAction.class, "CreateBerichteTermReportEntriesAction.empty.value");
        textkurs.submit(student, term.getScheduledItemId(), null, empty, timestamp);
        final NdsTextKursImportItem[] l = new NdsTextKursImportItem[]{textkurs};
        final TextKursUpdater run = support.createUpdater(l, provider, term, importTarget);
        provider.getDefaultRequestProcessor().post(run);
    }

}
