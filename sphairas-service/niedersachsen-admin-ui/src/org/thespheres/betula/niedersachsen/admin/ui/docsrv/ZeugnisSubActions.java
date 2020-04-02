/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.docsrv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author boris.heithecker
 */
@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.ZeugnisSubActions")
@ActionRegistration(
        displayName = "#ZeugnisSubActions.name",
        lazy = false)
@ActionReferences({
    @ActionReference(path = "Menu/units-administration", position = 1100),
    @ActionReference(path = "Loaders/application/betula-unit-data/Actions", position = 12000)
})
@NbBundle.Messages({"ZeugnisSubActions.name=Zeugnisse/Listen"})
public class ZeugnisSubActions extends AbstractAction implements ActionListener, Presenter.Popup {

    @Override
    public void actionPerformed(ActionEvent e) {
        //NOP
    }

    @Override
    public JMenuItem getPopupPresenter() {
        final JMenu main = new JMenu(NbBundle.getMessage(ZeugnisSubActions.class, "ZeugnisSubActions.name"));
        Utilities.actionsForPath("Loaders/application/betula-unit-data/ZeugnisSubActions").stream()
                .filter(Objects::nonNull)
                .forEach(main::add);
        return main;
    }
}
