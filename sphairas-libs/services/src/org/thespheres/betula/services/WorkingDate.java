/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import java.time.LocalDate;
import java.util.Date;
import javax.swing.event.ChangeListener;

/**
 *
 * @author boris.heithecker
 */
public interface WorkingDate {

    @Deprecated
    public Date getCurrentWorkingDate();

    public LocalDate getCurrentWorkingLocalDate();

    public boolean isNow();

    public Updater markUpdating();

    public void addChangeListener(ChangeListener l);

    public void removeChangeListener(ChangeListener l);

    public static abstract class Updater {

        public final long id;

        protected Updater(long id) {
            this.id = id;
        }

        public abstract void unmarkUpdating();

        @Override
        public int hashCode() {
            int hash = 7;
            return 97 * hash + (int) (this.id ^ (this.id >>> 32));
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
            final Updater other = (Updater) obj;
            return this.id == other.id;
        }

    }
}
