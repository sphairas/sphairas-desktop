/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.unitsui;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.util.Util;

/**
 *
 * @author boris.heithecker
 */
class StudentUnits {

    private final static Cache<StudentId, StudentUnits> CACHE2 = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Util.RP_THROUGHPUT)
            .initialCapacity(6000)
            .build();
    private final Set<UnitId> units = new HashSet<>();
    private final RemoteStudent student;

    private StudentUnits(final RemoteStudent stud) {
        this.student = stud;
    }

    public static StudentUnits getUnits(final RemoteStudent stud) {
        try {
            return CACHE2.get(stud.getStudentId(), () -> {
                return new StudentUnits(stud);
            });
        } catch (ExecutionException ex) {
            //Should never happen.
            throw new RuntimeException(ex);
        }
    }

    public Set<UnitId> getUnits() {
        synchronized (units) {
            return units.stream()
                    .collect(Collectors.toSet());
        }
    }

    public RemoteStudent getStudent() {
        return student;
    }

    void update(List<Unit> l) {
        final Set<UnitId> uu = l.stream()
                .filter(u -> u.getStudents().stream().anyMatch(s -> s.getStudentId().equals(student.getStudentId())))
                .map(u -> u.getUnitId())
                .collect(Collectors.toSet());
        synchronized (units) {
            units.addAll(uu);
        }
    }

}
