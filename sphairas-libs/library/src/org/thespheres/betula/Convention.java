/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

import java.util.ServiceLoader;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public interface Convention<T> {

    public T find(String id);

    public String getDisplayName();

    public String getName();

    public static Convention findConvention(String convention) {
        final ServiceLoader<Convention> loader = ServiceLoader.load(Convention.class);
        for (Convention<?> con : loader) {
            if (con.getName().equals(convention)) {
                return con;
            }
        }
        return null;
    }

}
