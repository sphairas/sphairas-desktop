/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.curriculum.CourseSelection;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.curriculum.Section;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"ContextStundentafelImportSettings.createSettings.noCommonImportTarget=Keine gemeinsame Import-Konfiguration gefunden."})
public class ContextStundentafelImportSettings extends StundentafelImportSettings {

    private final List<PrimaryUnitOpenSupport> context;

    private ContextStundentafelImportSettings(Curriculum file, List<PrimaryUnitOpenSupport> context, ConfigurableImportTarget config, TermSchedule termSchedule) {
        super(file);
        this.context = context;
        putProperty(AbstractFileImportAction.IMPORT_TARGET, config);
        final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
        putProperty(AbstractFileImportAction.TERM, termSchedule.getTerm(wd.getCurrentWorkingDate()));
    }

    static ContextStundentafelImportSettings createSettings(final Curriculum xml, final List<PrimaryUnitOpenSupport> ctx) {
        ConfigurableImportTarget config;
        try {
            config = findCommonImportTarget(ctx);
        } catch (IOException | NoProviderException ex) {
            ImportUtil.getIO().getErr().println(ex);
            return null;
        }
        if (config == null) {
            String msg = NbBundle.getMessage(ContextStundentafelImportSettings.class, "ContextStundentafelImportSettings.createSettings.noCommonImportTarget");
            ImportUtil.getIO().getErr().println(msg);
            return null;
        }
        final TermSchedule ts = config.getTermSchemeProvider().getScheme(config.getTermSchemeId(), TermSchedule.class);
        return new ContextStundentafelImportSettings(xml, ctx, config, ts);
    }

    private static ConfigurableImportTarget findCommonImportTarget(final List<PrimaryUnitOpenSupport> l) throws IOException {
        String found = null;
        for (PrimaryUnitOpenSupport auos : l) {
            String url = auos.findBetulaProjectProperties().getProperty("providerURL");
//            auos.findBetulaProjectProperties().getProperty("termSchemeId", TermSchedule.DEFAULT_SCHEME);
            if (url == null) {
                return null;
            }
            if (found == null) {
                found = url;
            } else if (!found.equals(url)) {
                return null;
            }
        }
        return ConfigurableImportTarget.find(found);
    }

    @NbBundle.Messages({"ContextStundentafelImportSettings.initialize.termIdmismatch=Konflikt bei der Auflösung des Halbjahrs; {0} kann nicht importiert werden.",})
    @Override
    public void initialize(final WizardDescriptor wiz) throws IOException {
        final TermId configTid = ((Term) getProperty(AbstractFileImportAction.TERM)).getScheduledItemId();
        for (final PrimaryUnitOpenSupport uos : context) {
            final UnitId pu = uos.getUnitId();
            final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
            final Term term = uos.findTermSchedule().getTerm(wd.getCurrentWorkingDate());
            NamingResolver.Result nr = null;
            NamingResolver.Result r = null;
            try {
                nr = uos.findNamingResolver().resolveDisplayNameResult(pu);
                r = uos.findNamingResolver().resolveDisplayNameResult(pu);
                r.addResolverHint("naming.only.level");
            } catch (IllegalAuthorityException iaex) {
                throw new IOException(iaex);
            }
            if (!term.getScheduledItemId().equals(configTid)) {
                final String msg = NbBundle.getMessage(ContextStundentafelImportSettings.class, "ContextStundentafelImportSettings.initialize.termIdmismatch", nr.getResolvedName());
                ImportUtil.getIO().getErr().println(msg);
                continue;
            }
            final String se = r.getResolvedName(term);
            final Object he = term.getParameter("halbjahr");
            if (se != null && he instanceof Integer) {
                final int stufe;
                try {
                    stufe = Integer.parseInt(se);
                    final int hj = (Integer) he;
                    final Section sec = curriculum.findSection(stufe, hj);
                    if (sec != null) {
                        final Set<CourseSelection> subjects = sec.getSelection();
                        for (final CourseSelection cs : subjects) {
                            if (cs.getCourse() == null || cs.getCourse().getSubject() == null) {
                                continue;
                            }
                            final StundentafelImportTargetsItem add = new StundentafelImportTargetsItem(nr, pu, cs, stufe, hj, term, curriculum);
                            getSelectedNodesProperty().add(add);
                        }
                    }
                } catch (NumberFormatException nfex) {
                }
            }
        }
//iterate over uos and subjects corresponding in curriculum,  add selectednodes to changeset
    }

    public List<PrimaryUnitOpenSupport> getPrimaryUnits() {
        return context;
    }

    static class XmlDataImportActionWizardIterator extends AbstractFileImportWizard<StundentafelImportSettings> {

        XmlDataImportActionWizardIterator() {
        }

        //Vier verschiedene Typen: SuS,Klassen - Kurse - Kurszuordnungen - Lehrer
        @Override
        protected ArrayList<WizardDescriptor.Panel<StundentafelImportSettings>> createPanels() {
            final ArrayList<WizardDescriptor.Panel<StundentafelImportSettings>> ret = new ArrayList<>();
            ret.add(new StundentafelImportConfigVisualPanel.XmlDataImportConfigPanel<>());
            ret.add(new StundentafelImportDocumentsVisualPanel.DocumentsPanel());
            return ret;
        }

    }
}
