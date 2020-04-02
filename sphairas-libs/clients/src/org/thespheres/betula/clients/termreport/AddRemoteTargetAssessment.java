/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.termreport;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.clients.termreport.AddRemoteTargetAssessmentWizardVisualPanel.AddRemoteTargetAssessmentWizardPanel;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ui.util.Targets;
import org.thespheres.betula.services.util.UnitTarget;
import org.thespheres.betula.termreport.TermReportActions;

@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.clients.termreport.AddRemoteTargetAssessment"
)
@ActionRegistration(
        displayName = "#AddRemoteTargetAssessment.displayName"
)
@ActionReference(path = "Loaders/text/term-report-file+xml/Actions", position = 2000, separatorBefore = 1200, separatorAfter = 5000)
@Messages("AddRemoteTargetAssessment.displayName=Server-Liste hinzufügen")
public final class AddRemoteTargetAssessment implements ActionListener {

    private final TermReportActions context;

    public AddRemoteTargetAssessment(TermReportActions context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        RemoteServiceDescriptor ret = null;
        try {
            ret = doPerfomAction();
        } catch (IOException ex) {
        }
        if (ret != null) {
            List<WizardDescriptor.Panel<RemoteServiceDescriptor>> panels = new ArrayList<>();
            panels.add(new AddRemoteTargetAssessmentWizardPanel());
            String[] steps = new String[panels.size()];
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
            WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<RemoteServiceDescriptor>(panels), ret);
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            wiz.setTitleFormat(new MessageFormat("{0}"));
            wiz.setTitle("...dialog title...");
            if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
                DocumentId document = ret.getSelectedDocument();
                Term term = ret.getSelectedTerm();
                XmlRemoteTargetAssessmentProvider create = XmlRemoteTargetAssessmentProvider.create(document, term.getScheduledItemId(), context.getContext());
                context.addAssessmentProvider(create);
            }
        }
    }

    private RemoteServiceDescriptor doPerfomAction() throws IOException {
        final LocalFileProperties prop = context.getProperties();
        final String provider = prop.getProperty("providerURL", null);
        final Targets t = Targets.get(provider);
        final String termProvider = prop.getProperty("termSchedule.providerURL");
        if (provider != null) {
            final String naming = prop.getProperty("naming.providerURL", provider);
            final Unit unit = context.getContext().lookup(Unit.class);
            final DocumentId target = UnitTarget.parseTargetBase(prop);
            if (unit != null && target != null && termProvider != null) {
//            final UnitTarget ut = new UnitTarget(unit.getUnitId(), target, provider, naming);
                final SchemeProvider sp = SchemeProvider.find(termProvider);
                final TermSchedule ts = sp.getScheme(prop.getProperty("termSchemeId", TermSchedule.DEFAULT_SCHEME), TermSchedule.class);
                final NamingResolver nr = NamingResolver.find(naming);
                final DocumentsModel dm = new DocumentsModel();
                dm.initialize(prop.getProperties());
                final Map<DocumentId, List<UnitId>> m;
                try {
                    m = t.getWebServiceProvider().getDefaultRequestProcessor()
                            .submit(() -> t.getTargetDocuments())
                            .get();
                } catch (InterruptedException | ExecutionException ex) {
                    throw new IOException(ex);
                }
                Set<DocumentId> docs = m.entrySet().stream()
                        .filter(e -> dm.convert(e.getKey()).equals(target) && e.getValue().contains(unit.getUnitId()))
                        .map(e -> e.getKey())
                        .collect(Collectors.toSet());

                return new RemoteServiceDescriptor(provider, unit, target, ts, dm, nr, docs);
            }
        }
        return null;
    }
}
