/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ws;

import javax.xml.ws.WebFault;

/**
 *
 * @author boris.heithecker
 */
@WebFault(name = "SyntaxFault", targetNamespace = "http://web.service.betula.thespheres.org/")
public class SyntaxException extends ServiceException  {

    private SyntaxFault faultInfo;

    public SyntaxException(String message, SyntaxFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public SyntaxException(String message, SyntaxFault faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public SyntaxFault getFaultInfo() {
        return faultInfo;
    }
}
