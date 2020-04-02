/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

/**
 *
 * @author boris.heithecker
 */
public interface Ordered extends Comparable<Ordered> {

    public int getPosition();

    @Override
    default public int compareTo(Ordered o) {
        return Integer.compare(getPosition(), o.getPosition());
    }
}
