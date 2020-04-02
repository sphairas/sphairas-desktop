/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import java.util.List;
import org.thespheres.betula.Convention;
import org.thespheres.betula.document.Document;
import org.thespheres.betula.document.DocumentId;

/**
 *Service interface to specify user available decorations (marker, assessment conventions) for documents
 * @author boris.heithecker
 * @param <T>
 * @param <D>
 */
public interface DocumentDecoration<T extends Convention, D extends Document> {

    public List<T> getDecoration(DocumentId id, D document);
}
