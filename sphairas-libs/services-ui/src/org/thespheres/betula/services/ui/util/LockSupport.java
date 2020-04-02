/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.openide.util.NetworkSettings;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.dav.DAVProp;
import org.thespheres.betula.services.dav.LockDiscovery;
import org.thespheres.betula.services.dav.LockInfo;
import org.thespheres.betula.services.ui.util.dav.HttpLock;
import org.thespheres.betula.services.ui.util.dav.HttpUnlock;

/**
 *
 * @author boris.heithecker
 */
public class LockSupport {

    private final static JAXBContext JAXB;

    static {
        try {
            JAXB = JAXBContext.newInstance(LockInfo.class, DAVProp.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private LockSupport() {
    }

    public static LockDiscovery lock(final WebProvider wp, final URI uri, final LockInfo lockInfo, final int seconds) throws IOException {

        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(wp);
        final HttpLock httpLock = new HttpLock(uri);
        httpLock.setHeader("Timeout", "Second-" + Integer.toString(seconds));
        class Producer implements ContentProducer {

            @Override
            public void writeTo(OutputStream out) throws IOException {
                try {
                    JAXB.createMarshaller().marshal(lockInfo, out);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }

        }
        httpLock.setEntity(new EntityTemplate(new Producer()));
        final CloseableHttpResponse response;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpLock));
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
        HttpException.orElseThrow(response.getStatusLine(), uri);
        final DAVProp ret;
        final HttpEntity entity = response.getEntity();
        try (final InputStream is = entity.getContent()) {
            final Unmarshaller um = JAXB.createUnmarshaller();
            final BufferedInputStream bis = new BufferedInputStream(is); //? BufferedEntity?
//            String theString = IOUtils.toString(bis, "utf-8");
            ret = (DAVProp) um.unmarshal(bis);
            EntityUtils.consume(entity);
            return ret.getLockDiscovery();
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            response.close();
        }
    }

    public static LockDiscovery refreshLock(final WebProvider wp, final URI uri, final String lockToken, final int seconds) throws IOException {

        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(wp);
        final HttpLock httpLock = new HttpLock(uri);
        final String ifValue = "(<" + lockToken + ">)";
        httpLock.addHeader("If", ifValue);
        httpLock.setHeader("Timeout", "Second-" + Integer.toString(seconds));
        final CloseableHttpResponse response;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpLock));
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
        HttpException.orElseThrow(response.getStatusLine(), uri);
        final DAVProp ret;
        final HttpEntity entity = response.getEntity();
        try (final InputStream is = entity.getContent()) {
            final Unmarshaller um = JAXB.createUnmarshaller();
            final BufferedInputStream bis = new BufferedInputStream(is); //? BufferedEntity?
//            String theString = IOUtils.toString(bis, "utf-8");
            ret = (DAVProp) um.unmarshal(bis);
            EntityUtils.consume(entity);
            return ret.getLockDiscovery();
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            response.close();
        }
    }

    public static void unlock(final WebProvider wp, final URI uri, final String lockToken) throws IOException {

        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(wp);
        final HttpUnlock httpUnlock = new HttpUnlock(uri);
        httpUnlock.setHeader("Lock-Token", "<" + lockToken + ">");
        final CloseableHttpResponse response;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpUnlock));
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
        HttpException.orElseThrow(response.getStatusLine(), uri);
    }
}
