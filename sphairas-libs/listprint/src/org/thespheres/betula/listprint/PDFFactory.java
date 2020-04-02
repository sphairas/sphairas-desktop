/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.listprint;

import java.io.IOException;
import java.io.OutputStream;
import org.openide.util.Exceptions;
import org.plutext.jaxb.xslfo.Root;

/**
 *
 * @author boris.heithecker
 */
public interface PDFFactory {

    public Root createRoot() throws XSLFOException;

    public OutputStream getOutputStream(String mimeType) throws IOException;

    default public void success() {
    }

    default public void failure(Exception ex) {
        Exceptions.printStackTrace(ex);
    }
}
