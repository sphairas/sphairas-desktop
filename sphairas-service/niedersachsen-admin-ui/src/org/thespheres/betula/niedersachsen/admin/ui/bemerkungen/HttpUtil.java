/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openide.util.EditableProperties;
import org.openide.util.NetworkSettings;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.web.ContextCredentials;

/**
 *
 * @author boris.heithecker
 */
class HttpUtil {

    private static JAXBContext JAXB;

    private synchronized static JAXBContext getJAXB() {
        if (JAXB == null) {
            try {
                JAXB = JAXBContext.newInstance(TermReportNoteSetTemplate.class);
            } catch (JAXBException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return JAXB;
    }

    static EditableProperties fetchResourceBundle(final WebProvider wp, String uri) throws IOException {
        final HttpClientBuilder builder = HttpClients.custom();

        class WebCredentials implements Credentials {

            private final ContextCredentials credentials;

            private WebCredentials(ContextCredentials creds) {
                credentials = creds;
            }

            @Override
            public Principal getUserPrincipal() {
                return new BasicUserPrincipal(credentials.getUsername());
            }

            @Override
            public String getPassword() {
                return new String(credentials.getPassword());
            }
        }
        CredentialsProvider credsProvider = null;
        if (wp instanceof ContextCredentials.Provider && ((ContextCredentials.Provider) wp).getContextCredentials() != null) {
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, new WebCredentials(((ContextCredentials.Provider) wp).getContextCredentials()));
            builder.setDefaultCredentialsProvider(credsProvider);
        }
        if (wp instanceof WebProvider.SSL) {
            WebProvider.SSL ws = (WebProvider.SSL) wp;
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(ws.getSSLContext(), new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            builder.setSSLSocketFactory(sslsf);
        }
        final CloseableHttpClient httpclient = builder.build();
        final HttpGet httpGet = new HttpGet(uri);
        //        httpGet.setHeader("Accept", "application/pdf");
        final CloseableHttpResponse response;
        try {
            if (credsProvider != null) {
                response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpGet));
            } else {
                response = httpclient.execute(httpGet);
            }
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
        // The underlying HTTP connection is still held by the response object
        // to allow the response content to be streamed directly from the network socket.
        // In order to ensure correct deallocation of system resources
        // the user MUST call CloseableHttpResponse#close() from a finally clause.
        // Please note that if response content is not fully consumed the underlying
        // connection cannot be safely re-used and will be shut down and discarded
        // by the connection manager.
        final int sc = response.getStatusLine().getStatusCode();
        if (sc != HttpStatus.SC_OK) {
            if (sc == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }
        final HttpEntity entity = response.getEntity();
        try (final InputStream is = entity.getContent()) {
            final EditableProperties ret;
            try (final BufferedInputStream bis = new BufferedInputStream(is)) { //? BufferedEntity?{
                ret = new EditableProperties(false);
                ret.load(bis);
            }
            EntityUtils.consume(entity);
            return ret;
        } finally {
            response.close();
        }
    }

    static void storeResourceBundle(final WebProvider wp, final String uri, final EditableProperties properties) throws IOException {
        final HttpClientBuilder builder = HttpClients.custom();

        class WebCredentials implements Credentials {

            private final ContextCredentials credentials;

            private WebCredentials(ContextCredentials creds) {
                credentials = creds;
            }

            @Override
            public Principal getUserPrincipal() {
                return new BasicUserPrincipal(credentials.getUsername());
            }

            @Override
            public String getPassword() {
                return new String(credentials.getPassword());
            }
        }

        CredentialsProvider credsProvider = null;
        if (wp instanceof ContextCredentials.Provider && ((ContextCredentials.Provider) wp).getContextCredentials() != null) {
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, new WebCredentials(((ContextCredentials.Provider) wp).getContextCredentials()));
            builder.setDefaultCredentialsProvider(credsProvider);
        }

        if (wp instanceof WebProvider.SSL) {
            WebProvider.SSL ws = (WebProvider.SSL) wp;
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(ws.getSSLContext(), new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            builder.setSSLSocketFactory(sslsf);
        }

        final CloseableHttpClient httpclient = builder.build();
        final HttpPut httpPut = new HttpPut(uri);
        class CP implements ContentProducer {

            @Override
            public void writeTo(OutputStream out) throws IOException {
                properties.store(out);
            }
        }
        EntityTemplate fe = new EntityTemplate(new CP());
        httpPut.setEntity(fe);
        //        httpPut.setHeader("Accept", "application/pdf");
        final CloseableHttpResponse response;
        try {
            if (credsProvider != null) {
                response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpPut));
            } else {
                response = httpclient.execute(httpPut);
            }
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
        final int sc = response.getStatusLine().getStatusCode();
        // The underlying HTTP connection is still held by the response object
        // to allow the response content to be streamed directly from the network socket.
        // In order to ensure correct deallocation of system resources
        // the user MUST call CloseableHttpResponse#close() from a finally clause.
        // Please note that if response content is not fully consumed the underlying
        // connection cannot be safely re-used and will be shut down and discarded
        // by the connection manager.
        if (sc < 200 || sc >= 300) {
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }
    }

    static TermReportNoteSetTemplate fetchTemplate(final WebProvider wp, String uri) throws IOException {
        final HttpClientBuilder builder = HttpClients.custom();

        class WebCredentials implements Credentials {

            private final ContextCredentials credentials;

            private WebCredentials(ContextCredentials creds) {
                credentials = creds;
            }

            @Override
            public Principal getUserPrincipal() {
                return new BasicUserPrincipal(credentials.getUsername());
            }

            @Override
            public String getPassword() {
                return new String(credentials.getPassword());
            }
        }
        CredentialsProvider credsProvider = null;
        if (wp instanceof ContextCredentials.Provider && ((ContextCredentials.Provider) wp).getContextCredentials() != null) {
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, new WebCredentials(((ContextCredentials.Provider) wp).getContextCredentials()));
            builder.setDefaultCredentialsProvider(credsProvider);
        }
        if (wp instanceof WebProvider.SSL) {
            WebProvider.SSL ws = (WebProvider.SSL) wp;
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(ws.getSSLContext(), new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            builder.setSSLSocketFactory(sslsf);
        }
        final CloseableHttpClient httpclient = builder.build();
        final HttpGet httpGet = new HttpGet(uri);
        //        httpGet.setHeader("Accept", "application/pdf");
        final CloseableHttpResponse response;
        try {
            if (credsProvider != null) {
                response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpGet));
            } else {
                response = httpclient.execute(httpGet);
            }
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
        // The underlying HTTP connection is still held by the response object
        // to allow the response content to be streamed directly from the network socket.
        // In order to ensure correct deallocation of system resources
        // the user MUST call CloseableHttpResponse#close() from a finally clause.
        // Please note that if response content is not fully consumed the underlying
        // connection cannot be safely re-used and will be shut down and discarded
        // by the connection manager.
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }
        final HttpEntity entity1 = response.getEntity();
        try (final InputStream is = entity1.getContent()) {
            final TermReportNoteSetTemplate ret;
            try (final BufferedInputStream bis = new BufferedInputStream(is)) {
                try {
                    //? BufferedEntity?{
                    ret = (TermReportNoteSetTemplate) getJAXB().createUnmarshaller().unmarshal(bis);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }
            EntityUtils.consume(entity1);
            return ret;
        } finally {
            response.close();
        }
    }

    static void storeTemplate(final WebProvider wp, final String uri, final TermReportNoteSetTemplate template) throws IOException {
        final HttpClientBuilder builder = HttpClients.custom();

        class WebCredentials implements Credentials {

            private final ContextCredentials credentials;

            private WebCredentials(ContextCredentials creds) {
                credentials = creds;
            }

            @Override
            public Principal getUserPrincipal() {
                return new BasicUserPrincipal(credentials.getUsername());
            }

            @Override
            public String getPassword() {
                return new String(credentials.getPassword());
            }
        }

        CredentialsProvider credsProvider = null;
        if (wp instanceof ContextCredentials.Provider && ((ContextCredentials.Provider) wp).getContextCredentials() != null) {
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, new WebCredentials(((ContextCredentials.Provider) wp).getContextCredentials()));
            builder.setDefaultCredentialsProvider(credsProvider);
        }

        if (wp instanceof WebProvider.SSL) {
            WebProvider.SSL ws = (WebProvider.SSL) wp;
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(ws.getSSLContext(), new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            builder.setSSLSocketFactory(sslsf);
        }

        final CloseableHttpClient httpclient = builder.build();
        final HttpPut httpPut = new HttpPut(uri);
        class CP implements ContentProducer {

            @Override
            public void writeTo(OutputStream out) throws IOException {
                try {
                    final Marshaller m = getJAXB().createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
                    m.marshal(template, out);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }
        }
        final EntityTemplate fe = new EntityTemplate(new CP());
        httpPut.setEntity(fe);
        //        httpPut.setHeader("Accept", "application/pdf");
        final CloseableHttpResponse response;
        try {
            if (credsProvider != null) {
                response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpPut));
            } else {
                response = httpclient.execute(httpPut);
            }
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
        // The underlying HTTP connection is still held by the response object
        // to allow the response content to be streamed directly from the network socket.
        // In order to ensure correct deallocation of system resources
        // the user MUST call CloseableHttpResponse#close() from a finally clause.
        // Please note that if response content is not fully consumed the underlying
        // connection cannot be safely re-used and will be shut down and discarded
        // by the connection manager.
        final int sc = response.getStatusLine().getStatusCode();
        if (sc < 200 || sc >= 300) {
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }
    }
}
