/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.impl;

import org.thespheres.betula.xmlimport.utilities.AbstractDelayedStudents;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.VCardStudentsUtil;

/**
 *
 * @author boris.heithecker
 */
@Messages({"DelayedKursStudentSet.noStudent.message=Kein Schüler/keine Schülerin \"{0}\" (Geburtsdatum: {1}) in der Datenbank gefunden!",
    "DelayedKursStudentSet.multipleStudents.message=Mehrere Schüler/Schülerinnnen \"{0}\" (Geburtsdatum: {1}) in der Datenbank gefunden!",
    "DelayedKursStudentSet.multipleStudentsResolved.message=Mehrere Schüler/Schülerinnnen \"{0}\" (Geburtsdatum: {1}) in der Datenbank gefunden; konnte {2} aus SiBank-Assoziationen zuordnen."})
public class DelayedKursStudentSet extends AbstractDelayedStudents<SiBankKursItem> {

    private final Map<ImportStudentKey, Holder> studs = new HashMap<>();

    public DelayedKursStudentSet(final SiBankKursItem target) {
        super(target);
    }

    public void copyFrom(DelayedKursStudentSet other) throws IOException {
        synchronized (studs) {
            for (Entry<ImportStudentKey, Holder> o : other.studs.entrySet()) {
                put(o.getKey(), o.getValue().grade);
            }
        }
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
        final List<VCardStudent> found = VCardStudentsUtil.find(students, isk);
        if (found.isEmpty()) {
            holder.message = NbBundle.getMessage(DelayedKursStudentSet.class, "DelayedKursStudentSet.noStudent.message", isk.getSourceName(), isk.getSourceDateOfBirth());
        } else {
            if (found.size() > 1) {
                final Optional<StudentId> assoc = SyncNames2Associations.tryResolve(isk, found, target.getImportData());
                if (assoc.isPresent()) {
                    holder.message = NbBundle.getMessage(DelayedKursStudentSet.class, "DelayedKursStudentSet.multipleStudentsResolved.message", isk.getSourceName(), isk.getSourceDateOfBirth(), assoc.get().toString());
                    holder.id = assoc.get();
                } else {
                    holder.message = NbBundle.getMessage(DelayedKursStudentSet.class, "DelayedKursStudentSet.multipleStudents.message", isk.getSourceName(), isk.getSourceDateOfBirth());
                }
            } else {
                holder.id = found.get(0).getStudentId();
            }
            if (holder.id != null) {
                final Grade g = holder.grade;
                if (g != null) {
                    submitGrade(holder, g);
                }
            }
        }
    }

    public StudentId[] getUnitStudents() {
        synchronized (studs) {
            return studs.values().stream()
                    .map(h -> h.id)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toArray(StudentId[]::new);
        }
    }

    public boolean isValid() {
        final List<Holder> l;
        synchronized (studs) {
            l = studs.values().stream()
                    .filter(h -> h.id == null && h.log() == null)
                    .collect(Collectors.toList());
        }
//        l.stream()
//                .forEach(Holder::log);
        return l.isEmpty();
    }

    public void put(final ImportStudentKey key, Grade g) throws IOException {
        final VCardStudents students = getVCardStudents();
        synchronized (studs) {
            //!! ImportStudentKey.equals(other) does not respect StudentId if set.
            //!! This may be a cause of possibles errors.
            final Holder h = studs.computeIfAbsent(key, k -> {
                final Holder ret = new Holder();
                if (students.getLoadTask().isFinished()) {
                    initHolder(key, ret);
                }
                return ret;
            });
            h.grade = g;
            submitGrade(h, g);
        }
    }

    private void submitGrade(Holder h, Grade g) {
        if (h.id != null && h.grade != null && target.getTerm() != null) {
            target.submit(h.id, target.getTerm().getScheduledItemId(), g, Timestamp.now());
        }
    }

    private final class Holder {

        private StudentId id;
        private Grade grade;
        private String message;

        private Holder() {
        }

        private String log() {
            if (!StringUtils.isBlank(message)) {
                InputOutput io = ImportUtil.getIO();
                try {
                    IOColorLines.println(io, message, Color.RED);
                } catch (IOException ex) {
                    io.getOut().println(message);
                }
                final String ret = message;
                message = null;
                return ret;
            }
            return null;
        }
    }
}
