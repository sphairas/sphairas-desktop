/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar;

import org.jdesktop.swingx.renderer.StringValue;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
@Messages({"Priority.undefined=Undefiniert",
    "Priority.highest=Höchst",
    "Priority.higher=Dringlich",
    "Priority.high=Hoch",
    "Priority.higherthanmedium=Mittel bis hoch",
    "Priority.medium=Mittel",
    "Priority.lowerthanmedium=Niedrig bis mittel",
    "Priority.low=Niedrig",
    "Priority.lower=Nicht dringlich",
    "Priority.lowest=Niedrigst"})
public enum Priority {

    UNDEFINED(0),
    HIGHEST(1),
    HIGHER(2),
    HIGH(3),
    HIGHERTHANMEDIUM(4),
    MEDIUM(5),
    LOWERTHANMEDIUM(6),
    LOW(7),
    LOWER(8),
    LOWEST(9);
    private final int level;
    private final String localizedDisplayName;

    private Priority(int level) {
        this.level = level;
        this.localizedDisplayName = NbBundle.getMessage(Priority.class, "Priority." + name().toLowerCase());
    }

    public int getLevel() {
        return level;
    }

    public String getLocalizedDisplayName() {
        return localizedDisplayName;
    }

    public static final StringValue stringValue() {
        return (StringValue) o -> o instanceof Priority ? ((Priority) o).getLocalizedDisplayName() : null;
    }
}
