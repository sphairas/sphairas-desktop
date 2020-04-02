/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.listprint;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.openide.util.*;
import org.openide.xml.XMLUtil;
import org.plutext.jaxb.xslfo.Root;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.w3c.dom.Document;

/**
 *
 * @author boris.heithecker
 */
public class Formatter {

    private static Formatter instance;
    private final FopFactory fopFactory;
    private static Object jaxb;
    private final TransformerFactory factory;

    private Formatter() {
        this(Utilities.toURI(new File(".")));
    }

    private Formatter(final URI baseURI) {
        final FopFactoryBuilder builder = new FopFactoryBuilder(baseURI);
//            ResourceResolverFactory.createDefaultResourceResolver()
        fopFactory = builder.build();
        factory = TransformerFactory.newInstance();
//            String path = System.getProperty("com.sun.aas.instanceRoot");
//            Path p = Paths.get(path, "kgs-config/");
//            URL base = p.toUri().toURL();
//            foUserAgent.setBaseURL(base.toExternalForm());
    }

    public static Formatter create(final URI baseURI) {
        return new Formatter(baseURI);
    }

    public static Formatter getDefault() {
        synchronized (Formatter.class) {
            if (instance == null) {
                instance = new Formatter();
            }
            return instance;
        }
    }

    private static synchronized JAXBContext getJAXB() throws IOException {
        if (jaxb == null) {
            try {
                jaxb = JAXBContext.newInstance("org.plutext.jaxb.xslfo", Lookup.getDefault().lookup(ClassLoader.class));
            } catch (JAXBException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (jaxb instanceof JAXBContext) {
            return (JAXBContext) jaxb;
        }
        throw new IOException((JAXBException) jaxb);
    }

    public void transform(final Root rt, final OutputStream out, final String mime) throws IOException {
        if ("text/xml".equals(mime)) {
            try {
                // marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
                final Marshaller marshaller = getJAXB().createMarshaller();
                marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
                marshaller.marshal(rt, out);
            } catch (JAXBException ex) {
                throw new IOException(ex);
            } finally {
                try {
                    out.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        } else {
            final DOMResult result = new DOMResult();
            try {
                getJAXB().createMarshaller().marshal(rt, result);
            } catch (JAXBException ex) {
                throw new IOException(ex);
            }

            transform(new DOMSource(result.getNode()), null, out, mime);
        }
    }

    public void transform(final Source src, final Templates transform, final OutputStream out, final String mime) throws IOException {
        transform(src, transform, out, mime, null);
    }

    public void transform(final Source src, final Templates transform, final OutputStream out, final String mime, final String foDumpFile) throws IOException {
        try {
            // Construct fop with desired output format
            final FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            final Fop fop = fopFactory.newFop(mime, foUserAgent, out);

            final Source fopsrc;
            if (transform != null) {
                final Transformer transformer = transform.newTransformer();
                transformer.setParameter("versionParam", "2.0");
                final DOMResult res = new DOMResult();
                transformer.transform(src, res);
                fopsrc = new DOMSource(res.getNode());
                if (foDumpFile != null) {
                    dump((Document) res.getNode(), foDumpFile + ".fo");
                }
            } else {
                fopsrc = src;
            }
            final Transformer transformer = factory.newTransformer();
            final SAXResult finalres = new SAXResult(fop.getDefaultHandler());
            transformer.transform(fopsrc, finalres);
//                 FormattingResults res = fop.getResults();
//                 PageSequenceResults psr;
//                 psr.
        } catch (FOPException | TransformerException ex) {
            throw new IOException(ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }

    protected void dump(final Document node, final String file) {
        final String nbuser = System.getProperty("netbeans.user");
        if (nbuser != null) {
            final Path dir = Paths.get(nbuser, "var/log");
            final Path f = dir.resolve(file);
            try (final OutputStream os = Files.newOutputStream(f)) {
                XMLUtil.write(node, os, Charset.defaultCharset().name());
            } catch (IOException ioex) {
                final String msg = "An exception has ocurred dumping " + file + ".";
                PlatformUtil.getCodeNameBaseLogger(Formatter.class).log(Level.SEVERE, msg, ioex);
            }
        }
    }
}
