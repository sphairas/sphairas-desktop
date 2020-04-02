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
public class HttpReport extends HttpRequestBase {

    public HttpReport(String uri) {
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return "REPORT";
    }

}
