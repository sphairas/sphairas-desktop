/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Students;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;

/**
 *
 * @author boris.heithecker
 */
public class RemoteStudents extends Students<RemoteStudent> {

    private static final HashMap<String, RemoteStudents> MAP = new HashMap<>();
    public static final String PROP_PRIMARY_UNIT = "primary-unit";
    private final String provider;
    private final HashMap<StudentId, RemoteStudent> map = new HashMap<>();
    final EventBus events = new EventBus();
    final VCardStudents students;

    private RemoteStudents(String remote) {
        this.provider = remote;
        try {
            this.students = VCardStudents.get(LocalProperties.find(provider));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static RemoteStudents get(final String provider) {
        synchronized (MAP) {
            return MAP.computeIfAbsent(provider, rl -> new RemoteStudents(provider));
        }
    }

    public static RemoteStudent find(final String provider, final StudentId sid) {
        synchronized (MAP) {
            return MAP.computeIfAbsent(provider, rl -> new RemoteStudents(provider)).find(sid);
        }
    }

//    @Deprecated
//    public static RemoteStudents get(final RemoteLookup remote) {
//        final String key = remote.getProviderInfo().getURL();
//        synchronized (MAP) {
//            return MAP.computeIfAbsent(key, rl -> new RemoteStudents(key));
//        }
//    }
//
//    @Deprecated
//    public static RemoteStudent find(final RemoteLookup remote, final StudentId sid) {
//        final String key = remote.getProviderInfo().getURL();
//        synchronized (MAP) {
//            return MAP.computeIfAbsent(key, rl -> new RemoteStudents(key)).find(sid);
//        }
//    }
    public static RemoteStudent[] find(String search) {
        synchronized (MAP) {
            return MAP.entrySet().stream().flatMap((Map.Entry<String, RemoteStudents> e) -> e.getValue().getStudents().stream()).filter(rs -> rs.matches(search) != 0).toArray(RemoteStudent[]::new);
//            return studentsMap.entrySet().stream().flatMap((Map.Entry<String, HashMap<StudentId, RemoteStudent>> e) -> e.getValue().entrySet().stream()).filter((Map.Entry<StudentId, RemoteStudent> e2) -> e2.getValue().matches(search) != 0).map(Map.Entry<StudentId, RemoteStudent>::getValue).toArray(RemoteStudent[]::new);
        }
    }

    @Override
    public Set<RemoteStudent> getStudents() {
        synchronized (map) {
            return map.values().stream().collect(Collectors.toSet());
        }
    }

    @Override
    public RemoteStudent find(StudentId sid) {
        synchronized (map) {
            return map.computeIfAbsent(sid, s -> new RemoteStudent(provider, s, this, events));
        }
    }

    public EventBus getEventBus() {
        return events;
    }

}
