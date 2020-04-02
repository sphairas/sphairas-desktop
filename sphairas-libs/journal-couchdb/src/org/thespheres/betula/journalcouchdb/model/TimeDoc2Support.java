/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.model;

import java.util.List;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 */
//@View(name = "findByTargetId", map = "function(doc) { if (doc.target) { emit(doc.target, doc); } }")
public class TimeDoc2Support extends CouchDbRepositorySupport<TimeDoc2> {
    
    public TimeDoc2Support(CouchDbConnector db) {
        super(TimeDoc2.class, db);
    }
    
//    @GenerateView
    public List<TimeDoc2> findByTarget(DocumentId target) {
        return queryView("findByTargetId", TargetDoc.createId(target));
    }
}
