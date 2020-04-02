/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.model.XmlNumberAssessmentProvider.XmlProviderReference;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    //    @ActionReference(path = "Loaders/text/betula-term-report-assessment-reference-context/Actions", id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"), position = 12000),
    @ActionReference(path = "Loaders/text/betula-term-report-assessment-reference-context/Actions", id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), separatorBefore = 1000000, position = 1100000)
})
class ProviderReferenceNode extends AbstractNode {

    private final XmlProviderReference reference;
    private final Listener listener = new Listener();

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    ProviderReferenceNode(XmlProviderReference ref) {
        super(Children.LEAF);
        this.reference = ref;
        setDisplayName(ref.getReferenced().getDisplayName());
        setIconBaseWithExtension("org/thespheres/betula/termreport/resources/tables-relation.png");
        final PropertyChangeListener pcl = WeakListeners.propertyChange(listener, reference.getReferenced());
        reference.getReferenced().addPropertyChangeListener(pcl);
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
        reference.remove();
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/text/betula-term-report-assessment-reference-context/Actions").stream()
                .map(Action.class::cast)
                .toArray(Action[]::new);
    }

    private class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (AssessmentProvider.PROP_DISPLAYNAME.equals(evt.getPropertyName())) {
                setDisplayName(reference.getReferenced().getDisplayName());
            }
        }

    }

}
