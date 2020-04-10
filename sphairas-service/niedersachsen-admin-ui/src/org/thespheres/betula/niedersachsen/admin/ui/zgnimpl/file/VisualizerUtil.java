/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author boris
 */
class VisualizerUtil {

    private static Object fo2html;
    protected static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    public static synchronized Templates getXslFo2HtmlTemplate() throws IOException {
        if (fo2html == null) {
            final InputStream is = VisualizerUtil.class.getResourceAsStream("fo2html.xsl");
            try {
                fo2html = TRANSFORMER_FACTORY.newTemplates(new StreamSource(is));
            } catch (TransformerConfigurationException ex) {
                fo2html = new IOException(ex);
            }
        }
        if (fo2html instanceof IOException) {
            throw (IOException) fo2html;
        }
        return (Templates) fo2html;
    }
}
