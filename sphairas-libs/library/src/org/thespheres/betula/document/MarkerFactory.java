/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.Convention;
import org.thespheres.betula.Tag;
import org.thespheres.betula.tag.TagConvention;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class MarkerFactory {

    static final long CASH_TIME = 60 * 1000; //60 Sek
    private static final HashMap<Key, Holder> MARKER_CACHE = new HashMap<>();
    private static final HashMap<Key, WeakReference<Tag>> TAG_CACHE = new HashMap<>();
    private static final Mutex CACHE_ACCESS = new Mutex();

    public static Marker parse(String convention, String longLabel) throws MarkerParsingException {
        MarkerConvention con = findConvention(convention, MarkerConvention.class);
        if (con != null) {
            try {
                return con.parseMarker(longLabel);
            } catch (MarkerParsingException ex) {
                throw new MarkerParsingException(convention, longLabel);
            }
        } else {
            throw new MarkerParsingException(convention, longLabel);
        }
    }

    public static Marker resolve(final String respresentation) {
        final Marker res = resolveAbstract(respresentation);
        if (Marker.isNull(res)) {
            return Marker.NULL;
        } else {
            return MarkerFactory.find(res.getConvention(), res.getId(), res.getSubset());
        }
    }

    public static Marker resolveAbstract(final String representation) {
        if (representation == null || representation.isEmpty()) {
            return Marker.NULL;
        }
        final String[] parts = representation.split("#");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Marker can have only one # character.");
        }
        final String cnv = parts[0];
        final String[] idParts = parts[1].split("/");
        final String id;
        final String subset;
        switch (parts.length) {
            case 1:
                subset = null;
                id = idParts[0];
                break;
            case 2:
                subset = idParts[0];
                id = idParts[1];
                break;
            default:
                throw new IllegalArgumentException("Marker id can have only one / character.");
        }
        if (!cnv.equals(StringUtils.strip(cnv))) {
            throw new IllegalArgumentException("Untrimmed input \"" + cnv + "\"");
        }
        if (!id.equals(StringUtils.strip(id))) {
            throw new IllegalArgumentException("Untrimmed input \"" + id + "\"");
        }
        if (subset != null && !subset.equals(StringUtils.strip(subset))) {
            throw new IllegalArgumentException("Untrimmed input \"" + subset + "\"");
        }
        return new AbstractMarker(cnv, id, subset);
    }

    public static Tag find(String convention, String id) {
        if (id == null) {
            return null;
        }
        if (convention == null) {
            throw new IllegalArgumentException("Convention cannot be null.");
        }
        final Key k = new Key(convention, id);
        Tag ret = CACHE_ACCESS.readAccess((Mutex.Action<Tag>) () -> {
            if (TAG_CACHE.containsKey(k)) {
                WeakReference<? extends Tag> ref = TAG_CACHE.get(k);
                if (ref.get() != null) {
                    return ref.get();
                }
            }
            return null;
        });
        if (ret != null) {
            return ret;
        }
        TagConvention con = findConvention(convention, TagConvention.class);
        if (con != null) {
            try {
                try {
                    ret = CACHE_ACCESS.writeAccess((Mutex.ExceptionAction<Tag>) () -> {
                        try {
                            final Tag m = con.find(id);
                            TAG_CACHE.put(k, new WeakReference(m));
                            return m;
                        } catch (IllegalArgumentException ex) {
                            throw ex;
                        }
                    });
                } catch (MutexException ex) {
                    Logger.getLogger(MarkerFactory.class.getName()).log(Level.WARNING, ex.getException().getLocalizedMessage(), ex.getException());
                }
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(MarkerFactory.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        return ret;
    }

    public static Marker find(String convention, String id, String subset) {
        if (id == null) {
            return null;
        }
        if (convention == null) {
            throw new IllegalArgumentException("Convention cannot be null.");
        }
        final Key k = new Key(convention, id, subset);
        Marker ret = CACHE_ACCESS.readAccess((Mutex.Action<Marker>) () -> {
            if (MARKER_CACHE.containsKey(k)) {
                final Holder h = MARKER_CACHE.get(k);
                if (h.ref.get() != null && !h.expired()) {
                    return h.ref.get();
                }
            }
            return null;
        });
        if (ret != null) {
            return ret;
        }
        final MarkerConvention con = findConvention(convention, MarkerConvention.class);
        final boolean isMutable = con instanceof MarkerConvention.Mutable;
        if (con != null) {
            try {
                if (!isMutable) {
                    try {
                        ret = CACHE_ACCESS.writeAccess((Mutex.ExceptionAction<Marker>) () -> {
                            try {
                                final Marker m = con.find(id, subset);
                                MARKER_CACHE.put(k, new Holder(m));
                                return m;
                            } catch (IllegalArgumentException ex) {
                                throw ex;
                            }
                        });
                    } catch (MutexException ex) {
                        Logger.getLogger(MarkerFactory.class.getName()).log(Level.WARNING, ex.getException().getLocalizedMessage(), ex.getException());
                    }
                } else {
                    return con.find(id, subset);
                }
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(MarkerFactory.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        return ret;
    }

    public static MarkerConvention findConvention(String convention) {
        return findConvention(convention, MarkerConvention.class);
    }

    public static TagConvention findTagConvention(String convention) {
        return findConvention(convention, TagConvention.class);
    }

    private static <C extends Convention> C findConvention(final String convention, final Class<C> type) {

//        final ServiceLoader<C> loader = ServiceLoader.load(type);
//        C found = StreamSupport.stream(loader.spliterator(), false)
//                .filter(c -> c.getName().equals(convention))
//                .collect(CollectionUtil.requireSingleOrNull());
        final C found = Lookup.getDefault().lookupAll(type).stream()
                .filter(c -> c.getName().equals(convention))
                .collect(CollectionUtil.requireSingleOrNull());
        if (found != null) {
            return found;
        }

        return Lookups.forPath("Convention/" + type.getSimpleName().replace("Convention", "") + "/").lookupAll(type).stream()
                .filter(ac -> ac.getName().equals(convention))
                .collect(CollectionUtil.requireSingleOrNull());
    }

    public static Marker[] findAllChildrenMarkers(Marker parent) {
        return null;
    }

    private static final class Key {

        private final String convention;
        private final String id;
        private final String subset;
        private final boolean marker;

        private Key(String convention, String id, String subset) {
            this.convention = convention;
            this.id = id;
            this.subset = subset;
            this.marker = true;
        }

        private Key(String convention, String id) {
            this.convention = convention;
            this.id = id;
            this.subset = null;
            this.marker = false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + Objects.hashCode(this.marker);
            hash = 17 * hash + Objects.hashCode(this.convention);
            hash = 17 * hash + Objects.hashCode(this.id);
            hash = 17 * hash + Objects.hashCode(this.subset);
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
            if (!Objects.equals(this.marker, other.marker)) {
                return false;
            }
            if (!Objects.equals(this.convention, other.convention)) {
                return false;
            }
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return Objects.equals(this.subset, other.subset);
        }

    }

    private static final class Holder {

        private final WeakReference<Marker> ref;
        private final long time;

        Holder(Marker ref) {
            this.ref = new WeakReference<>(ref);
            this.time = System.currentTimeMillis();
        }

        boolean expired() {
            return System.currentTimeMillis() - time > CASH_TIME;
        }

    }
}
