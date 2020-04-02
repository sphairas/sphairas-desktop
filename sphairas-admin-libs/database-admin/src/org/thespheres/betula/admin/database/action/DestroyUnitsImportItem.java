/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.util.Date;
import java.util.Optional;
import org.openide.util.Lookup;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.AdminUnit;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.WorkingDate;

/**
 *
 * @author boris.heithecker
 */
class DestroyUnitsImportItem extends AbstractImportTargetsItem {

    private final String info;

    private DestroyUnitsImportItem(String sourceNode, UnitId unit, DocumentId id, final String message) {
        super(sourceNode, id, true);
        this.info = message;
        this.unit = unit;
    }

    static DestroyUnitsImportItem create(final AdminUnit ru, final WebServiceProvider service) {
        final String provider = ru.getProvider();
        final LocalProperties lp = LocalProperties.find(provider);
        final DocumentsModel model = new DocumentsModel();
        model.initialize(lp.getProperties());
        final UnitId uid = ru.getUnitId();
        final DocumentId doc = model.convertToUnitDocumentId(uid);
//        final ConfigurableImportTarget it = TargetsUtil.findCommonImportTarget(provider);
        final TermSchedule schedule = Optional.ofNullable(lp)
                .map(p -> p.getProperty("termSchedule.providerURL"))
                .map(tp -> SchemeProvider.find(tp))
                .map(t -> t.getScheme(lp.getProperty("termSchemeId", TermSchedule.DEFAULT_SCHEME), TermSchedule.class))
                .orElse(null);
        final NamingResolver np = Optional.ofNullable(lp)
                .map(p -> p.getProperty("naming.providerURL", provider))
                .map(tp -> NamingResolver.find(provider))
                .orElse(null);
        final Date workingDate = Lookup.getDefault().lookup(WorkingDate.class).getCurrentWorkingDate();
        String label = null;
        try {
            if (np != null && schedule != null) {
                label = np.resolveDisplayNameResult(uid).getResolvedName(schedule.getTerm(workingDate));
            }
        } catch (IllegalAuthorityException ex) {
        }
        if (label == null) {
            label = uid.toString();
        }
//        long[] count = new long[]{0l};
        final String message = null; //createMessage(s, i, count[0]);
        return new DestroyUnitsImportItem(label, uid, doc, message);
    }

    public String getInfo() {
        return info;
    }

    @Override
    public UnitId getUnitId() {
        return unit;
    }

    @Override
    public Marker[] allMarkers() {
        return null;
    }

}
