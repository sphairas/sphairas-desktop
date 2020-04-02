/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.model;

import org.thespheres.betula.termreport.AssessmentProviderNode;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.Actions;
import org.openide.util.Utilities;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    @ActionReference(path = "Loaders/text/betula-term-report-target-assessment-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.MoveUpAction"), position = 11100),
    @ActionReference(path = "Loaders/text/betula-term-report-target-assessment-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.MoveDownAction"), position = 11200),
    @ActionReference(path = "Loaders/text/betula-term-report-target-assessment-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"), position = 12000),
    @ActionReference(path = "Loaders/text/betula-term-report-target-assessment-context/Actions", id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), separatorBefore = 70000, position = 71000),
    @ActionReference(path = "Loaders/text/betula-term-report-target-assessment-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), separatorBefore = 1000000, position = 1100000)
})
class XmlTargetAssessmentProviderNode extends AssessmentProviderNode<XmlTargetAssessmentProvider> {

    XmlTargetAssessmentProviderNode(XmlTargetAssessmentProvider p) {
        super(p);
    }

    @Override
    public void setName(String s) {
        provider.setDisplayNameImpl(s);
        setDisplayName(provider.getDisplayName());
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/text/betula-term-report-target-assessment-context/Actions").stream()
                .map(Action.class::cast)
                .toArray(Action[]::new);
    }

    @Override
    public Action getPreferredAction() {
        return Actions.forID("System", "org.openide.actions.RenameAction");
    }

//    @Override
//    protected void createPasteTypes(Transferable t, List<PasteType> s) {
//        super.createPasteTypes(t, s); //To change body of generated methods, choose Tools | Templates.
//    }
//    @Override
//    public PasteType getDropType(final Transferable t, int action, int index) {
//        final Node n = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
//        if (n != null) {
//            final EditableProblem p = n.getLookup().lookup(EditableProblem.class);
//            if (p != null && !p.isBasket() && !this.equals(n.getParentNode())) {
//                return new PasteType() {
//                    @Override
//                    public Transferable paste() throws IOException {
//                        ((EditableBasket) problem).addProblem(p);
////                        if ((action & DnDConstants.ACTION_MOVE) != 0) {
////                            dropNode.getParentNode().getChildren().remove(new Node[]{dropNode});
////                        }
//                        return null;
//                    }
//                };
//            }
//        }
//        return null;
//    }
}
