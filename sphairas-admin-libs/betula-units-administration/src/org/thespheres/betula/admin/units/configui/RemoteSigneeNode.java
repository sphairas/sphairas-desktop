/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.configui;

import javax.swing.Action;
import org.openide.awt.Actions;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.admin.units.RemoteSignee;
import org.thespheres.betula.admin.units.SigneesTopComponentModel;

/**
 *
 * @author boris.heithecker
 */
public class RemoteSigneeNode extends AbstractNode {

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public RemoteSigneeNode(final RemoteSignee signee, final SigneesTopComponentModel model) {
        super(Children.create(new RemoteSigneeChildren(signee, model), true), Lookups.fixed(signee, model));
        setName(signee.getWebServiceProvider() + ":" + signee.getSignee().toString());
        setDisplayName(signee.getCommonName());
        setIconBaseWithExtension("org/thespheres/betula/admin/units/resources/user-business-boss.png");
    }

    @Override
    public Action getPreferredAction() {
        return Actions.forID("Window", "org.thespheres.betula.ui.actions.ConfigPanelVisible");
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/application/betula-remotesignee-context/Actions").stream()
                .toArray(Action[]::new);
    }

}
