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
@WebFault(name = "NotFoundFault", targetNamespace = "http://web.service.betula.thespheres.org/")
public class NotFoundException  extends ServiceException  {

    private NotFoundFault faultInfo;

    public NotFoundException(String message, NotFoundFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public NotFoundException(String message, NotFoundFault faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public NotFoundFault getFaultInfo() {
        return faultInfo;
    }
}
