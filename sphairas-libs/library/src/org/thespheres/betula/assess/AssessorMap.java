/*
 * AssessorMap.java
 *
 * Created on 16. November 2007, 22:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.util.Iterator;
import org.thespheres.betula.Identity;

/**
 *
 * @author Boris Heithecker
 * @param <I>
 * @param <T>
 */
public interface AssessorMap<I extends Identity, T extends Comparable> extends Iterable<AssessorMapEntry<T>> {
    
    public AssessorMapEntry<T> createAndAdd(I key, T value);
    
    public void remove(I key);//Detach all listeners
    
    public boolean contains(I key);
    
    @Override
    public Iterator<AssessorMapEntry<T>> iterator();
    
}
