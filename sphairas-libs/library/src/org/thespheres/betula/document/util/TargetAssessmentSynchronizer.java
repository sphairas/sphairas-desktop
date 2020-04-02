/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.util.Objects;
import java.util.function.BiConsumer;
import org.thespheres.betula.Identity;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.IdentityTargetAssessment;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <TA>
 */
public class TargetAssessmentSynchronizer<I extends Identity, TA extends IdentityTargetAssessment<Grade, I, ?>> {

    protected final BiConsumer<LogKey<I>, LogValue> consumer;

    public TargetAssessmentSynchronizer() {
        this(null);
    }

    public TargetAssessmentSynchronizer(BiConsumer<LogKey<I>, LogValue> consumer) {
        this.consumer = consumer;
    }

    public TA synchronizeTargets(TA local, TA remote) {
        if (local == null) {
            return remote;
        }
        updateLocal(remote, local);
        return local;
    }

    protected void updateLocal(TA remote, TA local) {
        remote.students().stream()
                .forEach(sid -> {
                    remote.identities().stream()
                            .forEach(tid -> updateOneLocal(remote, local, sid, tid));
                });
    }

    protected void updateOneLocal(TA remote, TA local, StudentId sid, I tid) {
        final Grade remoteGrade = remote != null ? remote.select(sid, tid) : null;
        final Timestamp remoteTime = remote != null ? remote.timestamp(sid, tid) : null;
        if (remoteGrade != null || remoteTime != null) {
            final Grade localGrade = local.select(sid, tid);
            final Timestamp localTime = local.timestamp(sid, tid);
            final boolean overrideLocal = localTime == null || (remoteTime != null && remoteTime.getValue().after(localTime.getValue()));
            if (!(Objects.equals(remoteGrade, localGrade) && Objects.equals(remoteTime, localTime))) {
                if (overrideLocal) {
                    local.submit(sid, tid, remoteGrade, remoteTime);
                }
                if (consumer != null) {
                    final LogKey key = new LogKey(tid, sid);
                    final LogValue log = new LogValue(localGrade, remoteGrade, overrideLocal);
                    consumer.accept(key, log);
                }
            }
        }
    }

    public static class LogKey<I extends Identity> {

        private final I identity;
        private final StudentId student;

        public LogKey(I term, StudentId sid) {
            this.identity = term;
            this.student = sid;
        }

        public I getIdentity() {
            return identity;
        }

        public StudentId getStudent() {
            return student;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + Objects.hashCode(this.identity);
            hash = 53 * hash + Objects.hashCode(this.student);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LogKey<?> other = (LogKey<?>) obj;
            if (!Objects.equals(this.identity, other.identity)) {
                return false;
            }
            return Objects.equals(this.student, other.student);
        }

    }

    public static class LogValue {

        private final Grade localValue;
        private final Grade remoteValue;
        private final boolean localOverridden;

        public LogValue(Grade localValue, Grade remoteValue, boolean localOverridden) {
            this.localValue = localValue;
            this.remoteValue = remoteValue;
            this.localOverridden = localOverridden;
        }

        public Grade getLocalValue() {
            return localValue;
        }

        public Grade getRemoteValue() {
            return remoteValue;
        }

        public boolean isLocalOverridden() {
            return localOverridden;
        }

    }

}
