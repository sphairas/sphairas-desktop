/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.awt.Color;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import java.io.IOException;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.model.XmlStudentItem;
import org.thespheres.betula.xmlimport.utilities.AbstractDelayedStudents;
import org.thespheres.betula.xmlimport.utilities.VCardStudentsUtil;

/**
 *
 * @author boris.heithecker
 */
@Messages("TableImportStudentsSet.studentId.conflict.message=Es wurden mehrere Schülerinnen/Schüler mit der ID {0} in der Datenbank gefunden: {1}")
public class PrimaryUnitImportStudentsSet extends AbstractDelayedStudents<PrimaryUnitsXmlCsvItem> {

    private final Map<ImportStudentKey, Holder> studs = new HashMap<>();

    PrimaryUnitImportStudentsSet(PrimaryUnitsXmlCsvItem target) {
        super(target);
    }

    @Override
    protected void onLoad() {
        synchronized (studs) {
            for (Map.Entry<ImportStudentKey, Holder> e : studs.entrySet()) {
                final ImportStudentKey isk = e.getKey();
                final Holder holder = e.getValue();
                initHolder(isk, holder);
            }
        }
    }

    protected void initHolder(final ImportStudentKey isk, final Holder holder) throws MissingResourceException {
        final VCardStudents students;
        try {
            students = getVCardStudents();
        } catch (IOException ex) {
            //should not happen here
            throw new IllegalStateException(ex);
        }
        VCardStudent card = null;
        final StudentId id = holder.getStudentId();
        if (id != null) {
            final List<VCardStudent> found = students.getStudents().stream()
                    .filter(vcs -> vcs.getStudentId().equals(id))
                    .collect(Collectors.toList());
            if (found.size() != 1) {
                final String names = found.stream()
                        .map(vcs -> vcs.getFirstName())
                        .collect(Collectors.joining(", "));
                holder.message = NbBundle.getMessage(PrimaryUnitImportStudentsSet.class, "TableImportStudentsSet.studentId.conflict.message", id.toString(), names);
            } else {
                card = found.get(0);
            }
        } else {
            final List<VCardStudent> found = VCardStudentsUtil.find(students, isk);
            if (found.isEmpty()) {
                holder.message = NbBundle.getMessage(TableImportStudentsSet.class, "TableImportStudentsSet.noStudent.message", isk.getSourceName(), isk.getSourceDateOfBirth());
            } else if (found.size() > 1) {
                holder.message = NbBundle.getMessage(TableImportStudentsSet.class, "TableImportStudentsSet.multipleStudents.message", isk.getSourceName(), isk.getSourceDateOfBirth());
            } else {
                card = found.get(0);
            }
        }
        if (card != null) {
            initHolder(holder, card);
        }
    }

    protected void initHolder(final Holder holder, VCardStudent found) {
        holder.setStudentId(found.getStudentId());
        holder.setOriginalVCard(found.getVCard());
    }

    protected boolean compareDateOfBirth(ImportStudentKey k, VCardStudent vcs) {
        return k.getDateOfBirth() == null || vcs.getDateOfBirth().equals(k.getDateOfBirth());
    }

    public boolean isValid() {
        final List<Holder> l;
        synchronized (studs) {
            l = studs.values().stream()
                    .filter(h -> h.getStudentId() == null)
                    .collect(Collectors.toList());
        }
        l.stream()
                .forEach(Holder::log);
        return l.isEmpty();
    }

    public void clear() {
        synchronized (studs) {
            studs.clear();
        }
    }

    public StudentId[] getUnitStudents() {
        synchronized (studs) {
            return studs.values().stream()
                    .map(PrimaryUnitImportStudentItem::getStudentId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toArray(StudentId[]::new);
        }
    }

    public List<PrimaryUnitImportStudentItem> values() {
        synchronized (studs) {
            return studs.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getSourceName(), Collator.getInstance(Locale.getDefault())))
                    .map(Entry::getValue)
                    .collect(Collectors.toList());
        }
    }

    public PrimaryUnitImportStudentItem put(final ImportStudentKey key, final XmlStudentItem stud) throws IOException {
        final VCardStudents students = getVCardStudents();
        final PrimaryUnitImportStudentItem h;
        synchronized (studs) {
            h = studs.computeIfAbsent(key, s -> {
                final Holder ret = new Holder(key, stud, target.getSource().getSourceUnit());
                if (students.getLoadTask().isFinished()) {
                    initHolder(key, ret);
                }
                return ret;
            });
        }
        //If uncomment -> call  setClientProperty(PROP_IMPORT_TARGET, config); in PrimaryUnitsXmlCsvItem BEFORE     setStudents(ls); !!!
//        final UnitId[] pu = h.getPrimaryUnits();
//        final UnitId[] pus;
//        if (pu != null) {
//            pus = Stream.concat(Arrays.stream(pu), Stream.of(target.getUnitId()))
//                    .distinct()
//                    .toArray(UnitId[]::new);
//        } else {
//            pus = new UnitId[]{target.getUnitId()};
//        }
//        h.setPrimaryUnits(pus);
        return h;
    }

    public boolean isEmpty() {
        synchronized (studs) {
            return studs.isEmpty();
        }
    }

    protected class Holder extends PrimaryUnitImportStudentItem {

        private String message;

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private Holder(ImportStudentKey key, XmlStudentItem stud, String sourceUnit) {
            super(key, stud, sourceUnit);
            setStudentId(stud.getStudent());
            setSelected(true);
        }

        private void log() {
            if (!StringUtils.isBlank(message)) {
                InputOutput io = ImportUtil.getIO();
                try {
                    IOColorLines.println(io, message, Color.RED);
                } catch (IOException ex) {
                    io.getOut().println(message);
                }
                message = null;
            }
        }
    }
}
