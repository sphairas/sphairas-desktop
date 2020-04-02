/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.openide.util.NetworkSettings;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.UID;
import org.thespheres.ical.builder.ICalendarBuilder;

/**
 *
 * @author boris.heithecker
 */
public class CalendarHttpUtilities {
    
    public static List<ICalendar> parseCalendars(WebProvider wp, URI url) throws IOException, ParseException, InvalidComponentException {
        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(wp);
        final HttpGet httpGet = new HttpGet(url);
        //        httpGet.setHeader("Accept", "application/pdf");
        try (final CloseableHttpResponse response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpGet))) {
            // The underlying HTTP connection is still held by the response object
            // to allow the response content to be streamed directly from the network socket.
            // In order to ensure correct deallocation of system resources
            // the user MUST call CloseableHttpResponse#close() from a finally clause.
            // Please note that if response content is not fully consumed the underlying
            // connection cannot be safely re-used and will be shut down and discarded
            // by the connection manager.
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + url);
            }
            HttpEntity entity1 = response.getEntity();
            try (final InputStream is = entity1.getContent();) {
                final BufferedInputStream bis = new BufferedInputStream(is); //? BufferedEntity?
                //            String theString = IOUtils.toString(bis, "utf-8");
                entity1.getContentEncoding();
                final Charset charset = ContentType.getOrDefault(entity1).getCharset();
                final List<ICalendar> l = ICalendarBuilder.parseCalendars(bis, charset.name());
                EntityUtils.consume(entity1);
                return l;
            }
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
    }
    
    public static List<ICalendar> parseCalendars(WebProvider wp, String url) throws IOException, ParseException, InvalidComponentException {
        return parseCalendars(wp, URI.create(url));
    }
    
    public static void putCalendar(WebProvider wp, String url, ICalendar ical) throws IOException {
        putCalendar(wp, URI.create(url), ical);
    }
    
    public static void putCalendar(WebProvider wp, URI uri, ICalendar ical) throws IOException {
        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(wp);
        final HttpPost httpPost = new HttpPost(uri);
        final StringEntity content = new StringEntity(ical.toString(), HTTP.DEF_CONTENT_CHARSET);
        content.setContentType(ICalendar.MIME);
        httpPost.setEntity(content);
        
        try (CloseableHttpResponse response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpPost)) // The underlying HTTP connection is still held by the response object
                // to allow the response content to be streamed directly from the network socket.
                // In order to ensure correct deallocation of system resources
                // the user MUST call CloseableHttpResponse#close() from a finally clause.
                // Please note that if response content is not fully consumed the underlying
                // connection cannot be safely re-used and will be shut down and discarded
                // by the connection manager.
                ) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
            }
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
    }
    
    public static void deleteEvent(WebProvider wp, URI base, UID uid) throws IOException {
        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(wp);
        final URI uri;
        try {
            uri = buildURI(base, uid);
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
        final HttpDelete httpPost = new HttpDelete(uri);
        try (CloseableHttpResponse response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(httpPost)) // The underlying HTTP connection is still held by the response object
                // to allow the response content to be streamed directly from the network socket.
                // In order to ensure correct deallocation of system resources
                // the user MUST call CloseableHttpResponse#close() from a finally clause.
                // Please note that if response content is not fully consumed the underlying
                // connection cannot be safely re-used and will be shut down and discarded
                // by the connection manager.
                ) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + base.toString());
            }
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException(ex);
        }
    }
    
    public static URI buildURI(URI base, UID uid) throws URISyntaxException {
        final URIBuilder builder = new URIBuilder(base);
        builder.addParameter("uid.host", uid.getAuthority());
        builder.addParameter("uid.id", uid.getId());
        final URI uri = builder.build();
        return uri;
    }
    
}
