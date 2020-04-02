/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports.sibank;

import org.thespheres.betula.niedersachsen.kgs.ui.imports.SGLFilter;
import org.thespheres.betula.niedersachsen.kgs.ui.imports.SGLFilterColumn;
import org.thespheres.betula.niedersachsen.kgs.ui.imports.SGLFilterOverride;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn.Factory.Registration;

/**
 *
 * @author boris.heithecker
 */
public class SiBankSGLFilterColumn extends SGLFilterColumn<SiBankImportTarget, SiBankKursItem> {

    private SiBankSGLFilterColumn() {
    }

    @Override
    protected SGLFilter createFilter(final SiBankKursItem il) {
        return new SGLFilter<>(il, configuration);
    }

    @Override
    protected SGLFilterOverride createOverride(final SGLFilter filter) {
        return new SiBankSGLFilterOverride(filter.getFilterMarkers());
    }

    @Registration(component = "SiBankCreateDocumentsVisualPanel")
    public static final class Factory extends ImportTableColumn.Factory {

        @Override
        public ImportTableColumn createInstance() {
            return new SiBankSGLFilterColumn();
        }

    }
}
