/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.mitarbeit.collect;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author boris.heithecker
 */
class CollGrade implements Grade.Cookie<String> {

    private final String id;
    private final String text;
    private final String slabel;
    private String cookie;
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    public static final Grade CLEARED = new CollGrade("cleared", "Beglichen", "o.k.");
    public static final Grade CLEARED_PARTIALLY = new CollGrade("cleared-partially", "Z.t. beglichen", "uv.");
    public static final Grade NOT_CLEARED_YET = new CollGrade("uncleared", "Ausstehend", "f.");
    public static final Grade EXEMPT = new CollGrade("exempt", "Entfällt", "e");
    public static final Grade[] ALL;
    public static final String PROP_COOKIE = "cookie";

    static {
        ALL = new Grade[]{
            CLEARED,
            CLEARED_PARTIALLY,
            NOT_CLEARED_YET,
            EXEMPT
        };
    }

    CollGrade(String id, String text, String shortLabel) {
        this.id = id;
        this.text = text;
        this.slabel = shortLabel;
    }

    @Override
    public Grade getNextLower() {
        return null;
    }

    @Override
    public Grade getNextHigher() {
        return null;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public String getLongLabel(Object... args) {
        return text;
    }

    @Override
    public String getShortLabel() {
        return slabel;
    }

    @Override
    public String getConvention() {
        return Collect.CONVENTION;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getCookie() {
        return cookie;
    }

    @Override
    public void setCookie(String value) {
        String old = cookie;
        cookie = value;
        pSupport.firePropertyChange(PROP_COOKIE, old, cookie);
    }

    @Override
    public Class<String> getCookieClass() {
        return String.class;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pSupport.removePropertyChangeListener(l);
    }
}
