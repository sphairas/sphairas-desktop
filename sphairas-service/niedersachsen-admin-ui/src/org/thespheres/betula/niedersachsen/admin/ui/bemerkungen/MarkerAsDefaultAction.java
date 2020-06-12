/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.MarkerItem;

@ActionID(category = "Tools",
        id = "org.thespheres.betula.niedersachsen.admin.ui.bemerkungen.MarkerAsDefaultAction")
@ActionRegistration(displayName = "#CTL_MarkerAsDefaultAction")
@Messages("CTL_MarkerAsDefaultAction=Standard setzen")
public final class MarkerAsDefaultAction implements ActionListener {

    private final MarkerItem context;

    public MarkerAsDefaultAction(final MarkerItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        this.context.setDefaultItem();
    }
}
