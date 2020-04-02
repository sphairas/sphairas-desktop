/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.xml;

import java.io.IOException;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author boris.heithecker
 */
public class XMLUtilities {

    private XMLUtilities() {
    }

    public static void removeElement(final Document doc, String element, String ns) throws IOException {
        Element e = null;
        synchronized (doc) {
            try {
                e = XMLUtil.findElement(doc.getDocumentElement(), element, ns);
            } catch (IllegalArgumentException ex) {
            }
            if (e != null) {
                Node old = null;
                try {
                    old = doc.getDocumentElement().removeChild(e);
                } catch (DOMException dex) {
                    throw new IOException(dex);
                }
                assert old == e;
            }
        }
    }
}
