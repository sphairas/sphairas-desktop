/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.ExceptionMessage;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.UnitEntry;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class UnitInfo {

    protected UnitId requested;
    private Object value;
    private StudentId[] students;

    protected void beforeSolicit(UnitEntry requestUnitEntry, UnitId unit) {
        requested = unit;
    }

    public UnitEntry getResponseUnitEntry() throws IOException {
        if (value == null) {
            throw new IOException("No responseUnitEntry available.");
        } else if (value instanceof IOException) {
            throw new IOException((Exception) value);
        }
        return (UnitEntry) value;
    }

    protected void extractResponseUnitEntry(final Container response) {
        final List<Entry<UnitId, ?>> l = DocumentUtilities.findEntry(response, Paths.UNITS_PARTICIPANTS_PATH, UnitId.class);
        final UnitEntry ret = l.stream()
                .filter(e -> e.getIdentity().equals(requested))
                .collect(CollectionUtil.requireSingleton())
                .filter(this::processException)
                .map(u -> u.getChildren().stream())
                .flatMap(s -> s.collect(CollectionUtil.singleton()))
                .filter(t -> t.getAction().equals(Action.RETURN_COMPLETION))
                .filter(UnitEntry.class::isInstance)
                .map(UnitEntry.class::cast)
                .orElse(null);
        if (ret != null) {
            value = ret;
        }
    }

    protected boolean processException(final Entry<UnitId, ?> entry) {
        final ExceptionMessage em = entry.getException();
        if (em == null) {
            return true;
        }
        final StringJoiner sj = new StringJoiner("/n");
        sj.add(em.getUserMessage());
        sj.add(em.getLogMessage());
        sj.add(em.getStackTraceElement());
        value = new IOException(sj.toString());
        return false;
    }

    public StudentId[] getStudents() {
        if (students == null) {
            throw new IllegalStateException("Students not extracted.");
        }
        return students;
    }

    protected void extractStudentIdsFormResponseUnitEntry() throws IOException {
        students = getResponseUnitEntry().getChildren().stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .map(Entry::getIdentity)
                .filter(StudentId.class::isInstance)
                .map(StudentId.class::cast)
                .distinct()
                .toArray(StudentId[]::new);
    }

}
