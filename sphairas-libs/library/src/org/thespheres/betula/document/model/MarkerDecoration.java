/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.UniqueMarkerSet;

/**
 *
 * @author boris.heithecker
 */
public interface MarkerDecoration extends DocumentDecoration<MarkerConvention, TargetDocument> {

    //E.g. view="subject" -> set: fächer,profile,kursart
    public UniqueMarkerSet getDistinguishingDecoration(DocumentId id, TargetDocument targetDocument, String view);
}
