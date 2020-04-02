/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.berichte.ui.impl;

import org.thespheres.betula.adminreports.TextKursUpdater;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.logging.Level;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.adminreports.BerichteImport;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.niedersachsen.FoerderungsBerichte;
import org.thespheres.betula.niedersachsen.berichte.ui.NdsBerichteImport;
import org.thespheres.betula.niedersachsen.berichte.ui.NdsTextKursImportItem;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.implementation.naming.Naming;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.ui.util.MultiContextAction;
import org.thespheres.betula.ui.util.MultiContextSensitiveAction;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.niedersachsen.berichte.ui.impl.CreateBerichteTermReportEntriesAction")
@ActionRegistration(displayName = "#CreateBerichteTermReportEntriesAction.display", asynchronous = true, lazy = false,
        iconBase = "org/thespheres/betula/niedersachsen/berichte/ui/resources/layer-shape-text.png")
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-students/Actions", position = 7120, separatorBefore = 7000)
})
@NbBundle.Messages({"CreateBerichteTermReportEntriesAction.display=Berichte anlegen",
    "CreateBerichteTermReportEntriesAction.display.context=Berichte anlegen für {0}",
    "CreateBerichteTermReportEntriesAction.empty.value=leer"})
public class CreateBerichteTermReportEntriesAction extends MultiContextAction {

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public CreateBerichteTermReportEntriesAction() {
        super(RemoteStudent.class, RemoteUnitsModel.class);
        putValue("iconBase", "org/thespheres/betula/classtest/resources/betulastud16.png");
    }

    @Override
    protected MultiContextSensitiveAction createMultiContextSensitiveAction() {
        return new ActionRunner();
    }

    static class ActionRunner extends MultiContextSensitiveAction {

        final static Marker OTHER = MarkerFactory.find(FoerderungsBerichte.CONVENTION_NAME, "andere", null);

        @Override
        protected String getName(final boolean enabled) {
            if (enabled) {
                final RemoteUnitsModel rum = this.currentContext.lookup(RemoteUnitsModel.class);
                final AbstractUnitOpenSupport uos = rum.getUnitOpenSupport();
                try {
                    final Term term = uos.findTermSchedule().getTerm(Lookup.getDefault().lookup(WorkingDate.class).getCurrentWorkingDate());
                    return NbBundle.getMessage(CreateBerichteTermReportEntriesAction.class, "CreateBerichteTermReportEntriesAction.display.context", term.getDisplayName());
                } catch (IOException | MissingResourceException ex) {
                }
            }
            return NbBundle.getMessage(CreateBerichteTermReportEntriesAction.class, "CreateBerichteTermReportEntriesAction.display");
        }

        @Override
        public void actionPerformed(final ActionEvent e, final Lookup context) {
            final RemoteUnitsModel rum = context.lookup(RemoteUnitsModel.class);
            final RemoteStudent student = context.lookup(RemoteStudent.class);
            final AbstractUnitOpenSupport uos = rum.getUnitOpenSupport();
            if (!(uos instanceof PrimaryUnitOpenSupport)) {
                return;
            }
            try {
                doPerformAction((PrimaryUnitOpenSupport) uos, student);
            } catch (IOException | NoProviderException | NumberFormatException ex) {
                PlatformUtil.getCodeNameBaseLogger(CreateBerichteTermReportEntriesAction.class).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }

        public void doPerformAction(final PrimaryUnitOpenSupport uos, final RemoteStudent student) throws IOException, NoProviderException, NumberFormatException {
            final Term term = uos.findTermSchedule().getTerm(Lookup.getDefault().lookup(WorkingDate.class).getCurrentWorkingDate());
            final TermId termId = term.getScheduledItemId();
            final NdsBerichteImport support = (NdsBerichteImport) BerichteImport.find();
            if (termId == null || student == null || support == null) {
                throw new IOException();
            }
            final DocumentsModel dm = uos.findDocumentsModel();
            final WebServiceProvider provider = uos.findWebServiceProvider();
//            final ConfigurableImportTarget.Factory fac = ConfigurableImportTarget.Factory.find(provider.getInfo().getURL());
//            final ConfigurableImportTarget importTarget = fac.createInstance();
            final NamingResolver.Result nr = uos.findNamingResolverResult();
            final String idbase = uos.getUnitId().getId().replace("klasse", "andere");
            final DocumentId target = new DocumentId(uos.getUnitId().getAuthority(), idbase + "-" + support.getBerichteSuffix(), DocumentId.Version.LATEST);
            final UnitId unit = dm.convertToUnitId(target);

            final MarkerConvention sections = Optional.ofNullable(uos.findBetulaProjectProperties().getProperty("berichte.convention"))
                    .map(MarkerFactory::findConvention)
                    .orElse(null);
            final NdsTextKursImportItem l = createTextKurse(sections, unit, target, student, nr, term, support);
            final TextKursUpdater run = support.createUpdater(new NdsTextKursImportItem[]{l}, provider, term, null);
            provider.getDefaultRequestProcessor().post(run);
        }

        private static NdsTextKursImportItem createTextKurse(final MarkerConvention sections, final UnitId unit, final DocumentId t, final RemoteStudent student, final NamingResolver.Result nr, final Term term, final NdsBerichteImport support) {
            final NdsTextKursImportItem textkurs = support.createTextKurs(unit, t);
            final LocalDate dDate = findDeleteDate(nr, term);
            textkurs.setDeleteDate(dDate);
            textkurs.setOtherMarker(OTHER);
            final Timestamp timestamp = new Timestamp(term.getBegin());
            final String empty = NbBundle.getMessage(CreateBerichteTermReportEntriesAction.class, "CreateBerichteTermReportEntriesAction.empty.value");
            if (sections == null) {
                textkurs.setPreferredSectionConventionName(null);
                textkurs.submit(student.getStudentId(), term.getScheduledItemId(), null, empty, timestamp);
            } else {
                textkurs.setPreferredSectionConventionName(sections.getName());
                Arrays.stream(sections.getAllMarkers())
                        .forEach(m -> textkurs.submit(student.getStudentId(), term.getScheduledItemId(), m, empty, timestamp));
            }
            return textkurs;
        }

        private static LocalDate findDeleteDate(final NamingResolver.Result nr, final Term term) {
            int s = Integer.parseInt(nr.getResolvedElement(Naming.STUFE));
            int dJahr = Integer.parseInt(nr.getResolvedElement(Naming.START_JAHR));
            int jahr = (Integer) term.getParameter(org.thespheres.betula.niedersachsen.NdsTerms.JAHR);
            int stufe = jahr - dJahr + s;
            return ImportUtil.calculateDeleteDate(stufe, 5, Month.JULY);
        }

    }

}
