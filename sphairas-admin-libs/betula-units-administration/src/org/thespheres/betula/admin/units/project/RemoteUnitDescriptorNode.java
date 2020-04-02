/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.project;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.thespheres.betula.admin.units.MultiUnitOpenSupport;

/**
 *
 * @author boris.heithecker
 */
class RemoteUnitDescriptorNode extends DataNode {

    @SuppressWarnings("OverridableMethodCallInConstructor")
    RemoteUnitDescriptorNode(RemoteUnitDescriptorDataObject data, Lookup lookup) {
        super(data, Children.LEAF, lookup);
        MultiUnitOpenSupport rud = data.getLookup().lookup(MultiUnitOpenSupport.class);
        setDisplayName(rud.getDisplayName());
    }

}
