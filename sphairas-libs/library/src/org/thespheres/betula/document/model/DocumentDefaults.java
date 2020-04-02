/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import org.thespheres.betula.Tag;
import org.thespheres.betula.document.Document;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 * @param <T>
 * @param <D>
 */
public interface DocumentDefaults<T extends Tag, D extends Document> {

    public T getDefaultValue(DocumentId id, D document);
}
