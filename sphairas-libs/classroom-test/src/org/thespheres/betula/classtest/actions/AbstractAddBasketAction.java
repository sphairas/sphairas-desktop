/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.classtest.Assessable;
import org.thespheres.betula.classtest.Basket;
import org.thespheres.betula.classtest.Hierarchical;
import org.thespheres.betula.classtest.HierarchyExcpeption;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.ClassroomTestOutline;
import org.thespheres.betula.classtest.model.EditableBasket;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableProblem;
import org.thespheres.betula.classtest.module2.BasketNode;
import org.thespheres.betula.classtest.module2.ClasstestConfiguration;
import org.thespheres.betula.classtest.xml.XmlProblem;
import org.thespheres.betula.util.Utilities;

abstract class AbstractAddBasketAction implements ActionListener {

    protected ClassroomTestEditor2 context;
    protected final EditableProblem<?> parent;

    private AbstractAddBasketAction(ClassroomTestEditor2 context, EditableProblem parent) {
        this.context = context;
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (context == null) {
            context = org.openide.util.Utilities.actionsGlobalContext().lookup(ClassroomTestEditor2.class);
        }
        if (context == null) {
            final BasketNode bn = org.openide.util.Utilities.actionsGlobalContext().lookup(BasketNode.class);
            context = bn.getContext().lookup(ClassroomTestEditor2.class);
        }
        if (context == null) {
            return;
        }
        final XmlProblem p = createProblem();
        updateContext(p);
    }

    protected XmlProblem createProblem() {
        final EditableClassroomTest<?, ?, ?> etest = context.getEditableClassroomTest();
        final ClassroomTestOutline outline = context.getContext().lookup(ClassroomTestOutline.class);
        final XmlProblem p;
        if (outline != null) {
            p = (XmlProblem) outline.createProblem(etest, parent);
        } else {
            String id;
            int pos = etest.getEditableProblems().size();
            one:
            while (true) {
                id = Utilities.createId(pos);
                for (EditableProblem<?> ep : etest.getEditableProblems()) {
                    if (ep.getId().equals(id)) {
                        ++pos;
                        continue one;
                    }
                }
                break;
            }
            p = new XmlProblem(id, pos);
            final String displayName = ClasstestConfiguration.findProblemDisplayName(pos + 1, null, null);
            p.setDisplayName(displayName);
        }
        return p;
    }

    protected <P extends Assessable.Problem & Basket.Ref<?> & Hierarchical> void updateContext(P p) {
        EventQueue.invokeLater(() -> context.insertProblem(p));
    }

    @ActionID(category = "Betula", id = "org.thespheres.betula.classtest.actions.AddBasketAction")
    @ActionRegistration(displayName = "#AddBasketAction.displayName",
            iconBase = "org/thespheres/betula/classtest/resources/puzzle--plus.png")
    @ActionReferences({
        @ActionReference(path = "Loaders/application/betula-classroomtest-basket-context/Actions", position = 3000)})
    @Messages("AddBasketAction.displayName=Aufgabe hinzufügen")
    public static class AddBasketAction extends AbstractAddBasketAction {

        public AddBasketAction(ClassroomTestEditor2 context) {
            super(context, null);
        }
    }

    @ActionID(category = "Betula", id = "org.thespheres.betula.classtest.actions.AddSubBasketAction")
    @ActionRegistration(displayName = "#AddSubBasketAction.displayName",
            iconBase = "org/thespheres/betula/classtest/resources/puzzle--plus.png")
    @ActionReferences({
        @ActionReference(path = "Loaders/application/betula-classroomtest-basket-context/Actions", position = 100, separatorAfter = 450)})
    @Messages("AddSubBasketAction.displayName=Unteraufgabe hinzufügen")
    public static class AddSubBasketAction extends AbstractAddBasketAction {

        public AddSubBasketAction(EditableProblem context) {
            super(null, context);
        }

        @Override
        protected <P extends Assessable.Problem & Basket.Ref<?> & Hierarchical> void updateContext(P p) {
            final EditableProblem ep = context.insertProblem(p);
            try {
                ep.setParent((EditableBasket) parent);
                ((EditableBasket) parent).addReference(ep);
            } catch (HierarchyExcpeption ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }
}
