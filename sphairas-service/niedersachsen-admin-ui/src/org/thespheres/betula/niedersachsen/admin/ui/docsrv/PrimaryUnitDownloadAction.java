/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.docsrv;

import java.awt.Cursor;
import java.io.IOException;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admindocsrv.AbstractDownloadAction;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.util.LogLevel;

/**
 *
 * @author boris.heithecker
 */
@Messages({"PrimaryUnitDownloadAction.error.title=http-Fehler"})
abstract class PrimaryUnitDownloadAction extends AbstractDownloadAction<PrimaryUnitOpenSupport> {

    protected final RequestProcessor RP = new RequestProcessor(DownloadZeugnisse.class);

    @SuppressWarnings("OverridableMethodCallInConstructor")
    protected PrimaryUnitDownloadAction(String mime, String extension) {
        super(mime, extension);
        putValue(Action.NAME, getDisabledName());
        setEnabled(false);
    }

    protected PrimaryUnitDownloadAction(Lookup context, String mime, String extension) {
        super(context, PrimaryUnitOpenSupport.class, mime, extension, true, false);
        updateEnabled();
        updateName();
    }

    protected abstract String getDisabledName();

    @Override
    protected void actionPerformed(final List<PrimaryUnitOpenSupport> list) {
        final Term selectedTerm = term;
        actionPerformed(list, selectedTerm);
    }

    void actionPerformed(final List<PrimaryUnitOpenSupport> list, final Term selectedTerm) {
        final TopComponent ac = TopComponent.getRegistry().getActivated();
        final Cursor before = ac.getCursor();
        ac.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        RP.post(() -> {
            list.stream().forEach((PrimaryUnitOpenSupport ctx) -> {
                try {
                    downLoad(ctx, selectedTerm);
                } catch (IOException ex) {
                    notifyError(ex);
                }
            });
        }).addTaskListener(t -> ac.setCursor(before));
    }

    static void notifyError(IOException ex) throws MissingResourceException {
        Logger.getLogger(PrimaryUnitDownloadAction.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(PrimaryUnitDownloadAction.class, "PrimaryUnitDownloadAction.error.title");
        final String detail = ex.getMessage();
        NotificationDisplayer.getDefault()
                .notify(title, ic, detail != null ? detail : "", null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    protected abstract void downLoad(PrimaryUnitOpenSupport context, Term selectedTerm) throws IOException;
}
