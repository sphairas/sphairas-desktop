/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.termreport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.thespheres.betula.TermId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;

/**
 *
 * @author boris.heithecker
 */
class RemoteServiceDescriptor {

    private final String provider;
    private final Unit unit;
    private final DocumentId targetBase;
    private final TermSchedule termSchedule;
    private final DocumentsModel docModel;
    private final Set<DocumentId> documents;
    private final NamingResolver naming;
    private Term term;
    private DocumentId document;

    RemoteServiceDescriptor(String provider, Unit unit, DocumentId target, TermSchedule ts, DocumentsModel dm, NamingResolver nr, Set<DocumentId> docs) {
        this.provider = provider;
        this.unit = unit;
        this.targetBase = target;
        this.termSchedule = ts;
        this.docModel = dm;
        this.documents = docs;
        this.naming = nr;
    }

    List<Term> findSelectableTerms() {
        Term ct = termSchedule.getCurrentTerm();
        TermId ctid = ct.getScheduledItemId();
        int id = ct.getScheduledItemId().getId();
        final ArrayList<Term> ret = new ArrayList<>();
        for (int i = id - 4; i++ <= id + 4;) {
            Term add = null;
            if (i == 0) {
                add = ct;
            } else {
                TermId tid = new TermId(ctid.getAuthority(), i);
                try {
                    add = termSchedule.resolve(tid);
                } catch (TermNotFoundException | IllegalAuthorityException ex) {
                }
            }
            if (add != null) {
                ret.add(add);
            }
        }
        return ret;
    }

    String getProvider() {
        return provider;
    }

    Unit getUnit() {
        return unit;
    }

    DocumentId getTargetBase() {
        return targetBase;
    }

    TermSchedule getTermSchedule() {
        return termSchedule;
    }

    DocumentsModel getDocModel() {
        return docModel;
    }

    Set<DocumentId> getDocuments() {
        return documents;
    }

    NamingResolver getNamingResolver() {
        return naming;
    }

    Term getSelectedTerm() {
        return this.term;
    }

    void setSelectedTerm(Term t) {
        this.term = t;
    }

    DocumentId getSelectedDocument() {
        return this.document;
    }

    void setSelectedDocument(DocumentId d) {
        this.document = d;
    }
}
