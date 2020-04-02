/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.gs.ui.imports;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.util.UnitInfo;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.parse.NameParser;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
public class CrossMarksImportTargetsItem extends ImportTargetsItem {

    private final DocumentId docBase;
    private final ConfigurableImportTarget cit;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    private CrossMarksImportTargetsItem(final String sourceNode, final UnitId unit, final Marker subject, final DocumentId base, final String convention, final StudentId[] students, final ConfigurableImportTarget config) {
        super(sourceNode);
        setUnitId(unit);
        setSubjectMarker(subject);
        this.docBase = base;
        this.students = students;
        this.cit = config;
        this.preferredConvention = convention;
    }

    public static CrossMarksImportTargetsItem[] createForUnit(final UnitId unit, final Signee sig, final int level, final MarkerConvention mcd, final String preferredConvention, final String provider) {
        final LocalDate del = ImportUtil.calculateDeleteDate(level, 5, Month.JULY);
        final Marker[] all = mcd.getAllMarkers();
        final ConfigurableImportTarget t = ConfigurableImportTarget.find(provider);
        final String idBase = mcd.getDisplayName().toLowerCase(Locale.getDefault());

        final NamingResolver nr = t.getNamingResolver();
        final String first = nr.properties().get("first-element");
        final String bl = nr.properties().get("base-level");
        Integer baseLevel = null;
        if (bl != null) {
            try {
                baseLevel = Integer.parseInt(bl);
            } catch (NumberFormatException nfex) {
            }
        }
        final NameParser pn2 = new NameParser(t.getAuthority(), first, baseLevel);

        final StudentId[] studs = Units.get(provider)
                .filter(u -> u.hasUnit(unit))
                .map(u -> {
                    try {
                        return u.fetchParticipants(unit, null);
                    } catch (IOException ex) {
                        return null;
                    }
                })
                .map(UnitInfo::getStudents)
                .orElse(new StudentId[0]);

        final List<CrossMarksImportTargetsItem> l = new ArrayList<>();
        for (final Marker m : all) {

//            System.out.println("Found marker " + m.getLongLabel() + " Short " + m.getShortLabel());

            final String id = idBase + "." + m.getSubset() + "." + m.getId();
            final DocumentId tb = pn2.translateUnitIdToTargetDocumentBase(unit.getId(), id, null);

            final CrossMarksImportTargetsItem cmfuit = new CrossMarksImportTargetsItem(id, unit, m, tb, preferredConvention, studs, t);
            cmfuit.setDeleteDate(del);
            if (sig != null) {
                cmfuit.setSignee(sig);
            }
            l.add(cmfuit);
        }

        return l.stream().toArray(CrossMarksImportTargetsItem[]::new);
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        if (cit != null && isValid()) {
            return cit.createTargetDocuments(this);
        }
        return new TargetDocumentProperties[0];
    }

    @Override
    public DocumentId getTargetDocumentIdBase() {
        return docBase;
    }

    @Override
    public boolean fileUnitParticipants() {
        return false;
    }

    @Override
    public String getUnitDisplayName() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public boolean isUnitIdGenerated() {
        return false;
    }

    @Override
    public boolean existsUnitInSystem() {
        return true;
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public boolean isValid() {
        return getDeleteDate() != null
                && getUnitId() != null
                && getTargetDocumentIdBase() != null
                && !Marker.isNull(getSubjectMarker())
                && getUnitStudents() != null && getUnitStudents().length != 0;
    }

}
