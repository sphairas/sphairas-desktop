/*
 * Allocator.java
 *
 * Created on 19. Mai 2007, 11:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

/**
 *
 * @author Boris Heithecker
 * @param <T>
 */
public interface Allocator<T extends Comparable> {

    public Grade allocate(T value);

    public T getFloor(Grade grade);

    public void setFloor(Grade grade, T value);

    public T getCeiling(Grade grade);

}
