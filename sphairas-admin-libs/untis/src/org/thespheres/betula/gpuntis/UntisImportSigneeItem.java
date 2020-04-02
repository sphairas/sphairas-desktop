/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis;

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
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.*;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.model.ImportSigneeItem;

/**
 *
 * @author boris.heithecker
 */
public class UntisImportSigneeItem extends ImportSigneeItem implements Comparable<ImportSigneeItem> {

    private final Signee signee;
    private final UntisImportConfiguration configuration;
    private final boolean remote;
    private Set<Marker> markers;
    private String nameOverride;
    private final ChangeSet<UntisImportSigneeItem> selectedSet;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public UntisImportSigneeItem(Signee sig, String name, UntisImportConfiguration config, final ChangeSet<UntisImportSigneeItem> set, boolean remote) {
        super(name);
        this.signee = sig;
        this.configuration = config;
        this.remote = remote;
        this.selectedSet = set;
        updateSelected();
    }

    private void updateSelected() {
        try {
            final boolean shouldUpdate = !isForeignSuffix() && (!remote || isUserName());
            setSelected(shouldUpdate);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(UntisImportSigneeItem.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public Signee getSignee() {
        return signee;
    }

    @Override
    public String getName() {
        return nameOverride == null ? super.getName() : nameOverride;
    }

    public void setUserName(String value) {
        if (!Objects.equals(nameOverride, getName())) {
            nameOverride = value;
            updateSelected();
        } else {
            nameOverride = null;
        }
    }

    private String getDBName() {
        return Signees.get(configuration.getWebServiceProvider().getInfo().getURL())
                .map(s -> s.getSignee(signee, false))
                .get();
    }

    public boolean isUserName() {
        return isRemote() ? !getName().equals(getDBName()) : true;
    }

    public boolean isRemote() {
        return remote;
    }

    public final boolean isForeignSuffix() {
        return !getSignee().getSuffix().equals(configuration.getDefaultSigneeSuffix());
    }

    @Override
    public Marker[] getMarkers() {
        return Stream.concat(markers == null ? Stream.empty() : markers.stream(), status == null ? Stream.empty() : Stream.of(status))
                .toArray(Marker[]::new);
    }

    public void setMarkers(Marker[] markers) {
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
        } else {
            throw new IllegalArgumentException("Status must be either StatusMarker or null");
        }
    }

    @Override
    public void setSelected(boolean selected) throws PropertyVetoException {
        super.setSelected(selected);
        boolean sel = isSelected();
        if (sel) {
            selectedSet.add(this);
        } else {
            selectedSet.remove(this);
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
        hash = 59 * hash + Objects.hashCode(this.signee);
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
        final UntisImportSigneeItem other = (UntisImportSigneeItem) obj;
        return Objects.equals(this.signee, other.signee);
    }

}
