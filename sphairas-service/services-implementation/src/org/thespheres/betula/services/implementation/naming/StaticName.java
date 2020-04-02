/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.naming;

import java.util.HashMap;
import org.thespheres.betula.services.NamingResolver;

/**
 *
 * @author boris.heithecker
 */
public class StaticName extends NamingResolver.Result {

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public StaticName(String defaultName) {
        super(new HashMap<>());
        elements.put(ELEMENT_DEFAULT_NAME, defaultName);
        addResolverHint(HINT_STATIC_NAME);
    }

    @Override
    public String getResolvedName(Object... params) {
        return getResolvedElement(ELEMENT_DEFAULT_NAME);
    }

}
