/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ws;

import javax.xml.ws.WebFault;

/**
 *
 * @author boris.heithecker
 */
@WebFault(name = "UnauthorizedFault", targetNamespace = "http://web.service.betula.thespheres.org/")
public class UnauthorizedException extends ServiceException {

    private UnauthorizedFault faultInfo;

    public UnauthorizedException(String message, UnauthorizedFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public UnauthorizedException(String message, UnauthorizedFault faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public UnauthorizedFault getFaultInfo() {
        return faultInfo;
    }
}
