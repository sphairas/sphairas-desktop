/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.openide.util.Lookup;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class SigneeEntitlement {

    private final String entitlement;

    protected SigneeEntitlement(final String entitlement) {
        this.entitlement = entitlement;
    }

    public static Optional<SigneeEntitlement> find(final String entitlement) {
        return Lookup.getDefault().lookupAll(Provider.class).stream()
                .map(p -> p.get(entitlement))
                .filter(Objects::nonNull)
                .collect(CollectionUtil.requireSingleton());
    }

    public String getEntitlement() {
        return entitlement;
    }

    public abstract String getDisplayName();

    public static interface Provider {

        public List<SigneeEntitlement> getAll();

        public SigneeEntitlement get(final String entitlement);

    }
}
