/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.couchdb;

import java.io.IOException;
import org.ektorp.CouchDbConnector;
import org.thespheres.betula.services.WebProvider;

/**
 *
 * @author boris.heithecker
 */
public interface CouchDBProvider extends WebProvider {

    public CouchDbConnector getUserDatabase() throws IOException;
}
