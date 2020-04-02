/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ticketui;

import java.io.IOException;
import java.util.logging.Level;
import javax.swing.Icon;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.document.util.TicketEntry;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
class TicketActionUtil {

    @NbBundle.Messages(value = {"TicketActionUtil.checkException.title=Fehler beim Anlegen der Berechtigung", 
        "TicketActionUtil.checkException.message=Beim Anlegen der Berechtigung {0} ist ein Fehler aufgetreten. Siehe sphairas-log f\u00fcr mehr Informationen."})
    static boolean checkException(final TicketEntry e) {
        try {
            Util.processException(e, e.getIdentity());
        } catch (IOException ex) {
            final String id = e.getIdentity() != null ? e.getIdentity().getId().toString() : "null";
            PlatformUtil.getCodeNameBaseLogger(TicketActionUtil.class).log(Level.SEVERE, "Could not create ticket " + id + ".", ex);
            final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
            final String title = NbBundle.getMessage(AddUnitTicketAction.class, "TicketActionUtil.checkException.title");
            final String message = NbBundle.getMessage(AddUnitTicketAction.class, "TicketActionUtil.checkException.message", id);
            NotificationDisplayer.getDefault().notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
            return false;
        }
        return true;
    }

}
