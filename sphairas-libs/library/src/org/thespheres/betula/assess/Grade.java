/*
 * Grade.java
 *
 * Created on 27. April 2007, 19:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.beans.PropertyChangeListener;
import org.thespheres.betula.Tag;

/**
 *
 * @author Boris Heithecker
 */
public interface Grade extends Tag {

    public Grade getNextLower();

    public Grade getNextHigher();

    @Override
    public String toString();

    public interface Cookie<T> extends Grade {

        public T getCookie();

        public void setCookie(T t);

        public Class<T> getCookieClass();

        public void addPropertyChangeListener(PropertyChangeListener l);

        public void removePropertyChangeListener(PropertyChangeListener l);
    }

    public interface Biasable extends Grade {

        public Grade getUnbiased();
    }
}
