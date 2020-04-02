/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.impl;

import org.thespheres.betula.xmlimport.utilities.AbstractDelayedStudents;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import java.beans.PropertyVetoException;
import org.thespheres.betula.sibank.SiBankImportStudentItem;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;
import org.thespheres.betula.sibank.SiBankKlasseItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.ical.VCard;
import org.thespheres.ical.util.VCardHolder;

/**
 *
 * @author boris.heithecker
 */
//@Messages({"DelayedStudentSet.noStudent.message=Kein Schüler/keine Schülerin \"{0}\" (Geburtsdatum: {1}) in der Datenbank gefunden!",
//    "DelayedStudentSet.multipleStudents.message=Mehrere Schüler/Schülerinnnen \"{0}\" (Geburtsdatum: {1}) in der Datenbank gefunden!"})
public class DelayedKlasseStudentSet extends AbstractDelayedStudents<SiBankKlasseItem> {

    private final Map<StudentId, SiBankImportStudentItem> studs = new ConcurrentHashMap<>();

    public DelayedKlasseStudentSet(SiBankKlasseItem target) {
        super(target);
    }

    @Override
    protected void onLoad() {
        synchronized (studs) {
            for (Map.Entry<StudentId, SiBankImportStudentItem> e : studs.entrySet()) {
                final StudentId isk = e.getKey();
                final SiBankImportStudentItem holder = e.getValue();
                initHolder(isk, holder);
            }
        }
    }

    protected void initHolder(final StudentId id, final SiBankImportStudentItem holder) throws MissingResourceException {
        final VCardStudents students;
        try {
            students = getVCardStudents();
        } catch (IOException ex) {
            //should not happen here
            throw new IllegalStateException(ex);
        }
        VCardStudent found = students.find(id);
        if (found != null) {
            try {
                ((ImportStudentItemExt) holder).setExistingVCard(found.getVCard());
            } catch (PropertyVetoException ex) {
                ImportUtil.getIO().getErr().write(ex.getLocalizedMessage());
            }
        }
    }

    public Map<StudentId, SiBankImportStudentItem> getStudents() {
        return Collections.unmodifiableMap(studs);
    }

    public StudentId[] getUnitStudents() {
        synchronized (studs) {
            return studs.keySet().stream()
                    .toArray(StudentId[]::new);
        }
    }

    public boolean isValid() {
        final List<SiBankImportStudentItem> l;
        synchronized (studs) {
            l = studs.values().stream()
                    .filter(h -> !h.isValid())
                    .map(ImportStudentItemExt.class::cast)
                    .filter(h -> !h.ready)
                    .collect(Collectors.toList());
        }
        l.stream()
                .forEach(SiBankImportStudentItem::log);
        return l.isEmpty();
    }

    public SiBankImportStudentItem put(final VCardStudent stud, final ImportStudentKey key) throws IOException {
        final VCardStudents students = getVCardStudents();
        final SiBankImportStudentItem h;
        synchronized (studs) {
            h = studs.computeIfAbsent(stud.getStudentId(), s -> {
                final SiBankImportStudentItem ret = new ImportStudentItemExt(stud, key, target);
                if (students.getLoadTask().isFinished()) {
                    initHolder(stud.getStudentId(), ret);
                }
                return ret;
            });
        }
        final UnitId[] pu = h.getPrimaryUnits();
        final UnitId[] pus;
        if (pu != null) {
            pus = Stream.concat(Arrays.stream(pu), Stream.of(target.getUnitId()))
                    .distinct()
                    .toArray(UnitId[]::new);
        } else {
            pus = new UnitId[]{target.getUnitId()};
        }
        h.setPrimaryUnits(pus);
        return h;
    }

    public final class ImportStudentItemExt extends SiBankImportStudentItem {

        private VCardHolder vCardOverride;
        private boolean ready;

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        public ImportStudentItemExt(VCardStudent vcs, ImportStudentKey s, SiBankKlasseItem klasse) {
            super(vcs, s, klasse);
        }

        private void setExistingVCard(VCard card) throws PropertyVetoException {
            this.vCardOverride = new VCardHolder(card);
            this.ready = true;
        }

        @Override
        public VCard getVCard() {
//            if (vCardOverride != null) {
//                return vCardOverride.getVCard();
//            }
            return super.getVCard();
        }

        public VCardHolder getVCardOverride() {
            return vCardOverride;
        }

    }
}
