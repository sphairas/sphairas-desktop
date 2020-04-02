/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.docsrv;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadSelectActionPanel.DownloadSelectActionPanelWizardPanel;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadSelectAction")
@ActionRegistration(
        displayName = "#DownloadSelectAction.name")
@ActionReferences({
    @ActionReference(path = "Menu/units-administration", position = 50000),
    @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 50000) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
})
@Messages({"DownloadSelectAction.name=Auswahl",
    "DownloadSelectAction.title=Aktion und Halbjahr wählen"})
public class DownloadSelectAction implements ActionListener {

    static final String ACTION = "action";
    static final String TERM = "term";
    static final String DELEGATE = "delegate";
    private final List<PrimaryUnitOpenSupport> context;
    TermSchedule termSchedule;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public DownloadSelectAction(final List<PrimaryUnitOpenSupport> context) throws IOException {
        this.context = context;
        findCommonTermSchedule();
    }

    protected void findCommonTermSchedule() throws IOException {
        TermSchedule found = null;
        for (final PrimaryUnitOpenSupport puos : context) {
            final TermSchedule ts = puos.findTermSchedule();
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
        wiz.setTitle(NbBundle.getMessage(DownloadSelectAction.class, "DownloadSelectAction.title"));
        wiz.putProperty(DownloadSelectAction.ACTION, this);
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final Term t = (Term) wiz.getProperty(DownloadSelectAction.TERM);
            final PrimaryUnitDownloadAction d = (PrimaryUnitDownloadAction) wiz.getProperty(DownloadSelectAction.DELEGATE);
            d.actionPerformed(context, t);
        }
    }

    List<PrimaryUnitDownloadAction> findDelegateActions() {
        final List<PrimaryUnitDownloadAction> acl = new ArrayList<>();
        acl.add((PrimaryUnitDownloadAction) DownloadZeugnisse.pdfAction());
        acl.add((PrimaryUnitDownloadAction) DownloadZeugnisse.xmlAction());
        acl.add((PrimaryUnitDownloadAction) DownloadListen.listenPdfAction());
        acl.add((PrimaryUnitDownloadAction) DownloadListen.listenCsvAction());
        acl.add((PrimaryUnitDownloadAction) DownloadDetails.detailsPdfAction());
        acl.add((PrimaryUnitDownloadAction) new DownloadArchive());
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
