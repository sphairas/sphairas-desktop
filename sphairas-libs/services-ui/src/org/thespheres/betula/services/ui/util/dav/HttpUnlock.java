/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util.dav;

import java.net.URI;
import org.apache.http.client.methods.HttpRequestBase;

/**
 *
 * @author boris.heithecker
 */
public class HttpUnlock extends HttpRequestBase {

    public HttpUnlock(URI uri) {
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return "UNLOCK";
    }

}
