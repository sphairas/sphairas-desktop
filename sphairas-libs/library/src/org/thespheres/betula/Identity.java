/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
public abstract class Identity<I> {

    public abstract I getId();

    public abstract String getAuthority();

    @Override
    public String toString() {
        return "{" + getAuthority() + "}" + getId().toString();
    }

}
