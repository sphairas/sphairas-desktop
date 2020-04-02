/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.Unit;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ui.util.ExportTargetAssessmentVisualPanel.ExportTargetAssessmentPanel;
import org.thespheres.betula.services.ui.util.FileTargetAssessmentExport;
import org.thespheres.betula.services.ui.util.TargetAssessmentExport;
import org.thespheres.betula.services.util.UnitTarget;
import org.thespheres.betula.termreport.TargetAssessmentProvider;
import org.thespheres.betula.termreport.TermReportActions;
import org.thespheres.betula.ui.util.FileChooserBuilderWithHint;

public class ExportTargetAssessment implements ActionListener {

    private final TargetAssessmentProvider context;
    private final static RequestProcessor RP = new RequestProcessor(ExportTargetAssessment.class);
    private final String type;

    public ExportTargetAssessment(TargetAssessmentProvider context, String type) {
        this.context = context;
        this.type = type;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final TargetAssessmentExport ret = createDescriptor();
        if (ret == null) {
            return;
        }
        final String fn = context.getEnvironment().getContextLookup().lookup(DataObject.class).getName();
        String hint = "";
        if ("file".equals(type)) {
            hint = NbBundle.getMessage(ExportTargetAssessment.class, "FileExportTargetAssessment.hint", fn, context.getDisplayName());
        } else if ("qr".equals(type)) {
            hint = NbBundle.getMessage(ExportTargetAssessment.class, "QRExportTargetAssessment.hint", fn, context.getDisplayName());
        }
        final File save = showDialog(hint);
        if (save == null || save.isDirectory()) {
            return;
        }
        final FileObject p = FileUtil.toFileObject(save.getParentFile());
        String name = save.getName();
        if ("file".equals(type) && name.endsWith(".xml")) {
            name = name.substring(0, name.length() - ".xml".length()); // new File(save.getParent(), save.getName() + ".xml");
        } else if ("qr".equals(type) && name.endsWith(".pdf")) {
            name = name.substring(0, name.length() - ".pdf".length()); // new File(save.getParent(), save.getName() + ".xml");
        }
        if ("file".equals(type)) {
            ((FileTargetAssessmentExport) ret).setFile(p, name);
        } else if ("qr".equals(type)) {
            ((QRTargetAssessmentExport) ret).setFile(save.toPath());
        }

        final List<WizardDescriptor.Panel<TargetAssessmentExport>> panels = new ArrayList<>();
        panels.add(new ExportTargetAssessmentPanel());
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
        final WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels), ret);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("...dialog title...");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            RP.post(ret);
        }
    }

    @NbBundle.Messages({"ExportTargetAssessment.fileChooser.title=Export",
        "ExportTargetAssessment.FileChooser.fileDescription=Weiter",
        "ExportTargetAssessment.fileChooser.approve=Exportieren"
    })
    private File showDialog(final String hint) {
        final File home = new File(System.getProperty("user.home"));
        final String title = NbBundle.getMessage(ExportTargetAssessment.class, "ExportTargetAssessment.fileChooser.title");
        final String approve = NbBundle.getMessage(ExportTargetAssessment.class, "ExportTargetAssessment.fileChooser.approve");
        final FileChooserBuilderWithHint fcb = new FileChooserBuilderWithHint(ExportTargetAssessment.class, hint);
        fcb.setTitle(title).setDefaultWorkingDirectory(home).setApproveText(approve).setFileHiding(true);
        return fcb.showSaveDialog();
    }

    private TargetAssessmentExport createDescriptor() {
        final LocalFileProperties prop = context.getEnvironment().getContextLookup().lookup(TermReportActions.class).getProperties();
        if (prop == null) {
            return null;
        }
        final String provider = prop.getProperty("providerURL", null);
        final String termProvider = prop.getProperty("termSchedule.providerURL");
        TermSchedule ts = null;
        if (termProvider != null) {
            final SchemeProvider sp = SchemeProvider.find(termProvider);
            ts = sp.getScheme(prop.getProperty("termSchemeId", TermSchedule.DEFAULT_SCHEME), TermSchedule.class);
        }
        final String naming = prop.getProperty("naming.providerURL", provider);
        NamingResolver nr = null;
        if (naming != null) {
            nr = NamingResolver.find(naming);
        }
        final DocumentsModel dm = new DocumentsModel();
        try {
            dm.initialize(prop.getProperties());
        } catch (IllegalStateException e) {
            return null;
        }
        String suffix = "-" + dm.getModelPrimarySuffix();
        final Unit unit = context.getEnvironment().getContextLookup().lookup(Unit.class);//Get project lookup?
        if (unit == null) {
            return null;
        }
        DocumentId base = UnitTarget.parseTargetBase(prop);
        final DocumentId target;
        if (base == null) {
            target = new DocumentId(unit.getUnitId().getAuthority(), unit.getUnitId().getId() + suffix, DocumentId.Version.LATEST);
        } else {
            target = new DocumentId(base.getAuthority(), base.getId() + suffix, base.getVersion());
        }
        if ("file".equals(type)) {
            return new FileTargetAssessmentExport(provider, context, unit, target, ts, nr, prop);
        } else if ("qr".equals(type)) {
            return new QRTargetAssessmentExport(provider, context, unit, target, ts, nr, prop);

        }
        return null;
    }

    @ActionID(
            category = "Betula",
            id = "org.thespheres.betula.termreport.action.FileExportTargetAssessment"
    )
    @ActionRegistration(
            displayName = "#FileExportTargetAssessment.displayName",
            asynchronous = false
    )
    @ActionReference(path = "Loaders/text/betula-term-report-target-assessment-context/Actions", position = 530000, separatorBefore = 500000)
    @Messages({"FileExportTargetAssessment.displayName=Datei Exportieren",
        "FileExportTargetAssessment.hint={0} - {1}.xml"})
    public static class FileExportTargetAssessment extends ExportTargetAssessment {

        public FileExportTargetAssessment(TargetAssessmentProvider context) {
            super(context, "file");
        }

    }

    @ActionID(
            category = "Betula",
            id = "org.thespheres.betula.termreport.action.QRExportTargetAssessment"
    )
    @ActionRegistration(
            displayName = "#QRExportTargetAssessment.displayName",
            asynchronous = false
    )
    @ActionReference(path = "Loaders/text/betula-term-report-target-assessment-context/Actions", position = 530000, separatorBefore = 500000)
    @Messages({"QRExportTargetAssessment.displayName=QR Exportieren",
        "QRExportTargetAssessment.hint={0} - {1}.pdf"})
    public static class QRExportTargetAssessment extends ExportTargetAssessment {

        public QRExportTargetAssessment(TargetAssessmentProvider context) {
            super(context, "qr");
        }

    }
}
