/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.soap.Addressing;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 */
//@HandlerChain //--> use this to add signature
@WebService(name = "BetulaWebService", targetNamespace = "http://web.service.betula.thespheres.org/")
@Addressing(enabled=true)
public interface BetulaWebService {

    /**
     *
     * @param ticket
     * @return returns org.thespheres.betula.entities.ws.ContainerType
     */
    @WebMethod
    @WebResult(targetNamespace = "http://www.thespheres.org/xsd/betula/container.xsd")
    @RequestWrapper(localName = "fetch", targetNamespace = "http://web.service.betula.thespheres.org/", className = "org.thespheres.betula.local.ws.Fetch")
    @ResponseWrapper(localName = "fetchResponse", targetNamespace = "http://web.service.betula.thespheres.org/", className = "org.thespheres.betula.local.ws.FetchResponse")
    @Action(input = "http://web.service.betula.thespheres.org/BetulaService/fetchRequest", output = "http://web.service.betula.thespheres.org/BetulaService/fetchResponse")
    public Container fetch(
            @WebParam(name = "ticket", targetNamespace = "") DocumentId ticket);

    /**
     *
     * @param container
     * @return returns org.thespheres.betula.entities.ws.ContainerType
     * @throws org.thespheres.betula.services.ws.NotFoundException
     * @throws org.thespheres.betula.services.ws.UnauthorizedException
     * @throws org.thespheres.betula.services.ws.SyntaxException
     */
    @WebMethod
    @WebResult(targetNamespace = "http://www.thespheres.org/xsd/betula/container.xsd")
    @RequestWrapper(localName = "solicit", targetNamespace = "http://web.service.betula.thespheres.org/", className = "org.thespheres.betula.local.ws.SolicitExt")
    @ResponseWrapper(localName = "solicitResponse", targetNamespace = "http://web.service.betula.thespheres.org/", className = "org.thespheres.betula.local.ws.SolicitResponseExt")
    @Action(input = "http://web.service.betula.thespheres.org/BetulaService/solicitRequest", output = "http://web.service.betula.thespheres.org/BetulaService/solicitResponse", fault = {
        @FaultAction(className = UnauthorizedException.class, value = "http://web.service.betula.thespheres.org/BetulaService/solicit/Fault/UnauthorizedFault"),
        @FaultAction(className = SyntaxException.class, value = "http://web.service.betula.thespheres.org/BetulaService/solicit/Fault/SyntaxFault"),
        @FaultAction(className = NotFoundException.class, value = "http://web.service.betula.thespheres.org/BetulaService/solicit/Fault/NotFoundFault")
    })
    public Container solicit(
            @WebParam(name = "container", targetNamespace = "http://www.thespheres.org/xsd/betula/container.xsd") Container container) throws NotFoundException, UnauthorizedException, SyntaxException;

}
