/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util.dav;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.openide.util.NetworkSettings;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.dav.Multistatus;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.web.ContextCredentials;

/**
 *
 * @author boris.heithecker
 */
public class MultiStatusSupport {

    private static JAXBContext JAXB2;

    private MultiStatusSupport() {
    }

    public static JAXBContext getJAXB() {
        synchronized (MultiStatusSupport.class) {
            if (JAXB2 == null) {
                try {
                    JAXB2 = JAXBContext.newInstance(Multistatus.class);
                } catch (JAXBException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return JAXB2;
    }

    public static Multistatus fetchMultistatus(final WebProvider wp, String uri) throws IOException {
        final boolean hasContextCredentials = wp instanceof ContextCredentials.Provider 
                && ((ContextCredentials.Provider) wp).getContextCredentials() != null;
        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(wp);
        final HttpReport httpGet = new HttpReport(uri);
//        httpGet.setHeader("Accept", "application/pdf");
        final CloseableHttpResponse response;
        try {
            if (hasContextCredentials) {
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

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_MULTI_STATUS) {
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }

        final Multistatus ret;
        final HttpEntity entity1 = response.getEntity();
        try (final InputStream is = entity1.getContent()) {
            final Unmarshaller um = getJAXB().createUnmarshaller();

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
}
