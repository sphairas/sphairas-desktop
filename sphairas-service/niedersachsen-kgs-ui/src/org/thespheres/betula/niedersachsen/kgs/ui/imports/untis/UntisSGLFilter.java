/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports.untis;

import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.kgs.ui.imports.*;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;

/**
 *
 * @author boris.heithecker
 */
@Deprecated(forRemoval = true)
class UntisSGLFilter extends SGLFilter<UntisImportConfiguration, ImportedLesson> {

    private boolean negate = false;

    UntisSGLFilter(final ImportedLesson item, final UntisImportConfiguration sib) {
        super(item, sib);
    }

    boolean isNegate() {
        return negate;
    }

    void setNegate(final boolean neg) {
        negate = neg;
    }

    @Override
    protected boolean contains(final Marker m) {
        boolean contains = super.contains(m);
        return negate ? !contains : contains;
    }

}
