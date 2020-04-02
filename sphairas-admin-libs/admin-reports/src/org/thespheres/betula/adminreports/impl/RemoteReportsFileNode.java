/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
class RemoteReportsFileNode extends AbstractNode {

    private final RemoteReportsModel model;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    RemoteReportsFileNode(RemoteReportsModel rrm, Lookup lkp) {
        super(Children.LEAF, lkp);
        setName(rrm.getId());
        setDisplayName(rrm.getDisplayName());
        setIconBaseWithExtension("org/thespheres/betula/admin/units/resources/reports-stack.png");
        this.model = rrm;
    }

    @Override
    public Action getPreferredAction() {
        return OpenAction.get(OpenAction.class);
    }

}
