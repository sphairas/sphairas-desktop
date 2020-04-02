/*
 * To change this license header, choose License Headers in Project AppProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util.dav;

import com.google.common.collect.ImmutableSet;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Students;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.ical.builder.VCardBuilder;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.dav.AddressData;
import org.thespheres.betula.services.dav.CardDavProp;
import org.thespheres.betula.services.dav.Multistatus;
import org.thespheres.betula.services.ui.util.AppProperties;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.VCard;

/**
 *
 * @author boris.heithecker
 */
public class VCardStudents extends Students<VCardStudent> {

    private final static HashMap<Key, Object> INSTANCES = new HashMap<>();

    private RequestProcessor.Task task;
    private final Map<StudentId, VCardStudent> students = new HashMap<>();
    private WebServiceProvider service;
    private final Key key;

    private VCardStudents(Key k) {
        this.key = k;
    }

    public static VCardStudents get(final LocalProperties properties) throws IOException {
        final String studentsUrl = URLs.students(properties);
        final String provider = AppProperties.provider(properties);
        return VCardStudents.get(provider, studentsUrl);
    }

    public static VCardStudents get(String providerUrl, String studentsUrl) throws IOException {
        if (StringUtils.isBlank(providerUrl) || StringUtils.isBlank(studentsUrl)) {
            throw new IOException(new IllegalArgumentException("providerURL and studentsUrl both must not be null."));
        }
        final Key k = new Key(providerUrl, studentsUrl);
        Object s;
        synchronized (INSTANCES) {
            s = INSTANCES.get(k);
            if (s == null) {
                try {
                    s = create(k);
                } catch (IOException ex) {
                    s = ex;
                }
                INSTANCES.put(k, s);
            }
        }
        if (s instanceof IOException) {
            throw (IOException) s;
        }
        return (VCardStudents) s;
    }

    public String getStudentsUrl() {
        return key.studentsUrl;
    }

    public String getProviderUrl() {
        return key.providerUrl;
    }

    @Override
    public Set<VCardStudent> getStudents() {
        if (task != null) {
            if (!task.isFinished() && EventQueue.isDispatchThread()) {
                Logger.getLogger(VCardStudents.class.getName()).log(Level.WARNING, "VCardStudents.getStudents should not be called from AWT while signees are still being loaded.");
            }
            task.waitFinished();
        }
//        return Collections.unmodifiableSet(students.values());
        return ImmutableSet.copyOf(students.values());
    }

    public Task getLoadTask() {
        return task;
    }

    private static VCardStudents create(final Key k) throws IOException {
        WebServiceProvider wsp;
        try {
            wsp = WebProvider.find(k.providerUrl, WebServiceProvider.class);
        } catch (NoProviderException e) {
            throw new IOException(e);
        }
        final VCardStudents ret = new VCardStudents(k);
        ret.service = wsp;
        return reload(ret);
    }

    private static VCardStudents reload(final VCardStudents ret) {
        ret.task = ret.service.getDefaultRequestProcessor().post(() -> {
            try {
                ret.loadStudents();
            } catch (IOException ex) {
                Logger.getLogger(VCardStudents.class.getName()).log(LogLevel.INFO_WARNING, "An error occurred loading VCardStudents from provider ." + ret.service.getInfo().getDisplayName(), ex);
                synchronized (INSTANCES) {
                    INSTANCES.put(ret.key, ex);
                }
            }
        }, 0, Thread.MAX_PRIORITY);
        return ret;
    }

    private void loadStudents() throws IOException {
        final Multistatus ms = MultiStatusSupport.fetchMultistatus(service, key.studentsUrl);
        final List<String> l = ms.getResponses().stream()
                .flatMap(r -> r.getPropstat().stream())
                .filter(ps -> ps.getStatusCode() == HttpStatus.SC_OK)
                .map(ps -> ps.getProp())
                .filter(CardDavProp.class::isInstance)
                .map(CardDavProp.class::cast)
                .map(CardDavProp::getAddressData)
                .map(AddressData::getValue)
                .collect(Collectors.toList());
        final Set<StudentId> keys = new HashSet<>(students.keySet());
        for (String vc : l) {
            try {
                final List<VCard> c = VCardBuilder.parseCards(new StringReader(vc));
                for (VCard card : c) {
                    final StudentId id = VCardStudent.extractStudentId(card);
                    final VCardStudent vs = students.computeIfAbsent(id, VCardStudent::new);
//                    VCardStudent vs = new VCardStudent(id);
//                    if (students.contains(vs)) {
//                        students.remove(vs);
//                    }
                    vs.setVCard(card);
//                    students.add(vs);
                    keys.remove(id);
                }
            } catch (ParseException | InvalidComponentException ex) {
                throw new IOException(ex);
            }
        }
        keys.forEach(students::remove);
    }

    public void forceReload() {
        reload(this);
    }

    private static class Key {

        private final String studentsUrl;
        private final String providerUrl;

        private Key(String providerUrl, String studentsUrl) {
            this.studentsUrl = studentsUrl;
            this.providerUrl = providerUrl;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + Objects.hashCode(this.studentsUrl);
            hash = 83 * hash + Objects.hashCode(this.providerUrl);
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
            if (!Objects.equals(this.studentsUrl, other.studentsUrl)) {
                return false;
            }
            return Objects.equals(this.providerUrl, other.providerUrl);
        }

    }

}
