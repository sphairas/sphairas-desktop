/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.soap.Addressing;
import org.thespheres.betula.database.DBAdminTask;
import org.thespheres.betula.database.DBAdminTaskResult;

/**
 *
 * @author boris.heithecker
 */
//@HandlerChain //--> use this to add signature
@WebService(name = "DbAdminService", targetNamespace = "http://dbadmin.service.betula.thespheres.org/")
@Addressing(enabled = true)
public interface DbAdminService {

    @WebMethod(operationName = "submitTask")
//    @WebResult(targetNamespace = "http://www.thespheres.org/xsd/betula/db-admin.xsd")
    @RequestWrapper(localName = "submitTask", targetNamespace = "http://dbadmin.service.betula.thespheres.org/", className = "org.thespheres.betula.admin.database.service.Submit")
    @ResponseWrapper(localName = "submitTaskResponse", targetNamespace = "http://dbadmin.service.betula.thespheres.org/", className = "org.thespheres.betula.admin.database.service.SubmitResponse")
    @Action(input = "http://dbadmin.service.betula.thespheres.org/DbAdminService/submitRequest", output = "http://dbadmin.service.betula.thespheres.org/DbAdminService/submitResponse")
    public DBAdminTaskResult submitTask(@WebParam(name = "task") DBAdminTask task);
}
