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
public class MessageContent {

    protected String text;
    protected boolean isTextEncoded;
    protected int priority;

    public MessageContent() {
    }

    public MessageContent(String text, boolean isTextEncoded, int priority) {
        this.text = text;
        this.isTextEncoded = isTextEncoded;
        this.priority = priority;
    }

    public String getText() {
        return text;
    }

    public boolean isIsTextEncoded() {
        return isTextEncoded;
    }

    public int getPriority() {
        return priority;
    }

}
