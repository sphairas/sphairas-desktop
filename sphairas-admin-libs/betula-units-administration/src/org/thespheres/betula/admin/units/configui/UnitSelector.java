/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.configui;

import org.thespheres.betula.UnitId;

/**
 *
 * @author boris.heithecker
 */
public interface UnitSelector {

    public String getDisplayName();

    public boolean matches(final UnitId unit);

}
