/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports.currimp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.kgs.ui.imports.SGLFilter;
import org.thespheres.betula.niedersachsen.kgs.ui.imports.SGLFilterColumn;
import org.thespheres.betula.niedersachsen.kgs.ui.imports.SGLFilterOverride;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;

/**
 *
 * @author boris.heithecker@gmx.net
 */
@JAXBUtil.JAXBRegistration(target = "CurriculumAssociationsSupport")
@XmlRootElement(name = "sglfilter")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class CurriculumImportSGLFilterOverride extends SGLFilterOverride {

    public CurriculumImportSGLFilterOverride() {
    }

    CurriculumImportSGLFilterOverride(final Marker[] marker) {
        super(marker);
    }

    @ImportTableColumn.Factory.Registrations({
        @ImportTableColumn.Factory.Registration(component = "StundentafelImportConfigVisualPanel")})
    public static final class Factory extends ImportTableColumn.Factory {

        @Override
        public ImportTableColumn createInstance() {
            return new SGLFilterColumn() {
                @Override
                protected SGLFilterOverride createOverride(SGLFilter filter) {
                    return new CurriculumImportSGLFilterOverride(filter.getFilterMarkers());
                }
            };
        }

    }
}
