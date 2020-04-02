package org.thespheres.betula.termreport.action;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "org.thespheres.betula.termreport.action.ResolveLinksAction")
@ActionRegistration(
        displayName = "#ResolveLinksAction.displayName")
@ActionReferences({
    @ActionReference(path = "Loaders/text/betula-term-report-target-assessment-context/Actions", position = 110000, separatorBefore = 100000)})
@NbBundle.Messages("ResolveLinksAction.displayName=Datei-Link wiederherstellen")
public class ResolveLinksAction implements ActionListener {

    private final List<UnresolvedLink> context;

    public ResolveLinksAction(List<UnresolvedLink> links) {
        this.context = links;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (UnresolvedLink ul : context) {
            File baseDir = ul.getBaseDir();
            if (baseDir == null) {
                baseDir = new File(System.getProperty("user.home"));
            }
            String title = NbBundle.getMessage(ResolveLinksAction.class, "ResolveLinksAction.displayName");
            FileChooserBuilder fcb = new FileChooserBuilder(ResolveLinksAction.class);
            fcb.setTitle(title).setDefaultWorkingDirectory(baseDir).setFileHiding(true);
            File open = fcb.showOpenDialog();
            if (open != null && open.exists()) {
                try {
                    ul.resolve(open);
                } catch (IOException iOException) {
                    Exceptions.printStackTrace(iOException);
                }
            }
        }
    }
}
