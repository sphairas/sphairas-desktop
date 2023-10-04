/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.awt.EventQueue;
import java.io.IOException;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NetworkSettings;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.util.ContentValueEntry;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.ContainerBuilder;

/**
 *
 * @author boris.heithecker
 */
@Messages({"Signees.label.inactive=\u00A0(Inaktiv)"})
public class Signees {

    private final static Collator COLLATOR = Collator.getInstance(Locale.GERMAN);
    private final static Marker INACTIVE_SIGNEE = MarkerFactory.find(SigneeStatus.NAME, "inactive", null);
    private final static Marker ACTIVE_SIGNEE = MarkerFactory.find(SigneeStatus.NAME, "active", null);
    private final String providerUrl;
    private final static HashMap<String, Object> INSTANCES = new HashMap<>();
    private RequestProcessor.Task task;
    private final SortedMap<Signee, Holder>[] SIGNEES = new SortedMap[]{new TreeMap<>()};

    private Signees(String p) {
        this.providerUrl = p;
    }

    public static Optional<Signees> get(final String providerUrl) {
        Object s;
        synchronized (INSTANCES) {
            s = INSTANCES.get(providerUrl);
            if (s == null) {
                try {
                    s = create(providerUrl);
                } catch (IOException ex) {
                    s = ex;
                }
                INSTANCES.put(providerUrl, s);
            }
            Signees sig = s instanceof Signees ? (Signees) s : null;
            return Optional.ofNullable(sig);
        }
    }

    private Map<Signee, Holder> getSignees() {
        if (task != null) {
            if (EventQueue.isDispatchThread()) {
                Logger.getLogger(Signees.class.getName()).log(Level.WARNING, "Signees.getSignees should not be called from AWT while signees are still being loaded.");
            }
            task.waitFinished();
        }
        return SIGNEES[0];
    }

    //Sorted
    public Set<Signee> getSigneeSet() {
        return Collections.unmodifiableSet(getSignees().keySet());
    }

    public Signee[] getActiveSignee() {
        return getSignees().entrySet().stream()
                .filter(e -> e.getValue().isActive())
                .toArray(Signee[]::new);
    }

    public Optional<Signee> findSignee(final String name) {
        return findSignee(name, false);
    }

    public Optional<Signee> findSignee(final String name, final boolean excludeInactive) {
        return getSignees().entrySet().stream()
                .filter(e -> e.getValue().getCn(false).equals(name))
                .filter(e -> !excludeInactive || isActive(e))
                .map(Map.Entry::getKey)
                //                .collect(CollectionUtil.singleton());
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Signees::toSet));
    }

    private static Optional<Signee> toSet(Set<Signee> set) {
        if (set.size() == 1) {
            return Optional.of(set.iterator().next());
        } else {
            final String msg = set.stream().map(s -> s.getName()).collect(Collectors.joining(","));
            Logger.getLogger(Signees.class.getName()).log(Level.INFO, "Duplicate " + msg);
            return Optional.empty();
        }
    }

    private static boolean isActive(Map.Entry<Signee, Holder> e) {
//        return !e.getValue().isInactive();
        final boolean inactive = e.getValue().isInactive();
        if (inactive) {
            Logger.getLogger(Signees.class.getName()).log(Level.INFO, "Found inactive " + e.getValue().getCn(true));
        }
        return !inactive;
    }

    public String getSignee(Signee signee) {
        return getSignee(signee, true);
    }

    public String getSignee(Signee signee, boolean addNonactiveStatus) {
        if (getSignees().containsKey(signee)) {
            return Optional.ofNullable(getSignees().get(signee))
                    .map(h -> h.getCn(addNonactiveStatus))
                    .orElse(null);
        }
        throw new IllegalArgumentException("Signee not contained in signees.");
    }

    public Marker[] getMarkers(Signee signee) {
        return Optional.ofNullable(getSignees().get(signee))
                .map(h -> h.markers.stream())
                .orElse(Stream.empty())
                .toArray(Marker[]::new);
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public void forceReload() {
        reload(this);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return 67 * hash + Objects.hashCode(this.providerUrl);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Signees other = (Signees) obj;
        return Objects.equals(this.providerUrl, other.providerUrl);
    }

    private static Signees create(final String providerUrl) throws IOException {
        final Signees ret = new Signees(providerUrl);
        return reload(ret);
    }

    private static Signees reload(final Signees ret) {
        WebServiceProvider service;
        try {
            service = WebProvider.find(ret.providerUrl, WebServiceProvider.class);
        } catch (NoProviderException e) {
            Logger.getLogger(Units.class.getName()).log(Level.WARNING, e.getMessage());
            return null;
        }
        final WebServiceProvider s = service;
        final RequestProcessor proc = s.getDefaultRequestProcessor();
        ret.task = proc.post(() -> {
            try {
                final TreeMap<Signee, Holder> ls = loadSignees(s.createServicePort());
                synchronized (ret) {
                    ret.SIGNEES[0] = ls;
                }
            } catch (IOException ex) {
                Logger.getLogger(Units.class.getName()).log(Level.INFO, "An error occurred loading Signees from provider ." + s.getInfo().getDisplayName(), ex);
                synchronized (INSTANCES) {
                    INSTANCES.put(ret.providerUrl, ex);
                }
            } finally {
                ret.task = null;
            }
        }, 0, Thread.NORM_PRIORITY);
        return ret;
    }

    private static TreeMap<Signee, Holder> loadSignees(BetulaWebService service) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        builder.createTemplate(null, null, null, Paths.SIGNEES_PATH, null, Action.REQUEST_COMPLETION);
        final Container response;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> service.solicit(builder.getContainer()));
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        //signees
        final List<Envelope> lt = DocumentUtilities.findEnvelope(response, Paths.SIGNEES_PATH);
        final Map<Signee, Holder> l = lt.stream()
                .filter(n -> Objects.equals(n.getAction(), Action.RETURN_COMPLETION))
                .map(Envelope::getChildren)
                .flatMap(List::stream)
                .filter(ContentValueEntry.class::isInstance)
                .map(e -> (ContentValueEntry<Signee>) e)
                .collect(Collectors.toMap(Entry::getIdentity, Holder::new));
        //See contract of TreeMap: keys are equal on grounds of the compareTo method!
        final TreeMap<Signee, Holder> ret = new TreeMap<>(Comparator.comparing(s -> l.containsKey(s) ? l.get(s).getCn(false) + s.toString() : s.toString(), COLLATOR));
        ret.putAll(l);
        return ret;
    }

    private final static class Holder {

        private static final String MESSAGE_INACTIVE = NbBundle.getMessage(Signees.class, "Signees.label.inactive");
        private final String cn;
        private final Set<Marker> markers;

        private Holder(ContentValueEntry<Signee> entry) {
            this.cn = entry.getStringValue();
            this.markers = entry.getValue().getMarkerSet().stream()
                    .collect(Collectors.toSet());
        }

        private String getCn(final boolean addStatus) {
            if (cn == null || cn.isEmpty()) {
                return "---";
            }
            if (addStatus && isInactive()) {
                return cn + MESSAGE_INACTIVE;
            }
            return cn;
        }

        boolean isInactive() {
            return markers.stream()
                    .anyMatch(INACTIVE_SIGNEE::equals);
        }

        boolean isActive() {
            final Marker t = markers.stream()
                    .filter(m -> m.getConvention().equals(SigneeStatus.NAME))
                    .collect(CollectionUtil.singleOrNull());
            return t == null || t.equals(ACTIVE_SIGNEE);
        }
    }
}
