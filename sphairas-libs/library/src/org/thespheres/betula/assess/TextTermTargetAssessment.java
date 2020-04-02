/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.util.List;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.TextTermTargetAssessment.Listener;
import org.thespheres.betula.assess.TextTermTargetAssessment.TextEntry;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 * @param <E>
 */
public interface TextTermTargetAssessment<E extends TextEntry> extends IdentityTargetAssessment<List<E>, TermId, Listener<E>> {

    @Override
    public default Timestamp timestamp(StudentId student, TermId term) {
        throw new UnsupportedOperationException("Not supported. Every TextEntry in List<TextEntry> has its own timestamp.");
    }

    public interface Listener<E extends TextEntry> extends IdentityTargetAssessment.Listener<List<E>, TermId> {
    }

    public static interface TextEntry {

        public Marker getSection();

        public String getText();

        public java.sql.Timestamp getTimestamp();

    }
}
