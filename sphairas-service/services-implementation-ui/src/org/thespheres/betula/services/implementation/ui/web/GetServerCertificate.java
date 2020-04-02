/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.openide.util.NetworkSettings;

/**
 *
 * @author boris.heithecker
 */
public class GetServerCertificate {

    public static final String SERVER_PATH = "/web/dav/public/server.crt";
    public static final String CA_PATH = "/web/dav/public/ca.crt";
    private static final String PEER_CERTIFICATES = "peer-certificates";
    private static SSLContext sslContext;
    private final static CertificateFactory CF;

    static {
        try {
            CF = CertificateFactory.getInstance("X.509");
        } catch (CertificateException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static Certificate[] fetchCertificates(final String base, final int port) throws IOException, CertificateException {
        final CloseableHttpClient httpclient = buildHttpClient();

//        final String serverUrl = "https://" + host + "/web/dav/public/server.crt";
//        final String caUrl = "https://" + host + "/web/dav/public/ca.crt";
        final String serverUrl = "https://" + base + ":" + Integer.toString(port) + SERVER_PATH;
        final String caUrl = "https://" + base + ":" + Integer.toString(port) + CA_PATH;

        final HttpGet httpget = new HttpGet(serverUrl);
        CloseableHttpResponse response = null;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpget));
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        } finally {
            if (response != null) {
                response.close();
            }
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
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + serverUrl);
        }
        final HttpEntity entity = response.getEntity();
        final Certificate serverCert;
        try (final InputStream is = entity.getContent()) {
            final BufferedInputStream bis = new BufferedInputStream(is); //? BufferedEntity?        
            serverCert = CF.generateCertificate(bis);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
        final HttpGet httpget2 = new HttpGet(caUrl);
        CloseableHttpResponse response2 = null;
        try {
            response2 = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpget2));
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        } finally {
            if (response2 != null) {
                response2.close();
            }
        }
        // The underlying HTTP connection is still held by the response object
        // to allow the response content to be streamed directly from the network socket.
        // In order to ensure correct deallocation of system resources
        // the user MUST call CloseableHttpResponse#close() from a finally clause.
        // Please note that if response content is not fully consumed the underlying
        // connection cannot be safely re-used and will be shut down and discarded
        // by the connection manager.
        final int sc2 = response2.getStatusLine().getStatusCode();
        if (sc2 == HttpStatus.SC_NOT_FOUND) {
            return new Certificate[]{serverCert};
        } else if (sc2 != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + caUrl);
        }
        final HttpEntity entity2 = response2.getEntity();
        final Certificate caCert;
        try (final InputStream is = entity2.getContent()) {
            final BufferedInputStream bis = new BufferedInputStream(is); //? BufferedEntity?        
            caCert = CF.generateCertificate(bis);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
        return new Certificate[]{serverCert, caCert};
    }

    public static Certificate[] serverCertificatesFromSSLHandshake(final String uri) throws MalformedURLException, SSLPeerUnverifiedException, IOException, ClassCastException {
        final CloseableHttpClient httpclient = buildHttpClient();

        final HttpOptions hpptOptions = new HttpOptions(uri);
        final HttpContext context = new BasicHttpContext();
        CloseableHttpResponse response = null;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(hpptOptions, context));
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return (Certificate[]) context.getAttribute(PEER_CERTIFICATES);
//        final URL url = new URL(https);
//        final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//        return conn.getServerCertificates();
    }

    private static CloseableHttpClient buildHttpClient() {
        final HttpClientBuilder builder = HttpClients.custom();
        final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(getTrustAllSSLContext(), new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        builder.setSSLSocketFactory(sslsf);
        builder.addInterceptorLast((HttpResponse response, HttpContext context) -> {
            final ManagedHttpClientConnection routedConnection = (ManagedHttpClientConnection) context.getAttribute(HttpCoreContext.HTTP_CONNECTION);
            final SSLSession session = routedConnection.getSSLSession();
            if (session != null) {
                final Certificate[] certificates = session.getPeerCertificates();
                context.setAttribute(PEER_CERTIFICATES, certificates);
            }
        });
        return builder.build();
    }

    private static SSLContext getTrustAllSSLContext() throws IllegalStateException {
        if (sslContext == null) {
            try {
                final SSLContext ctx = SSLContext.getInstance("TLSv1.2");
                final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
                };
                ctx.init(null, trustAllCerts, new SecureRandom());
                sslContext = ctx;
            } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return sslContext;
    }
}
