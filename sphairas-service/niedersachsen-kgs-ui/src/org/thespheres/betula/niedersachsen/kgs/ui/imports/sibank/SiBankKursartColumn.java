/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports.sibank;

import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.kgs.*;
import org.thespheres.betula.niedersachsen.kgs.ui.imports.KursartColumn;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class SiBankKursartColumn extends KursartColumn<SiBankImportTarget, SiBankKursItem> {

    @Override
    protected void parseKursart(final SiBankKursItem kurs) {
        if (configuration instanceof ConfigurableImportTarget) {
            final ConfigurableImportTarget cit = (ConfigurableImportTarget) configuration;
            final String kursnr = kurs.getDistinguisher().getKursnr();
            if (kursnr != null) {
                final Marker sgl = cit.getImportScripts().parseRealm(kursnr);
                if (sgl != null && SGL.NAME.equals(sgl.getConvention())) {
                    kurs.getUniqueMarkerSet().add(sgl);
                }
            }
        }
    }

    @ImportTableColumn.Factory.Registrations({
        @ImportTableColumn.Factory.Registration(component = "SiBankCreateDocumentsVisualPanel")})
    public static final class Factory extends ImportTableColumn.Factory {

        @Override
        public ImportTableColumn createInstance() {
            return new SiBankKursartColumn();
        }

    }
}
