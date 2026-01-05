/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.web;

import java.io.IOException;
import java.net.URI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.util.Exceptions;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.NotFoundException;
import org.thespheres.betula.services.ws.SyntaxException;
import org.thespheres.betula.services.ws.UnauthorizedException;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class BetulaRESTServicePort implements BetulaWebService {

    private final static JAXBContext jaxb;
    private final AdminRESTProvider service;
    private final URI baseURI;

    static {
        try {
            jaxb = JAXBContext.newInstance(Container.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    BetulaRESTServicePort(AdminRESTProvider service, URI baseURI) {
        this.service = service;
        this.baseURI = baseURI;
    }

    @Override
    public Container fetch(DocumentId ticket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Container solicit(final Container container) throws NotFoundException, UnauthorizedException, SyntaxException {
        final URI uri = baseURI.resolve("solicit");
        try {
            return HttpUtilities.post(service, uri, out -> {
                try {
                    jaxb.createMarshaller().marshal(container, out);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }, "application/xml", is -> {
                try {
                    return (Container) jaxb.createUnmarshaller().unmarshal(is);
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
