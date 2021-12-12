/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.gymnasium.ui.imports;

import org.thespheres.betula.niedersachsen.*;
import org.thespheres.betula.niedersachsen.admin.ui.imports.KursartColumn;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class SiBankGymKursartColumn extends KursartColumn<SiBankImportTarget, SiBankKursItem> {

    public SiBankGymKursartColumn() {
        super(Unterricht.NAME);
    }

    @ImportTableColumn.Factory.Registrations({
        @ImportTableColumn.Factory.Registration(component = "SiBankCreateDocumentsVisualPanel")})
    public static final class Factory extends ImportTableColumn.Factory {

        @Override
        public ImportTableColumn createInstance() {
            return new SiBankGymKursartColumn();
        }

    }
}
