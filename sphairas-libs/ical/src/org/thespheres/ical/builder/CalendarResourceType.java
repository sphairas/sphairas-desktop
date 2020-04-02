/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.builder;

/**
 *
 * @author boris.heithecker
 */
public abstract class CalendarResourceType {

    private final String type;

    protected CalendarResourceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
    
}
