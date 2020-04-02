/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ChannelObject {

    private String name;
    private String type;
    private String display;

    public ChannelObject() {
    }

    public ChannelObject(String name, String type, String currentDisplay) {
        this.name = name;
        this.type = type;
        this.display = currentDisplay;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCurrentDisplayName() {
        return display;
    }

}
