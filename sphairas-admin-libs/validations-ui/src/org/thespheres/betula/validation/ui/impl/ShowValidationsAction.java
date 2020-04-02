/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.ui.impl;

import com.google.common.collect.Sets;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOProvider;
import org.openide.windows.IOSelect;
import org.openide.windows.InputOutput;
import org.thespheres.betula.validation.ui.ValidationProgressUI;
import org.thespheres.betula.validation.ui.ValidationProgressUI.ValidationItem;

@ActionID(
        category = "Tools",
        id = "org.thespheres.betula.validation.ui.impl.ShowValidationsAction")
@ActionRegistration(
        displayName = "#CTL_ShowValidationsAction")
@ActionReference(path = "Menu/Tools", position = 1250)
@Messages("CTL_ShowValidationsAction=Validierungen anzeigen")
public final class ShowValidationsAction implements ActionListener {

    private static InputOutput io;

    @Override
    public void actionPerformed(ActionEvent e) {
        getIO().select();
    }

    @NbBundle.Messages({"ImportUtil.ioTab.title=Validierungen"})
    public static synchronized InputOutput getIO() {
        if (io == null) {
            final Action[] ac = new Action[]{new ShowDetailsAction()};
            io = IOProvider.getDefault().getIO(NbBundle.getMessage(ShowValidationsAction.class, "ImportUtil.ioTab.title"), ac);
            IOSelect.select(io, Sets.newHashSet(IOSelect.AdditionalOperation.OPEN));
        }
        return io;
    }

    private final static class ShowDetailsAction extends AbstractAction {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private ShowDetailsAction() {
            super("set-verbose");
            final Icon icon = ImageUtilities.loadImageIcon("org/thespheres/betula/validation/ui/resources/application-detail.png", true);
            putValue(Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            InputOutput io = ShowValidationsAction.getIO();
            for (ValidationItem it : ValidationProgressUI.getDefault().getItemsSnapshot()) {
                try {
                    IOColorLines.println(io, it.getDisplayName(), Color.RED);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

    }
}
