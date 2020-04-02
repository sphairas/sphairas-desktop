/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import org.openide.awt.Actions;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;
import org.thespheres.betula.xmlimport.ImportItem;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <W>
 */
public abstract class AbstractCreateDocumentsAction<I extends ImportItem, W extends DefaultImportWizardSettings> extends AbstractAction implements Presenter.Toolbar {

    protected JButton button;

    protected AbstractCreateDocumentsAction(String name, Icon icon) {
        super(name, icon);
    }

    protected AbstractCreateDocumentsAction(String name) {
        super(name);
    }

    @Override
    public Component getToolbarPresenter() {
        if (button == null) {
            button = new JButton();
            button.setIcon(ImageUtilities.loadImageIcon("org/thespheres/betula/gpuntis/resources/blue-document-copy.png", true));
            Actions.connect(button, this);
        }
        return button;
    }

    protected CreateDocumentsComponent<I, W> getCreateDocumentsComponent() {
        Component comp = button;
        while ((comp = comp.getParent()) != null) {
            if (comp instanceof CreateDocumentsComponent) {
                return (CreateDocumentsComponent<I, W>) comp;
            }
        }
        throw new IllegalStateException("No CreateDocumentsComponent ancestor found.");
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        performAction(getCreateDocumentsComponent());
    }

    protected abstract void performAction(CreateDocumentsComponent<I, W> panel);

}
