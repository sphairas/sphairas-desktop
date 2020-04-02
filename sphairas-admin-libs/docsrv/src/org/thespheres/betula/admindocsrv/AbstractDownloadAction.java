/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.ui.util.WorkingDateSensitiveAction;

/**
 *
 * @author boris.heithecker
 * @param <C>
 */
@NbBundle.Messages({"AbstractDownloadAction.error.httpResponse=Beim Laden der Datei ist ein Fehler aufgetreten: http-Status {0} ({1})"})
public abstract class AbstractDownloadAction<C> extends WorkingDateSensitiveAction<C> {

    protected Term term;
    protected final String extension;
    protected final String mime;

    protected AbstractDownloadAction(final String mime, final String extension) {
        this.extension = extension;
        this.mime = mime;
    }

    @SuppressWarnings(value = {"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    protected AbstractDownloadAction(Lookup context, Class<C> clz, final String mime, final String extension, boolean multiple, boolean surviveFocusChange) {
        super(context, clz, multiple, surviveFocusChange);
        this.extension = extension;
        this.mime = mime;
    }

    protected Term findCommonTerm() throws IOException {
        final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
        term = null;
        for (PrimaryUnitOpenSupport puos : context.lookupAll(PrimaryUnitOpenSupport.class)) {
            final TermSchedule ts = puos.findTermSchedule();
            Term t = wd.isNow() ? ts.getCurrentTerm() : ts.getTerm(wd.getCurrentWorkingDate());
            if (term == null) {
                term = t;
            } else if (!term.equals(t)) {
                throw new IOException("Unequal terms.");
            }
        }
        if (term == null) {
            throw new IOException("No term.");
        }
        return term;
    }

    protected void doDownload(final String uri, final Path tmp, final WebServiceProvider wsp) throws IOException {
        final HttpClientBuilder builder
                = //                .setDefaultCredentialsProvider(credsProvider)
                HttpClients.custom();
        if (wsp instanceof WebProvider.SSL) {
            final SSLContext sc = ((WebProvider.SSL) wsp).getSSLContext();
            final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sc, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            builder.setSSLSocketFactory(sslsf);
        }
        final CloseableHttpClient httpclient = builder.build();
        final HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("Accept", "application/pdf");
        try (final CloseableHttpResponse response = httpclient.execute(httpGet)) {
            // The underlying HTTP connection is still held by the response object
            // to allow the response content to be streamed directly from the network socket.
            // In order to ensure correct deallocation of system resources
            // the user MUST call CloseableHttpResponse#close() from a finally clause.
            // Please note that if response content is not fully consumed the underlying
            // connection cannot be safely re-used and will be shut down and discarded
            // by the connection manager.
            final StatusLine sl = response.getStatusLine();
            if (sl.getStatusCode() != 200) {
                String msg = NbBundle.getMessage(AbstractDownloadAction.class, "AbstractDownloadAction.error.httpResponse", sl.getStatusCode(), sl.getReasonPhrase());
                throw new IOException(msg);
            }
            HttpEntity entity1 = response.getEntity();
            try (final InputStream is = entity1.getContent()) {
                Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
                EntityUtils.consume(entity1);
            }
        }
    }
}
