/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.PropertyResourceBundle;
import javax.net.ssl.HostnameVerifier;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openide.util.NetworkSettings;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.dav.Multistatus;
import org.thespheres.betula.services.ui.util.dav.HttpCopy;
import org.thespheres.betula.services.ui.util.dav.HttpPropfind;
import org.thespheres.betula.services.ui.util.dav.MultiStatusSupport;
import org.thespheres.betula.services.web.ContextCredentials;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition;

/**
 *
 * @author boris.heithecker
 */
public class HttpUtilities {

    private static JAXBContext jaxb;
    final HostnameVerifier defaultHostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();

    private static JAXBContext getJAXB() {
        synchronized (HttpUtilities.class) {
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

    public static void post(WebProvider wp, URI uri, ContentProducer producer, String contentType) throws IOException {
        final EntityTemplate entity = new EntityTemplate(producer);
        if (contentType != null) {
            entity.setContentType(contentType);
        }
        post(wp, uri, entity);
    }

    public static void putIfNoneMatch(WebProvider wp, URI uri, byte[] content, String contentType, String lockToken) throws IOException {
        final ByteArrayEntity entity = new ByteArrayEntity(content);
        if (contentType != null) {
            entity.setContentType(contentType);
        }
        putPost(wp, uri, entity, HttpPut.METHOD_NAME, lockToken, true);
    }

    public static void put(WebProvider wp, URI uri, byte[] content, String contentType, String lockToken) throws IOException {
        final ByteArrayEntity entity = new ByteArrayEntity(content);
        if (contentType != null) {
            entity.setContentType(contentType);
        }
        putPost(wp, uri, entity, HttpPut.METHOD_NAME, lockToken, false);
    }

    public static void put(WebProvider wp, URI uri, ContentProducer producer, String contentType, String lockToken) throws IOException {
        final EntityTemplate entity = new EntityTemplate(producer);
        if (contentType != null) {
            entity.setContentType(contentType);
        }
        put(wp, uri, entity, lockToken);
    }

    public static void put(WebProvider wp, URI uri, ContentProducer producer, String contentType) throws IOException {
        final EntityTemplate entity = new EntityTemplate(producer);
        if (contentType != null) {
            entity.setContentType(contentType);
        }
        put(wp, uri, entity, null);
    }

    public static void post(WebProvider wp, URI uri, AbstractHttpEntity entity) throws IOException {
        putPost(wp, uri, entity, HttpPost.METHOD_NAME, null, false);
    }

    public static void put(WebProvider wp, URI uri, AbstractHttpEntity entity, String lockToken) throws IOException {
        putPost(wp, uri, entity, HttpPut.METHOD_NAME, lockToken, false);
    }

    private static void putPost(WebProvider wp, URI uri, AbstractHttpEntity entity, final String method, final String lockToken, final boolean ifNoneMatch) throws IOException {
        final CloseableHttpClient httpclient = buildHttpClient(wp);
        final HttpEntityEnclosingRequestBase httpPost = HttpPut.METHOD_NAME.equals(method) ? new HttpPut(uri) : new HttpPost(uri);

        if (!StringUtils.isBlank(lockToken)) {
            final String ifValue = "(<" + lockToken + ">)";
            httpPost.addHeader("If", ifValue);
        }

        if (ifNoneMatch) {
            httpPost.addHeader("If", "*");
        }

        httpPost.setEntity(entity);

        try ( CloseableHttpResponse response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpPost))) { // The underlying HTTP connection is still held by the response object
            // to allow the response content to be streamed directly from the network socket.
            // In order to ensure correct deallocation of system resources
            // the user MUST call CloseableHttpResponse#close() from a finally clause.
            // Please note that if response content is not fully consumed the underlying
            // connection cannot be safely re-used and will be shut down and discarded
            // by the connection manager.
            HttpException.orElseThrow(response.getStatusLine(), uri);
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
    }

    public static void delete(final WebProvider wp, final URI uri) throws IOException {
        delete(wp, uri, null);
    }

    public static void delete(final WebProvider wp, final URI uri, final String lockToken) throws IOException {
        final CloseableHttpClient httpclient = buildHttpClient(wp);
        final HttpDelete httpDelete = new HttpDelete(uri);

        if (!StringUtils.isBlank(lockToken)) {
            final String ifValue = "(<" + lockToken + ">)";
            httpDelete.addHeader("If", ifValue);
        }

        try ( CloseableHttpResponse response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpDelete)) // The underlying HTTP connection is still held by the response object
                // to allow the response content to be streamed directly from the network socket.
                // In order to ensure correct deallocation of system resources
                // the user MUST call CloseableHttpResponse#close() from a finally clause.
                // Please note that if response content is not fully consumed the underlying
                // connection cannot be safely re-used and will be shut down and discarded
                // by the connection manager.
                ) {
            HttpException.orElseThrow(response.getStatusLine(), uri);
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
    }

    public static void copy(WebProvider wp, final URI from, final String to) throws IOException {
        final CloseableHttpClient httpclient = buildHttpClient(wp);
        final HttpCopy httpCopy = new HttpCopy(from, to);

        try ( CloseableHttpResponse response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpCopy)) // The underlying HTTP connection is still held by the response object
                // to allow the response content to be streamed directly from the network socket.
                // In order to ensure correct deallocation of system resources
                // the user MUST call CloseableHttpResponse#close() from a finally clause.
                // Please note that if response content is not fully consumed the underlying
                // connection cannot be safely re-used and will be shut down and discarded
                // by the connection manager.
                ) {
            HttpException.orElseThrow(response.getStatusLine(), from);
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
    }

    public static Multistatus getProperties(final WebProvider wp, final URI uri, final int depth, final boolean ignoreNotFound) throws IOException {
        final CloseableHttpClient httpclient = buildHttpClient(wp);

        final HttpPropfind httpGet = new HttpPropfind(uri);
        final String depthValue;
        switch (depth) {
            case 0:
                depthValue = "0";
                break;
            case 1:
                depthValue = "1";
                break;
            case -1:
                depthValue = "infinity";
                break;
            default:
                throw new IllegalArgumentException("Depth value must be 0, 1 or -1");
        }
        httpGet.addHeader("Depth", depthValue);

        final CloseableHttpResponse response;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpGet));
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
        if (ignoreNotFound && sc == HttpStatus.SC_NOT_FOUND) {
            return null;
        } else if (sc != HttpStatus.SC_MULTI_STATUS) {
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }

        final Multistatus ret;
        final HttpEntity entity1 = response.getEntity();
        try (final InputStream is = entity1.getContent()) {
            final Unmarshaller um = MultiStatusSupport.getJAXB().createUnmarshaller();
            final BufferedInputStream bis = new BufferedInputStream(is); //? BufferedEntity?
//            String theString = IOUtils.toString(bis, "utf-8");
            ret = (Multistatus) um.unmarshal(bis);
            EntityUtils.consume(entity1);
            return ret;
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            response.close();
        }
    }

    public static String get(final WebProvider wp, final URI uri, final String ifModifiedSince, final boolean ignoreNotFound) throws IOException {
        final CloseableHttpClient httpclient = buildHttpClient(wp);

        final HttpGet httpGet = new HttpGet(uri);
        if (ifModifiedSince != null) {
            httpGet.addHeader(HttpHeaders.IF_MODIFIED_SINCE, ifModifiedSince);
        }
        final CloseableHttpResponse response;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpGet));
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
        if (sc == HttpStatus.SC_NOT_MODIFIED && ifModifiedSince != null) {
            return null;
        } else if (ignoreNotFound && sc == HttpStatus.SC_NOT_FOUND) {
            return null;
        } else if (sc != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }
        final HttpEntity entity = response.getEntity();
        final String lm = Arrays.stream(response.getAllHeaders())
                .filter(h -> HttpHeaders.LAST_MODIFIED.equals(h.getName()))
                .map(h -> h.getValue())
                .collect(CollectionUtil.singleOrNull());
        try (final InputStream is = entity.getContent()) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } finally {
            response.close();
        }
    }

    public static <R> R get(final WebProvider wp, final URI uri, final GetConverter<InputStream, R> converter, final String ifModifiedSince, final boolean ignoreNotFound) throws IOException {
        final CloseableHttpClient httpclient = buildHttpClient(wp);

        final HttpGet httpGet = new HttpGet(uri);
        if (ifModifiedSince != null) {
            httpGet.addHeader(HttpHeaders.IF_MODIFIED_SINCE, ifModifiedSince);
        }
        final CloseableHttpResponse response;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpGet));
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
        if (sc == HttpStatus.SC_NOT_MODIFIED && ifModifiedSince != null) {
            return null;
        } else if (ignoreNotFound && sc == HttpStatus.SC_NOT_FOUND) {
            return null;
        } else if (sc != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }
        final HttpEntity entity = response.getEntity();
        final String lm = Arrays.stream(response.getAllHeaders())
                .filter(h -> HttpHeaders.LAST_MODIFIED.equals(h.getName()))
                .map(h -> h.getValue())
                .collect(CollectionUtil.singleOrNull());
        try (final InputStream is = entity.getContent()) {
            final BufferedInputStream bis = new BufferedInputStream(is); //? BufferedEntity?
            final R ret = converter.apply(lm, bis);
            EntityUtils.consume(entity);
            return ret;
        } finally {
            response.close();
        }
    }

    public static CloseableHttpClient buildHttpClient(final WebProvider wp) {
        final HttpClientBuilder builder = HttpClients.custom();

        class WebCredentials implements Credentials {

            private final ContextCredentials credentials;

            private WebCredentials(ContextCredentials creds) {
                super();
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
        CredentialsProvider credsProvider;
        if (wp instanceof ContextCredentials.Provider && ((ContextCredentials.Provider) wp).getContextCredentials() != null) {
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, new WebCredentials(((ContextCredentials.Provider) wp).getContextCredentials()));
            builder.setDefaultCredentialsProvider(credsProvider);
        }
        if (wp instanceof WebProvider.SSL) {
            WebProvider.SSL ws = (WebProvider.SSL) wp;
            final HostnameVerifier hostnameVerifier = ws.getHostnameVerifier();
            final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(ws.getSSLContext(), new String[]{"TLSv1"}, null, hostnameVerifier != null ? hostnameVerifier : SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            builder.setSSLSocketFactory(sslsf);
        }
        return builder.build();
    }

    public static PropertyResourceBundleExt fetchResourceBundle(final WebProvider wp, String uri) throws IOException {
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
        final String lm = Arrays.stream(response.getAllHeaders())
                .filter(h -> "Last-Modified".equals(h.getName()))
                .map(h -> h.getValue())
                .collect(CollectionUtil.singleOrNull());
        final HttpEntity entity1 = response.getEntity();
        try (final InputStream is = entity1.getContent()) {
            final BufferedInputStream bis = new BufferedInputStream(is); //? BufferedEntity?
            final PropertyResourceBundleExt ret = new PropertyResourceBundleExt(bis);
            EntityUtils.consume(entity1);
            ret.setLastModified(lm);
            return ret;
        } finally {
            response.close();
        }
    }

    public static XmlMarkerConventionDefinition fetchXmlMarkerDefinition(final WebProvider wp, String uri, ClientXmlDefinitionSupport support) throws IOException {
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
        final String lm = Arrays.stream(response.getAllHeaders())
                .filter(h -> "Last-Modified".equals(h.getName()))
                .map(h -> h.getValue())
                .collect(CollectionUtil.singleOrNull());
        final HttpEntity entity = response.getEntity();
        try (final InputStream is = entity.getContent()) {
            final BufferedInputStream bis = new BufferedInputStream(is); //? BufferedEntity?
            final XmlMarkerConventionDefinition ret;
            try {
                ret = (XmlMarkerConventionDefinition) getJAXB().createUnmarshaller().unmarshal(bis);
            } catch (JAXBException ex) {
                throw new IOException(ex);
            }
            EntityUtils.consume(entity);
            support.setLastModified(lm);
            return ret;
        } finally {
            response.close();
        }
    }

    @Deprecated//use getProperties
    public static String fetchLastModified(final WebProvider wp, String uri) throws IOException {
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
        final HttpHead httpGet = new HttpHead(uri);
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
        return Arrays.stream(response.getAllHeaders())
                .filter(h -> "Last-Modified".equals(h.getName()))
                .map(h -> h.getValue())
                .collect(CollectionUtil.singleOrNull());
    }

    public static class PropertyResourceBundleExt extends PropertyResourceBundle {

        private String lastModified;

        public PropertyResourceBundleExt(InputStream stream) throws IOException {
            super(stream);
        }

        public PropertyResourceBundleExt(Reader reader) throws IOException {
            super(reader);
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

    }

    @FunctionalInterface
    public static interface GetConverter<T, R> {

        public R apply(String lm, T t) throws IOException;
    }

}
