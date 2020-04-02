/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.mitarbeit;

import org.thespheres.betula.assess.Grade;

/**
 *
 * @author boris.heithecker
 */
class HAGrade implements Grade {

    private final String id;
    private final String text;
    private final String slabel;
    public static final Grade UNDEF = new HAGrade("undef", "---", "?");
    public static final Grade OK = new HAGrade("ok", "Hausaufgaben i.O.", "HA");
    public static final Grade FEHLEND = new HAGrade("f", "Ohne Hausaufgaben", "Ohne HA");
    public static final Grade PLUS = new HAGrade("ok_p", "Hausaufgaben (+)", "HA (+)");
    public static final Grade MINUS = new HAGrade("ok_m", "Hausaufgaben (-)", "HA (-)");
    public static final Grade ENTSCHULDIGT = new HAGrade("e", "Ohne Hausaufgaben (entschuldigt)", "Ohne HA (e)");
    public static final Grade[] ALL;

    static {
        ALL = new Grade[]{
            FEHLEND,
            ENTSCHULDIGT,
            OK,
            PLUS,
            MINUS,
            UNDEF
        };
    }

    HAGrade(String id, String text, String shortLabel) {
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
    public String getLongLabel(Object... formattingArgs) {
        return text;
    }

    @Override
    public String getShortLabel() {
        return slabel;
    }

    @Override
    public String getConvention() {
        return Hausaufgaben.CONVENTION;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
