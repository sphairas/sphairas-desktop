/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.lang.reflect.InvocationTargetException;
import org.thespheres.betula.Identity;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 */
public class IDUtilities {

    private IDUtilities() {
    }

    public static DocumentId parseDocumentId(String raw) throws IllegalArgumentException {
        int rb = raw.indexOf("}");
        String auth = raw.substring(1, rb);
        int v = raw.indexOf("#");
        DocumentId.Version version;
        String id;
        if (v != -1) {
            version = DocumentId.Version.parse(raw.substring(v + 1));
            id = raw.substring(rb + 1, v);
        } else {
            version = DocumentId.Version.UNSPECIFIED;
            id = raw.substring(rb);
        }
        return new DocumentId(auth, id, version);
    }

    public static StudentId parseStudentId(String raw) throws IllegalArgumentException {
        return parseId(raw, StudentId.class, Long.class);
    }

    public static UnitId parseUnitId(String raw) throws IllegalArgumentException {
        return parseId(raw, UnitId.class, String.class);
    }

    public static TermId parseTermId(String raw) throws IllegalArgumentException {
        return parseId(raw, TermId.class, Integer.class);
    }

    private static <T extends Object, I extends Identity<T>> I parseId(String raw, Class<I> type, Class<T> idType) {
        try {
            int rb = raw.indexOf("}");
            String auth = raw.substring(1, rb);
            String idval = raw.substring(rb + 1);
            T id = idType.getConstructor(String.class).newInstance(idval);
            return type.getConstructor(String.class, idType).newInstance(auth, id);
        } catch (IndexOutOfBoundsException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
