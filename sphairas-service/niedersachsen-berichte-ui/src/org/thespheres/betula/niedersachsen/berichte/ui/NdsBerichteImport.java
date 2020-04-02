/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.berichte.ui;

import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.*;
import org.thespheres.betula.adminreports.TextKursUpdater;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.niedersachsen.LSchB;
import org.thespheres.betula.niedersachsen.NdsCommonConstants;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.adminreports.BerichteImport;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = BerichteImport.class)
public class NdsBerichteImport implements BerichteImport<NdsTextKursImportItem> {

    @Override
    public ProviderInfo getProvider() {
        return LSchB.PROVIDER_INFO;
    }

    @Override
    public String getBerichteSuffix() {
        return NdsCommonConstants.SUFFIX_BERICHTE;
    }

    @Override
    public NdsTextKursImportItem createTextKurs(UnitId unit, DocumentId target) {
        return new NdsTextKursImportItem(unit, target);
    }

    @Override
    public TextKursUpdater createUpdater(NdsTextKursImportItem[] kurse, WebServiceProvider provider, Term current, ConfigurableImportTarget cit) {
        return new NdsTextKursUpdater(kurse, provider, current, cit);
    }

}
