/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme;

import java.util.WeakHashMap;
import org.openide.util.Lookup;
import org.thespheres.betula.TermId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;

/**
 *
 * @author boris.heithecker
 */
public class Terms {

    private static final WeakHashMap<TermId, Term> CACHE = new WeakHashMap<>();

    public static Term forTermId(TermId id) {
        if (CACHE.containsKey(id)) {
            return CACHE.get(id);
        }
        for (final SchemeProvider sp : Lookup.getDefault().lookupAll(SchemeProvider.class)) {
            for (final TermSchedule ts : sp.getAllSchemes(TermSchedule.class)) {
                try {
                    final Term ret = ts.resolve(id);
                    CACHE.put(id, ret);
                    return ret;
                } catch (TermNotFoundException | IllegalAuthorityException ex) {
                }
            }
        }
        return null;
    }
}
