/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.berichte.ui;

import java.time.ZoneId;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.adminreports.TextKursUpdater;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.util.TextAssessmentEntry;
import org.thespheres.betula.niedersachsen.NdsCommonConstants;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
class NdsTextKursUpdater extends TextKursUpdater<NdsTextKursImportItem> {

    NdsTextKursUpdater(NdsTextKursImportItem[] kurse, WebServiceProvider provider, Term current, ConfigurableImportTarget cit) {
        super(kurse, provider, current, cit);
    }

    @Override
    protected void configureTextAssessmentEntry(TextAssessmentEntry tae, NdsTextKursImportItem k) {
        //Terms
        Marker fach = k.getFach();
        Marker ksgl = k.getKursart();
        Marker other = k.getOtherMarker();
        if (fach != null) {
            tae.getValue().getMarkerSet().add(fach);
        }
        if (ksgl != null) {
            tae.getValue().getMarkerSet().add(ksgl);
        }
        if (other != null) {
            tae.getValue().getMarkerSet().add(other);
        }
        tae.setTargetType(StringUtils.capitalize(NdsCommonConstants.SUFFIX_BERICHTE));
        tae.setDocumentValidity(k.getDeleteDate().atStartOfDay(ZoneId.systemDefault()));
    }

}
