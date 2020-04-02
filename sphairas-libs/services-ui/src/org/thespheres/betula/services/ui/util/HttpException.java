/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.IOException;
import java.net.URI;
import org.apache.http.StatusLine;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
@Messages({"HttpException.message=Unerwartete Antwort {0} von {1}"})
public class HttpException extends IOException {

    private final StatusLine statusLine;
    private final URI uri;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public HttpException(final StatusLine line, final URI target) {
        this.statusLine = line;
        this.uri = target;
    }
    
    public static void orElseThrow(final StatusLine line, final URI target) throws HttpException {
        if(line.getStatusCode() < 200 || line.getStatusCode() >= 300) {
            throw new HttpException(line, target);
        }
    }

    public int getStatusCode() {
        return statusLine.getStatusCode();
    }

    @Override
    public String getLocalizedMessage() {
        return NbBundle.getMessage(HttpException.class, "HttpException.message", statusLine, uri);
    }

}
