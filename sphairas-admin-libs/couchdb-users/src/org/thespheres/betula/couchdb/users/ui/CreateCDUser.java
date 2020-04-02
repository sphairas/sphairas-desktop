/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.couchdb.users.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.StringJoiner;
import javax.swing.JFileChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.admin.units.RemoteSignee;
import org.thespheres.betula.couchdb.*;
import org.thespheres.betula.couchdb.users.impl.CouchDBUser;
import org.thespheres.betula.couchdb.users.impl.GenDBTask;
import org.thespheres.betula.services.WebProvider;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.couchdb.users.ui.CreateCDUser")
@ActionRegistration(displayName = "#CreateCDUser.displayName")
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remotesignee-context/Actions", position = 15000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@Messages({"CreateCDUser.displayName=CouchDB Benutzer erstellen",
    "CreateCDUser.FileChooser.Title=pdf Datei",
    "CreateCDUser.FileChooser.FileDescription=pdf-Ausdruck erstellen",
    "CreateCDUser.FileChooser.approve=Ausdruck"})
public final class CreateCDUser implements ActionListener {

    private final List<RemoteSignee> context;

    public CreateCDUser(List<RemoteSignee> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final String hint = createFileNameHint();
        File save = Mutex.EVENT.writeAccess(() -> showDialog(hint));
        if (save != null && !save.getName().endsWith(".pdf")) {
            save = new File(save.getParent(), save.getName() + ".pdf");
        }

        for (RemoteSignee signee : context) {
//            CouchDBProvider db = WebProvider.find(signee.getRemoteLookup().getProviderInfo().getURL(), CouchDBProvider.class);
            CouchDBAdminInstance db = WebProvider.find("provider", CouchDBAdminInstance.class);
            CouchDBUser u = CouchDBUser.create(signee.getSignee().getId(), null, null);
            final RequestProcessor rp = db.getDefaultRequestProcessor();
            GenDBTask t = new GenDBTask(u, db);
            rp.post(t);
            if (save != null) {
                final PrintTask pt = new PrintTask(t, save.toPath());
                rp.post(pt);
            }
        }
    }

    private File showDialog(String hint) {
        File home = new File(System.getProperty("user.home"));
        String title = NbBundle.getMessage(CreateCDUser.class, "CreateCDUser.FileChooser.Title");
        String approve = NbBundle.getMessage(CreateCDUser.class, "CreateCDUser.FileChooser.approve");
        FileChooserBuilderWithHint fcb = new FileChooserBuilderWithHint(hint);
        fcb.setTitle(title).setDefaultWorkingDirectory(home).setApproveText(approve).setFileHiding(true);
        return fcb.showSaveDialog();
    }

    private String createFileNameHint() {
        StringJoiner sj = new StringJoiner("_", "", ".pdf");
        context.forEach(rs -> sj.add(rs.getSignee().getPrefix().replace("\\.", "_")));
        return sj.toString();
    }

    private static class FileChooserBuilderWithHint extends FileChooserBuilder {

        private final String hint;

        private FileChooserBuilderWithHint(String hint) {
            super(CreateCDUser.class);
            this.hint = hint;
        }

        @Override
        public JFileChooser createFileChooser() {
            JFileChooser ret = super.createFileChooser();
            File selected = ret.getCurrentDirectory();
            if (selected != null && selected.isDirectory()) {
                File withHint = new File(selected, hint);
                ret.setSelectedFile(withHint);
            }
            return ret;
        }

    }
}
