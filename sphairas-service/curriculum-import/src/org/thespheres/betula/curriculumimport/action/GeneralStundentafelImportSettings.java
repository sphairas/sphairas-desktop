/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.WizardDescriptor;
import org.openide.util.NetworkSettings;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.curriculum.CourseSelection;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.curriculum.Section;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.UnitEntry;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
public class GeneralStundentafelImportSettings extends StundentafelImportSettings implements PropertyChangeListener {

    @SuppressWarnings({"LeakingThisInConstructor"})
    GeneralStundentafelImportSettings(final Curriculum file) {
        super(file);
        addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (AbstractFileImportAction.IMPORT_TARGET.equals(evt.getPropertyName())) {
            final ConfigurableImportTarget cit = (ConfigurableImportTarget) getProperty(AbstractFileImportAction.IMPORT_TARGET);
            final Term importTerm = (Term) getProperty(AbstractFileImportAction.TERM);
            if (cit != null && importTerm != null) {
                final WebServiceProvider ws = cit.getWebServiceProvider();
                try {
                    final List<UnitEntry> loadEntries = loadUnits(ws);
                    final Set<UnitId> pu = loadEntries.stream()
                            .filter(u -> u.getValue().getMarkerSet().contains(ServiceConstants.BETULA_PRIMARY_UNIT_MARKER))
                            .map(u -> u.getUnit())
                            .collect(Collectors.toSet());
                    final NamingResolver res = NamingResolver.find(cit.getProvider());
                    initNodes(cit, importTerm, pu, res);
                } catch (IOException ex) {
                    putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ex.getLocalizedMessage());
                }
            }
        }
    }

    private static List<UnitEntry> loadUnits(final WebServiceProvider wp) throws IOException {
        final BetulaWebService service = wp.createServicePort();
        final ContainerBuilder builder = new ContainerBuilder();
        final Action action = Action.REQUEST_COMPLETION;
        final Template t = builder.createTemplate(Paths.UNITS_PATH, null, action);
        t.getHints().put("preferred-security-role", "unitadmin");
        t.getHints().put("request-completion.no-children", "true");
        final Container response;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> service.solicit(builder.getContainer()));
        } catch (ServiceException ex) {
            throw new IOException(ex);
        } catch (Exception ex) {
            throw (RuntimeException) ex;
        }
        //units
        return DocumentUtilities.findEnvelope(response, Paths.UNITS_PATH).stream()
                .filter(node -> (node.getAction() != null && node.getAction().equals(Action.RETURN_COMPLETION)))
                .flatMap(n -> n.getChildren().stream())
                .filter(UnitEntry.class::isInstance)
                .map(UnitEntry.class::cast)
                .collect(Collectors.toList());
    }

    private void initNodes(final ConfigurableImportTarget cit, final Term importTerm, final Set<UnitId> pus, final NamingResolver res) throws IOException {
        for (final UnitId pu : pus) {
            NamingResolver.Result nr = null;
            NamingResolver.Result r = null;
            try {
                nr = res.resolveDisplayNameResult(pu);
                r = res.resolveDisplayNameResult(pu);
                r.addResolverHint("naming.only.level");
            } catch (IllegalAuthorityException iaex) {
                throw new IOException(iaex);
            }
            final String se = r.getResolvedName(importTerm);
            final Object he = importTerm.getParameter("halbjahr");
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
                            final StundentafelImportTargetsItem add = new StundentafelImportTargetsItem(nr, pu, cs, stufe, hj, importTerm, curriculum);
                            getSelectedNodesProperty().add(add);
                        }
                    }
                } catch (NumberFormatException nfex) {
                }
            }
        }
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
