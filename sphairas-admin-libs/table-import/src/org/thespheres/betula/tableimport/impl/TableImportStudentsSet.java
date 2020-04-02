/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.awt.Color;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.AbstractDelayedStudents;
import org.thespheres.betula.xmlimport.utilities.VCardStudentsUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"TableImportStudentsSet.noStudent.message=Kein Schüler/keine Schülerin \"{0}\" (Geburtsdatum: {1}) in der Datenbank gefunden!",
    "TableImportStudentsSet.multipleStudents.message=Mehrere Schüler/Schülerinnnen \"{0}\" (Geburtsdatum: {1}) in der Datenbank gefunden!"})
public class TableImportStudentsSet<I extends AbstractXmlCsvImportItem> extends AbstractDelayedStudents<I> {

    private final Map<ImportStudentKey, Holder> studs = new HashMap<>();

    public TableImportStudentsSet(I target) {
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
        final List<VCardStudent> found = VCardStudentsUtil.find(students, isk);
        if (found.isEmpty()) {
            holder.message = NbBundle.getMessage(TableImportStudentsSet.class, "TableImportStudentsSet.noStudent.message", isk.getSourceName(), isk.getSourceDateOfBirth());
        } else if (found.size() > 1) {
            holder.message = NbBundle.getMessage(TableImportStudentsSet.class, "TableImportStudentsSet.multipleStudents.message", isk.getSourceName(), isk.getSourceDateOfBirth());
        } else {
            initHolder(holder, found.get(0));
        }
    }

    protected void initHolder(final Holder holder, VCardStudent found) {
        holder.id = found.getStudentId();
    }

    protected boolean compareDateOfBirth(ImportStudentKey k, VCardStudent vcs) {
        return k.getDateOfBirth() == null || vcs.getDateOfBirth().equals(k.getDateOfBirth());
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

    public StudentId find(ImportStudentKey key) {
        final Holder h;
        synchronized (studs) {
            h = studs.get(key);
        }
        return h == null ? null : h.id;
    }

    public boolean isValid() {
        final List<Holder> l;
        synchronized (studs) {
            l = studs.values().stream()
                    .filter(h -> h.id == null)
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

    public void set(List<ImportStudentKey> l) throws IOException {
        final VCardStudents students = getVCardStudents();
        synchronized (studs) {
            studs.clear();
            l.forEach(key -> {
                final Holder ret = studs.computeIfAbsent(key, k -> createHolder(k));
                if (students.getLoadTask().isFinished()) {
                    initHolder(key, ret);
                }
            });
        }
    }

    protected Holder createHolder(ImportStudentKey key) {
        return new Holder(key);
    }

    public Set<ImportStudentKey> keys() {
        final Set<ImportStudentKey> ret;
        synchronized (studs) {
            ret = studs.keySet().stream()
                    .collect(Collectors.toSet());
        }
        return ret;
    }

    public boolean isEmpty() {
        synchronized (studs) {
            return studs.isEmpty();
        }
    }

    protected class Holder {

        private StudentId id;
        private String message;

        protected Holder(ImportStudentKey k) {
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
