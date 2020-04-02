/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.module2;

import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.actions.RenameAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableBasket;
import org.thespheres.betula.classtest.model.EditableProblem;
import org.thespheres.betula.ui.util.FilterNodeProvider;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-classroomtest-basket-context/Actions", id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), position = 500),
    @ActionReference(path = "Loaders/application/betula-classroomtest-basket-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.MoveUpAction"), position = 11100),
    @ActionReference(path = "Loaders/application/betula-classroomtest-basket-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.MoveDownAction"), position = 11200),
    @ActionReference(path = "Loaders/application/betula-classroomtest-basket-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"), position = 12000),
    @ActionReference(path = "Loaders/application/betula-classroomtest-basket-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), separatorBefore = 1000000, position = 1100000)})
public class BasketNode extends AbstractNode {

    public static final String BASKET_CONTEXT_MIME = "application/betula-classroomtest-basket-context";
    private final EditableProblem<?> problem;
    private final Listener listener = new Listener();
    private final Lookup context;
    private final InstanceContent ic;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private BasketNode(EditableProblem p, BasketNodeChildren ch, Lookup context, InstanceContent ic) {
        super(ch, new AbstractLookup(ic));
        this.ic = ic;
        this.ic.add(this);
        problem = p;
        this.ic.add(problem);
        this.context = context;
        setDisplayName(problem.getDisplayName());
        setIconBaseWithExtension("org/thespheres/betula/classtest/resources/rblau.png");
        p.getEditableClassroomTest().getEventBus().register(this);
        addNodeListener(listener);
    }

    public static Node create(EditableBasket p, Lookup context) {
        final BasketNodeChildren ch = new BasketNodeChildren(p, context);
        final BasketNode bn = new BasketNode(p, ch, context, new InstanceContent());
        for (FilterNodeProvider fnp : MimeLookup.getLookup(BASKET_CONTEXT_MIME).lookupAll(FilterNodeProvider.class)) {
            FilterNode fn = fnp.createFilterNode(bn, p);
            if (fn != null) {
                return fn;
            }
        }
        return bn;
    }

    public Lookup getContext() {
        return context;
    }

    @Override
    public boolean canCut() {
        return true;
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        EventQueue.invokeLater(problem::remove);
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(RenameAction.class);
    }

    @Override
    public String getHtmlDisplayName() {
        String dN = getDisplayName();
        if (problem.isBasket()) {
            return "<html><b>" + dN + "</b></html>";
        } else {
            return "<html>" + dN + "</html>";
        }
    }

    @Override
    public PasteType getDropType(final Transferable t, int action, int index) {
        final Node n = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        if (n != null) {
            final EditableProblem p = n.getLookup().lookup(EditableProblem.class);
            if (p != null && !p.isBasket() && !this.equals(n.getParentNode())) {
                return new PasteType() {
                    @Override
                    public Transferable paste() throws IOException {
                        ((EditableBasket) problem).addReference(p);
//                        ((EditableBasket) problem).setParent((EditableBasket) problem);
                        return null;
                    }
                };
            }
        }
        return null;
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/" + BASKET_CONTEXT_MIME + "/Actions").stream()
                .map(Action.class::cast)
                .toArray(Action[]::new);
    }

    @Subscribe
    public void propertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (evt.getSource() == this.problem
                && (EditableProblem.PROP_DISPLAY_NAME.equals(name) || EditableBasket.PROP_REFERENCES.equals(evt.getPropertyName()))) {
            setDisplayName(problem.getDisplayName());
        }
    }

    private class Listener extends NodeAdapter {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Node.PROP_NAME.equals(evt.getPropertyName())) {
                final String name = (String) evt.getNewValue();
                context.lookup(ClassroomTestEditor2.class).setProblemDisplayName(problem, name);
            }
        }
    }
}
