/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 *
 * @author boris.heithecker
 */
public enum Action implements Serializable {
    @XmlEnumValue("request-completion")
    REQUEST_COMPLETION, //bitte um informationen NACH TICKET
    @XmlEnumValue("return-completion")
    RETURN_COMPLETION, //hier auch update, wenn z.B. nur ein Teil einer Liste übermittelt wird NACH TICKET
    @XmlEnumValue("file")
    FILE, // einreiche, updaten etc OHNE TICKET
    CREATE, //OHNE TICKET
    @XmlEnumValue("confirm")
    CONFIRM,
    @XmlEnumValue("annul")
    ANNUL
}
