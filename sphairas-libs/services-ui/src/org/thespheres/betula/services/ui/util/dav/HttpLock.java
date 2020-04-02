/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util.dav;

import java.net.URI;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 *
 * @author boris.heithecker
 */
public class HttpLock extends HttpEntityEnclosingRequestBase {

    public HttpLock(URI uri) {
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return "LOCK";
    }

}
