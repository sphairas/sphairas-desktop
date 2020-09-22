/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ws;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SyntaxFault", propOrder = {
    "message"
})
public class SyntaxFault implements Serializable {

    private static final long serialVersionUID = 1L;
    protected String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String value) {
        this.message = value;
    }

}
