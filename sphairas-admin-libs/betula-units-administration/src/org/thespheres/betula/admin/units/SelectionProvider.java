/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author boris.heithecker
 */
public abstract class SelectionProvider implements Lookup.Provider {

    protected final InstanceContent ic = new InstanceContent();
    private final Lookup lookup = new AbstractLookup(ic);

    @SuppressWarnings("LeakingThisInConstructor")
    protected SelectionProvider() {
        ic.add(this);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

}
