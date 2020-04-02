/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportUtil;

/**
 *
 * @author boris.heithecker
 * @param <I> JAXB implementation class
 * @param <C>
 */
@NbBundle.Messages({"SourceTargetLinksAccess.download.result={1} von {0} geladen.",
    "SourceTargetLinksAccess.download.failure={2} konnten nicht von {0} geladen werden (Status : {1}).",
    "SourceTargetLinksAccess.upload.result={1} nach {0} geladen.",
    "SourceTargetLinksAccess.writeAssoc.success={0} geschrieben.",
    "SourceTargetLinksAccess.message.loadAssocFromFile={1} aus Datei {0} geladen.",
    "SourceTargetLinksAccess.message.newAssoc=Verwende neue leere {0}-Daten."})
public abstract class SourceTargetLinksAccess<I, C extends ImportTarget> {

    protected boolean useFileOnly;
    private WebProvider.SSL webService;
    protected JAXBContext ctx2;
    private final Class<I> jaxbImpl;
    private final String name;
    public final RequestProcessor RP = new RequestProcessor(getClass());

    protected SourceTargetLinksAccess(Class<I> jaxbImplClass, String name) {
        this.jaxbImpl = jaxbImplClass;
        this.name = name;
    }

    public void run(C config) {
        try {
            loadSourceTargetLinks(config);
//            assoc.notifyAll();
        } catch (IOException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
        }
    }

    protected WebProvider.SSL getWebService(C config) throws IOException {
        if (webService == null) {
            WebServiceProvider wsp = config.getWebServiceProvider();
            if (!(wsp instanceof WebProvider.SSL)) {
                throw new IOException("WebProvider must be WebProvider.Secure");
            }
            webService = (WebProvider.SSL) wsp;
        }
        return webService;
    }

    protected JAXBContext getJAXB(C config) throws IOException {
        if (ctx2 == null) {
            try {
                ctx2 = JAXBContext.newInstance(jaxbImpl);
            } catch (JAXBException ex) {
                throw new IOException(ex);
            }
        }
        return ctx2;
    }

    protected void doUpload(I coll, C config) throws IOException {
        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(getWebService(config));
        String assoziationenURL = config.getSourceTargetLinksWebDavUrl(null);
        HttpPut put = new HttpPut(assoziationenURL);
        class CP implements ContentProducer {

            @Override
            public void writeTo(OutputStream out) throws IOException {
                try {
                    getJAXB(config).createMarshaller().marshal(coll, out);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }
        }
        EntityTemplate fe = new EntityTemplate(new CP());
        put.setEntity(fe);
        try ( CloseableHttpResponse resp = httpclient.execute(put)) {
            int s = resp.getStatusLine().getStatusCode();
            if (s >= 200 && s < 300) {
                String msg = NbBundle.getMessage(SourceTargetLinksAccess.class, "SourceTargetLinksAccess.upload.result", assoziationenURL, name);
                ImportUtil.getIO().getOut().println(msg);
            } else {
                throw new IOException("Could not upload resource " + assoziationenURL);
            }
        }
    }

    protected I doDownload(C config) throws IOException {
        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(getWebService(config));
        //TODO: user TermId etc
        String assoziationenURL = config.getSourceTargetLinksWebDavUrl(null);
        HttpGet httpGet = new HttpGet(assoziationenURL);
        CloseableHttpResponse resp = httpclient.execute(httpGet);
        BufferedInputStream is = null;
        I ass = null;
        try {
            int s = resp.getStatusLine().getStatusCode();
            HttpEntity respEntity = resp.getEntity();
            is = new BufferedInputStream(respEntity.getContent());
            try {
                if (s == 200) {
                    final Object result = getJAXB(config).createUnmarshaller().unmarshal(is);
                    ass = jaxbImpl.cast(result);
                    final String msg = NbBundle.getMessage(SourceTargetLinksAccess.class, "SourceTargetLinksAccess.download.result", assoziationenURL, name);
                    ImportUtil.getIO().getOut().println(msg);
                } else {
                    final String msg = NbBundle.getMessage(SourceTargetLinksAccess.class, "SourceTargetLinksAccess.download.failure", assoziationenURL, resp.getStatusLine(), name);
                    ImportUtil.getIO().getErr().println(msg);
                    return null;
                }
            } catch (JAXBException ex) {
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
            EntityUtils.consume(respEntity);
        } finally {
            if (is != null) {
                is.close();
            }
            resp.close();
        }
        if (ass == null) {
            throw new IOException("Could not load resource " + assoziationenURL);
        }
        return ass;
    }

    protected I loadSourceTargetLinks(C config) throws IOException {
        if (config == null) {
            return null;
        }
//        synchronized (assoc) {
        I assoziationen = null;
        //Load assoziationen from Configfile
//            if (afo != null) {
//            assoc[0] = afo;
//                IOException webdavex = null;
        if (!useFileOnly) {
            try {
                assoziationen = doDownload(config);
            } catch (IOException ex) {
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
        }
        if (useFileOnly || assoziationen == null) {
            final Path afo = getSourceTargetLinksFile(config);
            if (afo != null && Files.exists(afo)) {
                try (final InputStream is = Files.newInputStream(afo)) {
                    final Object result = getJAXB(config).createUnmarshaller().unmarshal(is);
                    assoziationen = jaxbImpl.cast(result);
                    final String fileMsg = NbBundle.getMessage(SourceTargetLinksAccess.class, "SourceTargetLinksAccess.message.loadAssocFromFile", afo.toString(), name);
                    ImportUtil.getIO().getOut().println(fileMsg);
                } catch (JAXBException | IOException | ClassCastException ex) {
                    ex.printStackTrace(ImportUtil.getIO().getErr());
                }
            }
        }
        if (assoziationen == null) {
            final String fileMsg = NbBundle.getMessage(SourceTargetLinksAccess.class, "SourceTargetLinksAccess.message.newAssoc", name);
            ImportUtil.getIO().getOut().println(fileMsg);
            assoziationen = createEmpty();
        }
        return assoziationen;
    }

    protected Path getSourceTargetLinksFile(C config) {
        final String cfg = config.getSourceTargetLinksConfigFile(null);
        //TODO: user TermId etc
        return cfg != null ? Paths.get(cfg) : null;
    }

    protected I createEmpty() throws IOException {
        try {
            return jaxbImpl.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public void saveSourceTargetLinks(I assoziationen, C config) {
        if (!overrideSourceTargetLinks(assoziationen, config)) {
            return;
        }
        final Path file = getSourceTargetLinksFile(config); //assoc[0];
        if (file != null && Files.exists(file)) {
            final Path n = file.normalize();
            final Path parent = n.getParent();
            final Path fn = n.getName(file.getNameCount() - 1);
            for (int i = 4; i >= 0; i--) {
                final String to = fn.toString() + ".bak" + Integer.toString(i + 1);
                final String from = fn.toString() + ".bak" + (i == 0 ? "" : Integer.toString(i));
                if (Files.exists(parent.resolve(from))) {
                    try {
                        Files.copy(parent.resolve(from), parent.resolve(to), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        ex.printStackTrace(ImportUtil.getIO().getErr());
                    }
                }
            }
            final String to = fn.toString() + ".bak";
            try {
                Files.copy(file, parent.resolve(to), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
        }
        if (file != null) {
            try (final OutputStream os = Files.newOutputStream(file)) {
                final Marshaller m = getJAXB(config).createMarshaller();
                m.setProperty("jaxb.formatted.output", Boolean.TRUE);
                m.marshal(assoziationen, os);
                ImportUtil.getIO().getOut().println(NbBundle.getMessage(SourceTargetLinksAccess.class, "SourceTargetLinksAccess.writeAssoc.success", file.toString()));
            } catch (JAXBException | IOException ex) {
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
        }
        if (!useFileOnly) {
            try {
                doUpload(assoziationen, config);
            } catch (IOException ex) {
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
        }
    }

    protected abstract boolean overrideSourceTargetLinks(I assoziationen, C config);

}
