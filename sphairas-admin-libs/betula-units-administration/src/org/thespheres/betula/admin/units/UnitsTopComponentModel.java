/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.openide.nodes.Node;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.configui.RemoteUnitNode;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class UnitsTopComponentModel extends ConfigurationTopComponentModel {

    public UnitsTopComponentModel(String provider) {
        super(provider);
    }

    public Node nodeForKey(UnitId key) {
        final Unit ru = AdminUnits.get(getProviderInfo().getURL()).getUnit(key);
//        ru.putClientProperty(RemoteUnitImpl.PROP_NAMING_RESOLVER, getNamingResolver());
//        ru.putClientProperty("provider", getProviderInfo().getURL());
        return new RemoteUnitNode(ru);
    }

    public Comparator<UnitId> comparator() {
        return Comparator.comparing(this::getUnitComparingKey, Collator.getInstance(Locale.getDefault()));
    }

    protected String getUnitComparingKey(UnitId uid) {
        final NamingResolver nr = getNamingResolver();
        try {
            return nr != null ? nr.resolveDisplayName(uid) : uid.getId();
        } catch (IllegalAuthorityException ex) {
            return uid.getId();
        }
    }

    public static interface Provider {

        public List<UnitsTopComponentModel> findAll();

        default public UnitsTopComponentModel find(final String provider) {
            return findAll().stream()
                    .filter(m -> m.getProvider().equals(provider))
                    .collect(CollectionUtil.requireSingleOrNull());
        }

    }
}
