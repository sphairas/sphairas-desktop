/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import org.thespheres.betula.TermId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.niedersachsen.impl.SimpleTermSchedule;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
public class NdsTerms {

    public static final String JAHR = "jahr";
    public static final String HALBJAHR = "halbjahr";

    private final static SimpleTermSchedule IMPL = new SimpleTermSchedule("niedersachsen");

    private NdsTerms() {
    }

    public static Term current() {
        return IMPL.getCurrentTerm();
    }

    public static Term getTerm(int jahr, int halbjahr) {
        return IMPL.getTerm(jahr, halbjahr);
    }

    public static Term fromId(TermId id) throws IllegalAuthorityException {
        if (!LSchB.AUTHORITY.equals(id.getAuthority())) {
            IllegalAuthorityException th = new IllegalAuthorityException();
            th.setIllegalIdentity(id);
            throw th;
        }
        int val = id.getId();
        int hj = id.getId() % 2 == 0 ? 2 : 1;
        int jahr = ((hj == 1 ? ++val : val) / 2) + 1699;
        return IMPL.getTerm(jahr, hj);
    }

}
