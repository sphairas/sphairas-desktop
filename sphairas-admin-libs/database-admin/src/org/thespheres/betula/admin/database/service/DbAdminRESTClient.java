/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.service;

import java.io.IOException;
import java.net.URI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.util.Exceptions;
import org.thespheres.betula.database.DBAdminTask;
import org.thespheres.betula.database.DBAdminTaskResult;
import org.thespheres.betula.database.DbAdminService;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.services.ui.util.HttpUtilities;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class DbAdminRESTClient implements DbAdminService {

    private final static JAXBContext jaxb;
    private final DbAdminRESTProvider service;
    private final URI baseURI;

    static {
        try {
            jaxb = JAXBContext.newInstance(DBAdminTask.class, DBAdminTaskResult.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    DbAdminRESTClient(DbAdminRESTProvider service, URI baseURI) {
        this.service = service;
        this.baseURI = baseURI;
    }

    @Override
    public DBAdminTaskResult submitTask(DBAdminTask task) {
        final URI uri = baseURI.resolve("submit-task");
        try {
            return HttpUtilities.post(service, uri, out -> {
                try {
                    jaxb.createMarshaller().marshal(task, out);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }, "application/xml", is -> {
                try {
                    return (DBAdminTaskResult) jaxb.createUnmarshaller().unmarshal(is);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            });
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
