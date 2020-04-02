/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.model;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;

/**
 *
 * @author boris.heithecker
 */
public class TargetDocSupport extends  CouchDbRepositorySupport<TargetDoc> {

    public TargetDocSupport(CouchDbConnector db) {
        super(TargetDoc.class, db);
    }
    
}
