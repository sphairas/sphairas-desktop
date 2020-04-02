/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;

/**
 *
 * @author boris.heithecker
 */
public class ProviderNode extends AbstractNode {

    private final ProviderInfo provider;

    public ProviderNode(final String provider) {
        this(ProviderRegistry.getDefault().get(provider));
    }

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public ProviderNode(final ProviderInfo provider) {
        super(Children.LEAF, Lookups.singleton(provider));
        this.provider = provider;
        setName(ProviderNode.class.getName() + ":" + provider);
        setDisplayName(provider.getDisplayName());
        setIconBaseWithExtension("org/thespheres/betula/admin/units/resources/server.png");
    }

}
