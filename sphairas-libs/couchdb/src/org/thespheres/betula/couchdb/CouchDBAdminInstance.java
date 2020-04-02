/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.couchdb;

import java.io.IOException;
import org.ektorp.CouchDbInstance;
import org.thespheres.betula.services.WebProvider;

/**
 *
 * @author boris.heithecker
 */
public interface CouchDBAdminInstance extends WebProvider {

    public CouchDbInstance getInstance() throws IOException;
    
    public String getBaseUrl();

}
