/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.module2;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.thespheres.betula.classtest.model.EditableBasket;
import org.thespheres.betula.classtest.model.EditableProblem;

/**
 *
 * @author boris.heithecker
 */
public class BasketNodeChildren extends Index.KeysChildren<EditableProblem> {

    private final EditableBasket basket;
    private final Lookup context;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public BasketNodeChildren(EditableBasket ed, Lookup context) {
        super(ed.getReferenced());
        this.basket = ed;
        this.context = context;
        this.basket.getEditableClassroomTest().getEventBus().register(this);
    }

    @Override
    protected Node[] createNodes(EditableProblem key) {
        final ArrayList<Node> n = new ArrayList<>(1);
        if (key instanceof EditableBasket) {
            final EditableProblem parent = key.getParent();
            final EditableBasket b = (EditableBasket) key;
            if (parent != null && parent.getId().equals(basket.getId())) {
                n.add(BasketNode.create(b, context));
            } else {
                n.add(new BasketReferenceNode(basket, b));
            }
        }
        return n.stream().toArray(Node[]::new);
    }

    @Subscribe
    public void onChange(PropertyChangeEvent evt) {
        if (evt.getSource() == basket && evt.getPropertyName().equals(EditableBasket.PROP_REFERENCES)) {
            update();
        }
    }
}
