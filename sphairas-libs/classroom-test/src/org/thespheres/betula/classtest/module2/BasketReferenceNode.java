/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.module2;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.thespheres.betula.classtest.model.EditableBasket;
import org.thespheres.betula.classtest.model.EditableProblem;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    @ActionReference(path = "Loaders/text/betula-classroomtest-basket-reference/Actions", id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), position = 500)
})
class BasketReferenceNode extends AbstractNode {

    private final EditableProblem reference;
    private final EditableBasket basket;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    BasketReferenceNode(EditableBasket basket, EditableProblem reference) {
        super(Children.LEAF, Lookup.EMPTY);
        this.reference = reference;
        this.basket = basket;
        setDisplayName(reference.getDisplayName());
        setIconBaseWithExtension("org/thespheres/betula/classtest/resources/sklein.png");
        reference.getEditableClassroomTest().getEventBus().register(this);
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        basket.removeReference(reference);
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public Action getPreferredAction() {
        return null;
    }

    @Override
    public String getHtmlDisplayName() {
        String dN = getDisplayName();
        return "<html>" + dN + "</html>";
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/text/betula-classroomtest-basket-reference/Actions").stream()
                .map(Action.class::cast)
                .toArray(Action[]::new);
    }

    @Subscribe
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == this.reference && EditableProblem.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
            setDisplayName((String) evt.getNewValue());
        }
    }
}
