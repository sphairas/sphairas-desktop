/*
 * Distribution.java
 *
 * Created on 18. Mai 2007, 13:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.util.List;

/**
 *
 * @author Boris Heithecker
 * @param <T>
 */
public interface Distribution<T extends Comparable> {

    public String getName();

    public String getDisplayName();

    public List<T> distribute(T ceiling); //index 0 entspricht Grade.SECHS

    public List<T> getDistributionValues();

}
