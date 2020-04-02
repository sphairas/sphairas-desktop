/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

import java.io.Serializable;

/**
 *
 * @author boris.heithecker
 */
public class AppResourceEvent implements JMSEvent, Serializable {

    private final String resource;

    public AppResourceEvent(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

}
