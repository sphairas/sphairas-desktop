/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.util.UnitInfo;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
public class AdminUnit implements Unit, ChangeListener {

    static final int DELAY = 2000;
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    private final UnitId unit;
    private final String provider;
    private String displayName = null;
    private final RequestProcessor.Task task;
    private UnitInfo ui;

    public AdminUnit(final String provider, final UnitId u) {
        this.provider = provider;
        this.unit = u;
        task = AdminUnits.get(provider).getRequestProcessor().post(this::reload);
    }

    public String getProvider() {
        return provider;
    }

    private void reload() {
        LocalDateTime asOf = null;
        VCardStudent v;
        ui = Units.get(provider).map(u -> {
            try {
                return u.fetchParticipants(unit, asOf);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(AdminUnit.class).log(Level.SEVERE, "An error has occurred loading unit " + unit.getId() + " from service " + provider, ex);
                task.schedule(DELAY);
                return null;
            }
        }).orElse(null);
    }

    @Override
    public UnitId getUnitId() {
        return unit;
    }

    @Override
    public Set<Student> getStudents() {
        task.waitFinished();
        class RStudent implements Student {

            private final StudentId id;

            RStudent(StudentId id) {
                this.id = id;
            }

            @Override
            public String getDirectoryName() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getGivenNames() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getSurname() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getFullName() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public StudentId getStudentId() {
                return id;
            }

        }
        return ui == null ? Collections.EMPTY_SET : Arrays.stream(ui.getStudents())
                .map(s -> new RStudent(s))
                .collect(Collectors.toSet());
    }

    @Override
    public Student findStudent(StudentId id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDisplayName() {
        if (displayName == null) {
            Lookup.getDefault().lookup(WorkingDate.class).addChangeListener(this);
            stateChanged(null);
        }
        return displayName;
    }

    private void setDisplayName(final String value) {
        String old = displayName;
        if (!value.equals(old)) {
            displayName = value;
            pSupport.firePropertyChange(Unit.PROP_DISPLAYNAME, old, value);
        }
    }

    public Marker[] getMarkers() {
        if (ui != null) {
            try {
                return ui.getResponseUnitEntry().getValue().getMarkerSet().stream()
                        .toArray(Marker[]::new);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public boolean isPrimaryUnit() {
        task.waitFinished();
        return Arrays.stream(getMarkers())
                .anyMatch(ServiceConstants.BETULA_PRIMARY_UNIT_MARKER::equals);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        final LocalProperties lp = LocalProperties.find(provider);
        final TermSchedule schedule = Optional.ofNullable(lp)
                .map(p -> p.getProperty("termSchedule.providerURL"))
                .map(tp -> SchemeProvider.find(tp))
                .map(t -> t.getScheme(lp.getProperty("termSchemeId", TermSchedule.DEFAULT_SCHEME), TermSchedule.class))
                .orElse(null);
        final NamingResolver np = Optional.ofNullable(lp)
                .map(p -> p.getProperty("naming.providerURL", provider))
                .map(tp -> NamingResolver.find(tp))
                .orElse(null);
        String dName = null;
        final LocalDate workingDate = Lookup.getDefault().lookup(WorkingDate.class).getCurrentWorkingLocalDate();
        if (schedule != null && np != null) {
            final Term currentTerm = schedule.termOf(workingDate);
            try {
                dName = np.resolveDisplayName(unit, currentTerm);
            } catch (IllegalAuthorityException ex) {
            }
        }
        if (dName == null) {
            dName = unit.getId();
        }
        setDisplayName(dName);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pSupport.removePropertyChangeListener(l);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.unit);
        return 79 * hash + Objects.hashCode(this.provider);
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
        final AdminUnit other = (AdminUnit) obj;
        if (!Objects.equals(this.provider, other.provider)) {
            return false;
        }
        return Objects.equals(this.unit, other.unit);
    }

}
