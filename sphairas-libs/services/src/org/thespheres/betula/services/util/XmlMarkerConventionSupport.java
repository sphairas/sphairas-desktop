/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition;

/**
 *
 * @author boris.heithecker
 */
public class XmlMarkerConventionSupport {

    private static JAXBContext jaxb;

    private XmlMarkerConventionSupport() {
    }

    private static JAXBContext getJAXB() {
        synchronized (XmlMarkerConventionSupport.class) {
            if (jaxb == null) {
                try {
                    jaxb = JAXBContext.newInstance(XmlMarkerConventionDefinition.class);
                } catch (JAXBException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return jaxb;
    }

    public static XmlMarkerConventionDefinition create(final Map<String, ?> attr) throws IOException {
        try {
            final String resource = (String) attr.get("resource");
            if (resource != null) {
                return load(resource);
            }
        } catch (ClassCastException ex) {
            throw new IOException(ex);
        }
        try {
            final URL url = (URL) attr.get("definition-url");
            if (url != null) {
                return load(url);
            }
        } catch (ClassCastException ex) {
            throw new IOException(ex);
        }
        throw new IOException("No definition not found.");
    }

    public static XmlMarkerConventionDefinition load(final InputStream is) throws IOException {
        try {
            return (XmlMarkerConventionDefinition) getJAXB().createUnmarshaller().unmarshal(is);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    public static XmlMarkerConventionDefinition load(final Path path) throws IOException {
        try (final InputStream is = Files.newInputStream(path)) {
            return (XmlMarkerConventionDefinition) getJAXB().createUnmarshaller().unmarshal(is);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    public static XmlMarkerConventionDefinition load(final URL url) throws IOException {
        try (final InputStream is = url.openStream()) {
            return (XmlMarkerConventionDefinition) getJAXB().createUnmarshaller().unmarshal(is);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    public static XmlMarkerConventionDefinition load(final String resource) throws IOException {
//        final ClassLoader sysCl = Lookup.getDefault().lookup(ClassLoader.class);
        final ClassLoader sysCl = Thread.currentThread().getContextClassLoader();
        try (final InputStream is = sysCl.getResourceAsStream(resource)) {
            if (is != null) {
                return (XmlMarkerConventionDefinition) getJAXB().createUnmarshaller().unmarshal(is);
            }
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
        try (final InputStream is = ClassLoader.getSystemResourceAsStream(resource)) {
            if (is != null) {
                return (XmlMarkerConventionDefinition) getJAXB().createUnmarshaller().unmarshal(is);
            }
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
        throw new IOException("Cannot find resource " + resource + ".");
    }
}
