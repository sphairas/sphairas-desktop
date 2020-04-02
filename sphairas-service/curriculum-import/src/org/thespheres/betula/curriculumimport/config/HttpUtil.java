/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.openide.util.NetworkSettings;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.web.ContextCredentials;

/**
 *
 * @author boris.heithecker
 */
class HttpUtil {

    static boolean fetchFile(final WebProvider wp, String uri, Path ret) throws IOException {
        final boolean hasContextCredentials = wp instanceof ContextCredentials.Provider
                && ((ContextCredentials.Provider) wp).getContextCredentials() != null;
        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(wp);
        final HttpGet httpGet = new HttpGet(uri);
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
        final int sc = response.getStatusLine().getStatusCode();
        if (sc != HttpStatus.SC_OK) {
            if (sc == HttpStatus.SC_NOT_FOUND) {
                return false;
            }
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }
        final HttpEntity entity = response.getEntity();
        try (final InputStream is = entity.getContent()) {
            try (final BufferedInputStream bis = new BufferedInputStream(is)) { //? BufferedEntity?{
                Files.copy(bis, ret, StandardCopyOption.REPLACE_EXISTING);
            }
            EntityUtils.consume(entity);
            return true;
        } finally {
            response.close();
        }
    }

    static void storeFile(final WebProvider wp, final String uri, final Path file) throws IOException {
        final boolean hasContextCredentials = wp instanceof ContextCredentials.Provider
                && ((ContextCredentials.Provider) wp).getContextCredentials() != null;
        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(wp);
        final HttpPut httpPut = new HttpPut(uri);
        class CP implements ContentProducer {

            @Override
            public void writeTo(OutputStream out) throws IOException {
                Files.copy(file, out);
            }
        }
        EntityTemplate fe = new EntityTemplate(new CP());
        httpPut.setEntity(fe);
        //        httpPut.setHeader("Accept", "application/pdf");
        final CloseableHttpResponse response;
        try {
            if (hasContextCredentials) {
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
}
