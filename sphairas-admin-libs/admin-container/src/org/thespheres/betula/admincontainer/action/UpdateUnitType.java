/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import org.openide.util.NbBundle;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"UpdateUnitType.UPDATE=Gruppenliste ändern", 
    "UpdateUnitType.KEEP=Gruppenliste nicht ändern"})
enum UpdateUnitType {

    UPDATE(NbBundle.getMessage(UpdateUnitType.class, "UpdateUnitType.UPDATE")), 
    KEEP(NbBundle.getMessage(UpdateUnitType.class, "UpdateUnitType.KEEP"));
    private final String displayName;

    private UpdateUnitType(String display) {
        this.displayName = display;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
