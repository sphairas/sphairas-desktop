/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.impl;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.util.SigneeEntitlement;

/**
 *
 * @author boris.heithecker
 */
@Messages({"entitled.signee=Unterzeichner",
    "trusted.signee=Vertrauensperson"})
class SigneeEntitlementImpl extends SigneeEntitlement {

    SigneeEntitlementImpl(String entitlement) {
        super(entitlement);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SigneeEntitlementImpl.class, getEntitlement());
    }

}
