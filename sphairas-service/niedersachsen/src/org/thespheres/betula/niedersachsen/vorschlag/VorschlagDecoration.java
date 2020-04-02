/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.vorschlag;

import org.thespheres.betula.document.model.AssessmentReference;
import java.util.Collections;
import java.util.List;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.AssessmentDecoration;

/**
 *
 * @author boris.heithecker
 */
public interface VorschlagDecoration extends AssessmentDecoration, AssessmentReference {

    @Override
    public default List<AssessmentConvention> getDecoration(DocumentId id, TargetDocument document) {
        switch (document.getTargetType().toLowerCase()) {
            case "arbeitsverhalten":
                return Collections.singletonList(AVSVVorschlag.CONVENTION);
            case "sozialverhalten":
                return Collections.singletonList(AVSVVorschlag.CONVENTION);
            default:
                return Collections.EMPTY_LIST;
        }
    }

}
