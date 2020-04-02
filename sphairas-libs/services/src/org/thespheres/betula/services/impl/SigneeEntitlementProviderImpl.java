/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.impl;

import java.util.Arrays;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.util.SigneeEntitlement;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = SigneeEntitlement.Provider.class)
public class SigneeEntitlementProviderImpl implements SigneeEntitlement.Provider {

    private final SigneeEntitlementImpl[] arr = new SigneeEntitlementImpl[]{new SigneeEntitlementImpl("entitled.signee"),
        new SigneeEntitlementImpl("trusted.signee")};

    @Override
    public List<SigneeEntitlement> getAll() {
        return Arrays.asList(arr);
    }

    @Override
    public SigneeEntitlement get(String entitlement) {
        return Arrays.stream(arr)
                .filter(i -> i.getEntitlement().equals(entitlement))
                .collect(CollectionUtil.requireSingleOrNull());
    }

}
