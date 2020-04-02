/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports.untis;

import org.thespheres.betula.niedersachsen.kgs.ui.imports.*;
import org.openide.util.NbBundle;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn.Factory.Registration;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SGLFilterColumn.columnName=Filter (Schulzweig)"})
public class UntisSGLFilterColumn extends SGLFilterColumn<UntisImportConfiguration, ImportedLesson> {

    @Override
    protected SGLFilter createFilter(final ImportedLesson il) {
        return new UntisSGLFilter(il, configuration);
    }

    @Override
    protected SGLFilterOverride createOverride(final SGLFilter filter) {
        return new UntisSGLFilterOverride((UntisSGLFilter) filter);
    }

    @Registration(component = "UntisCreateDocumentsVisualPanel")
    public static final class Factory extends ImportTableColumn.Factory {

        @Override
        public ImportTableColumn createInstance() {
            return new UntisSGLFilterColumn();
        }

    }
}
