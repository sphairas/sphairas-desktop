/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admindocsrv.action.TargetDownloadSelectActionPanel.DownloadSelectActionPanelWizardPanel;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "org.thespheres.betula.admindocsrv.action.TargetDownloadSelectAction")
@ActionRegistration(
        displayName = "#TargetDownloadSelectAction.name")
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-target-assessment-document/Actions", position = 57000, separatorBefore = 50000)
})
@Messages({"TargetDownloadSelectAction.name=Zensurenlisten erstellen (Auswahl)",
    "TargetDownloadSelectAction.title=Aktion und Halbjahr wählen"})
public class TargetDownloadSelectAction implements ActionListener {

    static final String ACTION = "action";
    static final String TERM = "term";
    static final String DELEGATE = "delegate";
    private final List<RemoteTargetAssessmentDocument> context;
    TermSchedule termSchedule;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public TargetDownloadSelectAction(final List<RemoteTargetAssessmentDocument> context) throws IOException {
        this.context = context;
        findCommonTermSchedule();
    }

    protected void findCommonTermSchedule() throws IOException {
        TermSchedule found = null;
        for (final RemoteTargetAssessmentDocument rtad : context) {
        final LocalProperties lp = LocalProperties.find(rtad.getProvider());
        final TermSchedule ts = Optional.ofNullable(lp)
                .map(p -> p.getProperty("termSchedule.providerURL"))
                .map(tp -> SchemeProvider.find(tp))
                .map(t -> t.getScheme(lp.getProperty("termSchemeId", TermSchedule.DEFAULT_SCHEME), TermSchedule.class))
                .orElse(null);
            if (found == null) {
                found = ts;
            } else if (!(found.getName().equals(ts.getName()) && found.getType().equals(ts.getType()))) {
                throw new IOException("No common term schedule.");
            }
        }
        if (found == null) {
            throw new IOException("No term schedule.");
        }
        termSchedule = found;
    }

    @Override
    public void actionPerformed(ActionEvent ignored) {
        final List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new DownloadSelectActionPanelWizardPanel());
        final String[] steps = new String[panels.size()];
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
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(TargetDownloadSelectAction.class, "TargetDownloadSelectAction.title"));
        wiz.putProperty(TargetDownloadSelectAction.ACTION, this);
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final Term t = (Term) wiz.getProperty(TargetDownloadSelectAction.TERM);
            final TargetDocumentDownloadAction d = (TargetDocumentDownloadAction) wiz.getProperty(TargetDownloadSelectAction.DELEGATE);
            d.actionPerformed(context, t);
        }
    }

    List<TargetDocumentDownloadAction> findDelegateActions() {
        final List<TargetDocumentDownloadAction> acl = new ArrayList<>();
        acl.add((TargetDocumentDownloadAction) TargetDocumentDownloadAction.pdfAction());
        acl.add((TargetDocumentDownloadAction) TargetDocumentDownloadAction.csvAction());
        return acl;
    }

    List<Term> findSelectableTerms() {
        final Term ct = termSchedule.getCurrentTerm();
        final TermId ctid = ct.getScheduledItemId();
        int id = ct.getScheduledItemId().getId();
        final ArrayList<Term> ret = new ArrayList<>();
        for (int i = id - 10; i++ <= id + 4;) {
            Term add = null;
            if (i == 0) {
                add = ct;
            } else {
                final TermId tid = new TermId(ctid.getAuthority(), i);
                try {
                    add = termSchedule.resolve(tid);
                } catch (TermNotFoundException | IllegalAuthorityException ex) {
                }
            }
            if (add != null) {
                ret.add(add);
            }
        }
        return ret;
    }

}
