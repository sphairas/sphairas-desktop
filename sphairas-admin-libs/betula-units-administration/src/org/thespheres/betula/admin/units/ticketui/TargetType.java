/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ticketui;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author boris.heithecker
 */
public class TargetType {

    private final String type;
    private final String displayName;
    private boolean selected = true;

    TargetType(String name) {
        type = name;
        displayName = StringUtils.capitalize(name);
    }

    public String getTargetType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
