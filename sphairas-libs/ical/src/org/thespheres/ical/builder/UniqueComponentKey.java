/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.builder;

import java.util.Date;
import java.util.Objects;
import org.thespheres.ical.UID;

/**
 *
 * @author boris.heithecker
 */
public final class UniqueComponentKey {

    private final UID uid;
    private final Date recurrenceId;

    public UniqueComponentKey(UID uid, Date recurrenceId) {
        this.uid = uid;
        this.recurrenceId = recurrenceId;
    }

    public UID getUid() {
        return uid;
    }

    public Date getRecurrenceId() {
        return recurrenceId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.uid);
        hash = 89 * hash + Objects.hashCode(this.recurrenceId);
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
        final UniqueComponentKey other = (UniqueComponentKey) obj;
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        return Objects.equals(this.recurrenceId, other.recurrenceId);
    }

}
