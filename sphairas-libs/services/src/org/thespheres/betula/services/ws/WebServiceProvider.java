/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ws;

import org.thespheres.betula.services.WebProvider;
import java.io.IOException;

/**
 *
 * @author boris.heithecker
 */
public interface WebServiceProvider extends WebProvider {

    public BetulaWebService createServicePort() throws IOException;

}
