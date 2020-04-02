/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.*;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.model.ImportSigneeItem;
import org.thespheres.betula.xmlimport.model.XmlSigneeItem;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
public class SigneeXmlCsvItem extends ImportSigneeItem implements Comparable<ImportSigneeItem> {

    private final static Marker STATUS_ACTIVE = MarkerFactory.find(SigneeStatus.NAME, "active", null);
    private final Signee signee;
    private final ConfigurableImportTarget configuration;
    private Set<Marker> markers;
    private String nameOverride;
    private final String name;
    private final XmlSigneeItem source;
    private final String remoteName;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    SigneeXmlCsvItem(final String label, final String name, final Signee sig, final XmlSigneeItem source, final ConfigurableImportTarget config) {
        super(label);
        this.source = source;
        this.name = name;
        this.signee = sig;
        this.configuration = config;
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
    }

    public XmlSigneeItem getSource() {
        return source;
    }

    @Override
    public Signee getSignee() {
        return signee;
    }

    @Override
    public String getName() {
        return nameOverride == null ? name : nameOverride;
    }

    public String getNameFromDatabase() {
        return remoteName;
    }

    public void userSetName(String value) {
        if (!Objects.equals(nameOverride, getName())) {
            nameOverride = value;
            updateSelected();
        } else {
            nameOverride = null;
        }
    }

    public boolean isUserName() {
        return !Objects.equals(name, getNameFromDatabase());
    }

    private void updateSelected() {
        try {
            final boolean shouldUpdate = !isForeignSuffix() && isUserName();
            setSelected(shouldUpdate);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(SigneeXmlCsvItem.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
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
        return !isForeignSuffix() && StringUtils.trimToNull(getName()) != null && getStatus() != null;
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
        final SigneeXmlCsvItem other = (SigneeXmlCsvItem) obj;
        return Objects.equals(this.signee, other.signee);
    }

}
