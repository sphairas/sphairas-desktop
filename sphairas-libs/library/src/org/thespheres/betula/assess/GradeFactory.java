/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author boris.heithecker
 */
public class GradeFactory {

    private static final HashMap<Key, WeakReference<Grade>> CACHE = new HashMap<>();
    private static final Mutex CACHE_ACCESS = new Mutex();

    public static Grade parse(String convention, String text) throws GradeParsingException {
        AssessmentConvention con = findConvention(convention);
        if (con != null) {
            try {
                return con.parseGrade(text);
            } catch (GradeParsingException ex) {
                throw new GradeParsingException(convention, text);
            }
        } else {
            throw new GradeParsingException(convention, text);
        }
    }

    public static Grade resolve(final String representation) {
        if (representation == null || representation.isEmpty()) {
            return null;
        }
        final String[] parts = representation.split("#");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Grade can have only one # character.");
        }
        final String cnv = parts[0];
        if (!cnv.equals(StringUtils.strip(cnv))) {
            throw new IllegalArgumentException("Untrimmed input \"" + cnv + "\"");
        }
        final String id = parts[1];
        if (!id.equals(StringUtils.strip(id))) {
            throw new IllegalArgumentException("Untrimmed input \"" + id + "\"");
        }
        return GradeFactory.find(cnv, id);
    }

    public static Grade find(String convention, String id) {
        if (id == null) {
            return null;
        }
        if (convention == null) {
            throw new IllegalArgumentException("Convention cannot be null.");
        }
        final Key k = new Key(convention, id);
        Grade ret = CACHE_ACCESS.readAccess((Action<Grade>) () -> {
            if (CACHE.containsKey(k)) {
                WeakReference<Grade> ref = CACHE.get(k);
                if (ref.get() != null) {
                    return ref.get();
                }
            }
            return null;
        });
        if (ret != null) {
            return ret;
        }
        AssessmentConvention con = findConvention(convention);
        if (con != null) {
            try {
                ret = CACHE_ACCESS.writeAccess((ExceptionAction<Grade>) () -> {
                    try {
                        final Grade g = con.find(id);
                        CACHE.put(k, new WeakReference(g));
                        return g;
                    } catch (IllegalArgumentException ex) {
                        throw ex;
                    }
                });
            } catch (MutexException ex) {
                Logger.getLogger(GradeFactory.class.getName()).log(Level.WARNING, ex.getException().getLocalizedMessage(), ex.getException());
            }
        } else {
            Logger.getLogger(GradeFactory.class.getName()).log(Level.WARNING, "Convention {0} not found.", convention);
        }
        return ret;
    }

    public static AssessmentConvention findConvention(String convention) {
        ServiceLoader<AssessmentConvention> loader = ServiceLoader.load(AssessmentConvention.class);
        for (AssessmentConvention con : loader) {
            if (con.getName().equals(convention)) {
                return con;
            }
        }
        //translate legacy names
        switch (convention) {
            case "SEK I":
                return findConvention("de.notensystem");
            case "SEK II":
                return findConvention("de.notensystem.os");
        }
        return Lookups.forPath("Convention/Assessment").lookupAll(AssessmentConvention.class).stream()
                .filter(ac -> ac.getName().equals(convention))
                .findAny()
                .orElse(null);
    }

    private static final class Key {

        private final String convention;
        private final String id;

        public Key(String convention, String id) {
            this.convention = convention;
            this.id = id;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + Objects.hashCode(this.convention);
            hash = 89 * hash + Objects.hashCode(this.id);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (!Objects.equals(this.convention, other.convention)) {
                return false;
            }
            return Objects.equals(this.id, other.id);
        }

    }
}
