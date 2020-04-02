/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

/**
 *
 * @author boris.heithecker
 */
public interface GradeReference extends Grade {

    //unresolved always, z.B. "Vorschlag: " oder "Vornote: "
    public String getDisplayLabel();
    
}
