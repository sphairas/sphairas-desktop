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
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.scheme.Terms;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.adminreports.BerichteImport;
import org.thespheres.betula.niedersachsen.berichte.ui.NdsBerichteImport;
import org.thespheres.betula.niedersachsen.berichte.ui.NdsTextKursImportItem;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.niedersachsen.berichte.ui.impl.CreateTermReportEntry")
@ActionRegistration(displayName = "#CreateTermReportEntry.displayName", asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 105000, separatorBefore = 100000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({
    "CreateTermReportEntry.displayName=Neuer Fach-Bericht"
})
public class CreateTermReportEntry implements ActionListener {

    private final TermId termId;
    private final NdsTextKursImportItem textkurs;
    private RemoteTargetAssessmentDocument remoteTarget;
    private WebServiceProvider provider;
    private NdsBerichteImport support;
    private ConfigurableImportTarget importTarget;

    public CreateTermReportEntry(TargetAssessmentSelectionProvider sp) throws IOException {
        termId = sp.getTerm();
        final RemoteStudent rs = sp.getLookup().lookup(RemoteStudent.class);
        final StudentId studentId = rs != null ? rs.getStudentId() : null;
        final RemoteTargetAssessmentDocument k = sp.getLookup().lookup(RemoteTargetAssessmentDocument.class);
        final DocumentId target = k != null ? k.getDocumentId() : null;
        final PrimaryUnitOpenSupport uos = sp.getLookup().lookup(PrimaryUnitOpenSupport.class);
        if (uos == null) {
            throw new IOException();
        }
        final DocumentsModel dm = uos.findDocumentsModel();
        String tid = null;
        if (target != null) {
            String[] el = target.getId().split("-");
            if (el.length > 2 && el[el.length - 1].equalsIgnoreCase(dm.getModelPrimarySuffix())) {
                String fachEl = el[1];
                if (fachEl.equals("englisch") || fachEl.equals("deutsch") || fachEl.equals("mathematik")) {
                    tid = target.getId();
                }
            }
        }
        support = (NdsBerichteImport) BerichteImport.find();
        if (termId == null || studentId == null || tid == null || uos == null) {
            throw new IOException();
        }
        provider = uos.findWebServiceProvider();
        importTarget = ConfigurableImportTarget.find(provider.getInfo().getURL());
        final String idbase = tid.replace("-" + dm.getModelPrimarySuffix(), "");
        this.remoteTarget = k;
        final DocumentId t = new DocumentId(importTarget.getAuthority(), idbase + "-" + support.getBerichteSuffix(), DocumentId.Version.LATEST);
        final UnitId unit = dm.convertToUnitId(t);
        textkurs = null; //support.createTextKurs(unit, t, new StudentId[]{studentId});
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Term term = Terms.forTermId(termId);
        textkurs.initializeMarkers(remoteTarget.markers());
        textkurs.setSignee(remoteTarget.getSignee("entitled.signee"));
        textkurs.setDeleteDate(remoteTarget.getDateOfExpiry());
        NdsTextKursImportItem[] l = new NdsTextKursImportItem[]{textkurs};
        final TextKursUpdater<NdsTextKursImportItem> run = support.createUpdater(l, provider, term, importTarget);
        run.run();
    }

}
