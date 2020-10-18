/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author boris.heithecker@gmx.net
 */
class DefaultColumnSet extends AbstractSet<ImportTableColumn> {

    private final HashSet<ImportTableColumn> delegate = new HashSet<>();

    @Override
    public Iterator<ImportTableColumn> iterator() {
        return delegate.iterator();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean add(final ImportTableColumn i) {
        final Iterator<ImportTableColumn> it = delegate.iterator();
        while (it.hasNext()) {
            final ImportTableColumn itc = it.next();
            if (i.columnId().equals(itc.columnId())) {
                it.remove();
            }
        }
        return delegate.add(i);
    }

}
