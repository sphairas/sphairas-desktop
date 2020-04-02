/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.NamingResolver;

/**
 *
 * @author boris.heithecker
 */
public class UnitTarget {

    private final UnitId unit;
    private final DocumentId targetBase;
    private final Set<DocumentId> targets = new HashSet<>();
    private final String provider;
    private final String namingProvider;
    private String displayName;

    public UnitTarget(UnitId unit, DocumentId base, String provider, String namingProv) {
        this.unit = unit;
        this.targetBase = base;
        this.provider = provider;
        this.namingProvider = namingProv;
    }

    public String getProvider() {
        return provider;
    }

    public UnitId getUnitId() {
        return unit;
    }

    public DocumentId getTargetBase() {
        return targetBase;
    }

    public Set<DocumentId> getTargetDocuments() {
        return targets;
    }

    public String getDisplayName() {
        if (displayName == null) {
            try {
                final NamingResolver nr = NamingResolver.find(namingProvider);
                String dn = nr.resolveDisplayNameResult(getTargetBase()).getResolvedName();
                if (!getTargetBase().getId().equals(getUnitId().getId())) {
                    dn = dn + " (" + nr.resolveDisplayNameResult(getUnitId()).getResolvedName() + ")";
                }
                displayName = dn;
            } catch (NoProviderException | IllegalAuthorityException e) {
                displayName = getTargetBase().getId();
            }
        }
        return displayName;
    }

    public static DocumentId parseTargetBase(LocalProperties prop) {
        String tdaauthority = prop.getProperty("baseTarget.documentAuthority");
        String tdid = prop.getProperty("baseTarget.documentId");
        if (tdaauthority != null && tdid != null) {
            return new DocumentId(tdaauthority, tdid, DocumentId.Version.LATEST);
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.unit);
        hash = 29 * hash + Objects.hashCode(this.targetBase);
        return 29 * hash + Objects.hashCode(this.provider);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UnitTarget other = (UnitTarget) obj;
        if (!Objects.equals(this.unit, other.unit)) {
            return false;
        }
        if (!Objects.equals(this.targetBase, other.targetBase)) {
            return false;
        }
        return Objects.equals(this.provider, other.provider);
    }

}
