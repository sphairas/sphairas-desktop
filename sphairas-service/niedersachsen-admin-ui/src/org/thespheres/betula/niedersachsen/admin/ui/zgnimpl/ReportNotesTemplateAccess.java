/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.util.ServiceConfiguration;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"ReportNotesTemplateAccess.downloadReportNotesTemplate.status.error=Bei Laden von {0} ist ein Fehler aufgetreten: {1}"})
class ReportNotesTemplateAccess implements Runnable {

    static final int CHECK_INTERVAL = 10000;
    private final static Map<String, ReportNotesTemplateAccess> TEMPLATES = new HashMap<>();
    private final RequestProcessor RP = new RequestProcessor(ReportNotesTemplateAccess.class.getName());
    private final String url;
    private final RequestProcessor.Task task;
    private TermReportNoteSetTemplate ref = null;
    private static JAXBContext ctx2;
    private final RemoteReportsModel2Impl history;
    private WebProvider.SSL webService;
    private String lastModified;
    private final RequestProcessor.Task checkLM;

    @SuppressWarnings({"LeakingThisInConstructor"})
    private ReportNotesTemplateAccess(final String url, final RemoteReportsModel2Impl model) {
        this.url = url;
        this.history = model;
        task = RP.post(this);
        checkLM = RP.create(this::checkLastModified);
    }

    static TermReportNoteSetTemplate find(final String url, final RemoteReportsModel2Impl model) throws IOException {
        final ReportNotesTemplateAccess access;
        synchronized (TEMPLATES) {
            access = TEMPLATES.computeIfAbsent(url, key -> new ReportNotesTemplateAccess(key, model));
        }
        if (!access.task.isFinished()) {
            if (EventQueue.isDispatchThread()) {
                final long maxWait = ServiceConfiguration.getInstance().getMaxWaitTimeInEDT();
                try {
                    access.task.waitFinished(maxWait);
                } catch (InterruptedException ex) {
                    throw new IOException(ex);
                }
            } else {
                access.task.waitFinished();
            }
        }
        return access.ref;
    }

    @Override
    public void run() {
        try {
            final TermReportNoteSetTemplate found = findReportNotesTemplate();
            ref = found;
            checkLM.schedule(CHECK_INTERVAL);
        } catch (IOException ex) {
            RemoteReportsModel2Impl.notifyError(ex, ex.getLocalizedMessage());
            ref = null;
        }
    }

    protected TermReportNoteSetTemplate findReportNotesTemplate() throws IOException {

        final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(getWebService().getSSLContext(), new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        final CloseableHttpClient httpclient
                = //                .setDefaultCredentialsProvider(credsProvider)
                HttpClients.custom().setSSLSocketFactory(sslsf).build();
        //config.getSourceTargetLinksWebDavUrl(null);
        final HttpGet httpGet = new HttpGet(url);
        final CloseableHttpResponse resp = httpclient.execute(httpGet);
        final StatusLine s = resp.getStatusLine();
        final HttpEntity respEntity = resp.getEntity();
        String lm = Arrays.stream(resp.getAllHeaders())
                .filter(h -> "Last-Modified".equals(h.getName()))
                .map(h -> h.getValue())
                .collect(CollectionUtil.singleOrNull());

        if (s.getStatusCode() == 200) {
            try (final BufferedInputStream is = new BufferedInputStream(respEntity.getContent())) {
                try {
                    return (TermReportNoteSetTemplate) getJAXB().createUnmarshaller().unmarshal(is);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            } finally {
                EntityUtils.consume(respEntity);
                resp.close();
                lastModified = lm;
            }
        } else {
            final String msg = NbBundle.getMessage(ReportNotesTemplateAccess.class, "ReportNotesTemplateAccess.downloadReportNotesTemplate.status.error", url, s.toString());
            throw new IOException(msg);
        }
    }

    private void checkLastModified() {
        String lm = null;
        try {
            lm = findLastModified();
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(ReportNotesTemplateAccess.class).log(Level.FINE, ex.getLocalizedMessage(), ex);
        }
        if (lm != null && !lastModified.equals(lm)) {
            task.schedule(0);
        } else {
            checkLM.schedule(CHECK_INTERVAL);
        }
    }

    protected String findLastModified() throws IOException {
        final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(getWebService().getSSLContext(), new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        final CloseableHttpClient httpclient
                = //                .setDefaultCredentialsProvider(credsProvider)
                HttpClients.custom().setSSLSocketFactory(sslsf).build();
        //config.getSourceTargetLinksWebDavUrl(null);
        final HttpHead httpGet = new HttpHead(url);
        final CloseableHttpResponse resp = httpclient.execute(httpGet);
        final StatusLine s = resp.getStatusLine();
        final String lm = Arrays.stream(resp.getAllHeaders())
                .filter(h -> "Last-Modified".equals(h.getName()))
                .map(h -> h.getValue())
                .collect(CollectionUtil.singleOrNull());
        if (s.getStatusCode() == 200) {
            return lm;
        } else {
            final String msg = "Could not update last modified of resource " + url + "; status code is " + s.toString();
            throw new IOException(msg);
        }
    }

    protected WebProvider.SSL getWebService() throws IOException {
        if (webService == null) {
            final WebServiceProvider wsp = history.support.findWebServiceProvider();
            if (!(wsp instanceof WebProvider.SSL)) {
                throw new IOException("WebProvider must be WebProvider.Secure");
            }
            webService = (WebProvider.SSL) wsp;
        }
        return webService;
    }

    static synchronized JAXBContext getJAXB() throws IOException {
        if (ctx2 == null) {
            try {
                ctx2 = JAXBContext.newInstance(TermReportNoteSetTemplate.class);
            } catch (JAXBException ex) {
                throw new IOException(ex);
            }
        }
        return ctx2;
    }
}
