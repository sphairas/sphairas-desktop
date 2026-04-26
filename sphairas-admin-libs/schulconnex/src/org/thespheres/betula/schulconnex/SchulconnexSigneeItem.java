/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex;

import de.schulconnex.qs.model.Person;
import de.schulconnex.qs.model.Personenkontext;
import java.beans.PropertyVetoException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.*;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.model.ImportSigneeItem;

/**
 *
 * @author boris.heithecker
 */
public class SchulconnexSigneeItem extends ImportSigneeItem implements Comparable<ImportSigneeItem> {

    private final static Marker STATUS_ACTIVE = MarkerFactory.find(SigneeStatus.NAME, "active", null);
    private Signee signee;
    private final SchulconnexImportConfiguration configuration;
    private Set<Marker> markers;
    private final Person person;
    private final Personenkontext personenkontext;
    private final String remoteName;
    private final String uuid;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    SchulconnexSigneeItem(final String label, final Person person, final Personenkontext personenkontext, final SchulconnexImportConfiguration config) {
        super(label);
        this.uuid = personenkontext.getId();
        this.person = person;
        this.personenkontext = personenkontext;
        this.configuration = config;
        this.signee = createProposedSignee(person, personenkontext, config);
        boolean found = Signees.get(configuration.getWebServiceProvider().getInfo().getURL())
                .map(s -> s.getSigneeSet().contains(signee))
                .get();
        if (found) {
            this.remoteName = Signees.get(configuration.getWebServiceProvider().getInfo().getURL())
                    .map(s -> s.getSignee(signee, false))
                    .orElse(null);
            final Marker[] m = Signees.get(configuration.getWebServiceProvider().getInfo().getURL())
                    .map(s -> s.getMarkers(signee))
                    .orElse(null);
            setMarkers(m);
        } else {
            this.remoteName = null;
        }
        if (getStatus() == null) {
            setStatus(STATUS_ACTIVE);
        }
        updateSelected();
        try {
            //Remove later
            setSelected(false);
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static Signee createProposedSignee(final Person person, final Personenkontext pek, final SchulconnexImportConfiguration config) {
        final String given = normalizeForSignee(person.getName().getVorname());
        final String family = normalizeForSignee(person.getName().getFamilienname());
        final String prefix = String.join(".", given, family);
        return new Signee(prefix, config.getDefaultSigneeSuffix(), true);
    }

    private static String normalizeForSignee(final String value) {
        final String normalized = StringUtils.stripAccents(StringUtils.defaultString(value)).toLowerCase();
        return Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("[^a-z0-9]+", ".")
                .replaceAll("(^\\.|\\.$)", "")
                .replaceAll("\\.{2,}", ".");
    }

    public String getUUID() {
        return uuid;
    }

    public Person getPerson() {
        return person;
    }

    public Personenkontext getPersonenkontext() {
        return personenkontext;
    }

    @Override
    public Signee getSignee() {
        return signee;
    }

    public String getNameFromDatabase() {
        return remoteName;
    }

    private void updateSelected() {
        try {
            final boolean shouldUpdate = !isForeignSuffix();
            setSelected(shouldUpdate);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(SchulconnexSigneeItem.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public final boolean isForeignSuffix() {
        return !getSignee().getSuffix().equals(configuration.getDefaultSigneeSuffix());
    }

    @Override
    public Marker[] getMarkers() {
        return Stream.concat(markers == null ? Stream.empty() : markers.stream(), status == null ? Stream.empty() : Stream.of(status))
                .toArray(Marker[]::new);
    }

    public void setMarkers(final Marker[] markers) {
        if (markers == null || markers.length == 0) {
            this.markers = null;
            this.status = null;
        } else {
            this.markers = Arrays.stream(markers)
                    .filter(m -> !SigneeStatus.NAME.equals(m.getConvention()))
                    .collect(Collectors.toSet());
            try {
                Marker s = Arrays.stream(markers)
                        .filter(m -> SigneeStatus.NAME.equals(m.getConvention()))
                        .collect(CollectionUtil.requireSingleOrNull());
                if (s != null) {
                    setStatus(s);
                }
            } catch (IllegalStateException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public void setStatus(Marker status) {
        if (status == null || status.getConvention().equals(SigneeStatus.NAME)) {
            this.status = status;
            updateSelected();
        } else {
            throw new IllegalArgumentException("Status must be either StatusMarker or null");
        }
    }

    @Override
    public boolean isValid() {
        return !isForeignSuffix() && getStatus() != null;
    }

    public boolean doUpdate() {
        return isSelected() && isValid();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return 59 * hash + Objects.hashCode(this.signee);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchulconnexSigneeItem other = (SchulconnexSigneeItem) obj;
        return Objects.equals(this.getUUID(), other.getUUID());
    }

}
