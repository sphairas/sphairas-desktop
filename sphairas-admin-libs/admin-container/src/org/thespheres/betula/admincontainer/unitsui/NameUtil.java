/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.unitsui;

import java.util.HashMap;
import java.util.Map;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
class NameUtil {

    private final static Map<UnitId, String> COLUMNS_CACHE = new HashMap<>();
    private final static Map<UnitId, String> KEY_CACHE = new HashMap<>();

    static synchronized String findColumnName(final NamingResolver nr, final UnitId unit) {
        return COLUMNS_CACHE.computeIfAbsent(unit, u -> {
            try {
                final NamingResolver.Result r = nr.resolveDisplayNameResult(u);
                final String f = r.getResolvedElement("fach");
                if (f != null) {
                    return f;
                }
            } catch (IllegalAuthorityException e) {
            }
            return "";
        });
    }

    static synchronized String findKey(final NamingResolver nr, final UnitId unit, final Term term) {
        return KEY_CACHE.computeIfAbsent(unit, u -> {
            try {
                final NamingResolver.Result r = nr.resolveDisplayNameResult(u);
                r.addResolverHint("naming.only.level");
                return r.getResolvedName(term);
            } catch (IllegalAuthorityException e) {
            }
            return null;
        });
    }

}
