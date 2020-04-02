/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.http.Consts;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.CloseableHttpClient;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NetworkSettings;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.xml.Document;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ws.WebServiceProvider;

/**
 *
 * @author boris.heithecker
 */
@Messages({"UploadXml.missingHref.exception=Kein Upload-Pfad konfiguiert.",
    "UploadXml.error.httpResponse=Beim Hochladen der Datei ist ein Fehler aufgetreten: http-Status {0} ({1})"})
public class UploadXml {

    public static void upload(UntisImportConfiguration context, Document untis) throws IOException {
        final String uri = context.getUntisXmlDocumentUploadHref();
        if (uri == null) {
            String msg = NbBundle.getMessage(UploadXml.class, "UploadXml.missingHref.exception");
            throw new IOException(msg);
        }

        final WebServiceProvider wsp = context.getWebServiceProvider();
        try {
            doUpload(uri, untis, (WebProvider.SSL) wsp);
        } catch (MalformedURLException | ClassCastException ex) {
            throw new IOException(ex);
        }
    }

    private static void doUpload(String uri, Document d, WebProvider.SSL wsp) throws IOException {
        final CloseableHttpClient httpclient = HttpUtilities.buildHttpClient(wsp);
        final HttpPut put = new HttpPut(uri);
        final Marshaller m;
        try {
            m = UntisXmlDataObject.JAXB.createMarshaller();
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
        class CP implements ContentProducer {

            @Override
            public void writeTo(OutputStream out) throws IOException {
                try {
                    m.marshal(d, out);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }
        }
        EntityTemplate fe = new EntityTemplate(new CP());
        put.setEntity(fe);
        fe.setContentEncoding(Consts.UTF_8.displayName());
        CloseableHttpResponse response = null;
        try {
            try {
                response = NetworkSettings.suppressAuthenticationDialog(() -> httpclient.execute(put));
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
            StatusLine sl = response.getStatusLine();
            if (sl.getStatusCode() < 200 || sl.getStatusCode() >= 300) {
                String msg = NbBundle.getMessage(UploadXml.class, "UploadXml.error.httpResponse", sl.getStatusCode(), sl.getReasonPhrase());
                throw new IOException(msg);
            }
//            EntityUtils.consume(entity);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ioex) {
                }
            }
        }
    }
}
