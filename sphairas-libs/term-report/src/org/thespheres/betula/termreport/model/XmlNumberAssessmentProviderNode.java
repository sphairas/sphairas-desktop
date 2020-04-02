/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.model;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.Actions;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.PasteType;
import org.thespheres.betula.termreport.TargetAssessmentProvider;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    @ActionReference(path = "Loaders/text/betula-term-report-number-assessment-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.MoveUpAction"), position = 11100),
    @ActionReference(path = "Loaders/text/betula-term-report-number-assessment-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.MoveDownAction"), position = 11200),
    @ActionReference(path = "Loaders/text/betula-term-report-number-assessment-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"), position = 12000),
    @ActionReference(path = "Loaders/text/betula-term-report-number-assessment-context/Actions", id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), separatorBefore = 70000, position = 71000),
    @ActionReference(path = "Loaders/text/betula-term-report-number-assessment-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), separatorBefore = 1000000, position = 1100000)
})
public class XmlNumberAssessmentProviderNode extends AbstractNode {

    private final XmlNumberAssessmentProvider provider;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public XmlNumberAssessmentProviderNode(XmlNumberAssessmentProvider np) {
        super(np.children, np.getLookup());
        this.provider = np;
        super.setName(np.getId());
        setDisplayName(np.getDisplayName());
        setIconBaseWithExtension("org/thespheres/betula/termreport/resources/table-sum.png");
    }

    @Override
    public void setName(String s) {
        provider.setDisplayNameImpl(s);
        setDisplayName(provider.getDisplayName());
    }

//    @Override
//    public String getHtmlDisplayName() {
//        String dN = getDisplayName();
//        if (problem.isBasket()) {
//            return "<html><b>" + dN + "</html>";
//        } else {
//            return "<html>" + dN + "</html>";
//        }
//    }
    @Override
    public boolean canRename() {
        return true;
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
    public Action getPreferredAction() {
        return Actions.forID("Window", "org.thespheres.betula.termreport.ui.NumberAssessmentConfigurationTopComponent");
    }

//    @Override
//    public Action getPreferredAction() {
//        return SystemAction.get(RenameAction.class);
//    }
    @Override
    public void destroy() throws IOException {
        provider.remove();
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/text/betula-term-report-number-assessment-context/Actions").stream()
                .map(Action.class::cast)
                .toArray(Action[]::new);
    }

    @Override
    public PasteType getDropType(final Transferable t, int action, int index) {
        final Node n = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        if (n != null) {
            final TargetAssessmentProvider p = n.getLookup().lookup(TargetAssessmentProvider.class);
            if (p != null && !this.equals(n.getParentNode())) {
                return new PasteType() {
                    @Override
                    public Transferable paste() throws IOException {
                        try {
                            provider.addReference(p);
                        } catch (IllegalStateException e) {
                            //thrown if p already referenced in provider, ignore drop
                        }
//                        if ((action & DnDConstants.ACTION_MOVE) != 0) {
//                            dropNode.getParentNode().getChildren().remove(new Node[]{dropNode});
//                        }
                        return null;
                    }
                };
            }
        }
        return null;
    }
}
