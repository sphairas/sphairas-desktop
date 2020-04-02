/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.project.BetulaProject;
import org.thespheres.betula.project.BetulaProjectUtil;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NamingResolver;

/**
 *
 * @author boris.heithecker
 */
class DefaultCustomizerPanelModel implements ActionListener {

    static final String SHOW_DISPLAY_NAMES = "showDisplayNames";
    private final Lookup context;
    private boolean showDisplay = true;
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);

    DefaultCustomizerPanelModel(Lookup context) {
        this.context = context;
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

    String getUnitDisplay() {
        LocalProperties prop = context.lookup(LocalFileProperties.class);
        String id = prop.getProperty("unit.id");
        String authority = prop.getProperty("unit.authority");
        if (id != null && authority != null) {
            final UnitId uid = new UnitId(authority, id);
            if (getIsShowDisplayNames()) {
                return findNamingResolver()
                        .map(nr -> {
                            try {
                                return nr.resolveDisplayName(uid);
                            } catch (IllegalAuthorityException ex) {
                                return (String) null;
                            }
                        })
                        .orElse(uid.getId());
            }
            return uid.getId();
        }
        return null;
    }

    private Optional< NamingResolver> findNamingResolver() {
        LocalProperties prop = context.lookup(LocalFileProperties.class);
        String prov = prop.getProperty("providerURL");
        String nprov = prop.getProperty("naming.providerURL", prov);
        return Optional.ofNullable(StringUtils.trimToNull(nprov))
                .map(NamingResolver::find);
    }

    String getTargetDisplay() {
        LocalProperties prop = context.lookup(LocalFileProperties.class);
        String id = prop.getProperty("baseTarget.documentId");
        String authority = prop.getProperty("baseTarget.documentAuthority");
        if (id != null && authority != null) {
            final DocumentId did = new DocumentId(authority, id, DocumentId.Version.LATEST);
            if (getIsShowDisplayNames()) {
                return findNamingResolver()
                        .map(nr -> {
                            try {
                                return nr.resolveDisplayName(did);
                            } catch (IllegalAuthorityException ex) {
                                return (String) null;
                            }
                        })
                        .orElse(did.getId());
            }
            return did.getId();
        }
        return null;
    }

    AssessmentConvention getAssessmentConvention() {
        LocalFileProperties prop = context.lookup(LocalFileProperties.class);
        String p = prop.getProperty("preferredConvention");
        if (p != null) {
            return GradeFactory.findConvention(p);
        }
        return null;
    }

    void setSelectedConvention(AssessmentConvention ac) {
        BetulaProject bp = context.lookup(BetulaProject.class);
        String prop = "preferredConvention=" + (ac != null ? ac.getName() : "");
        try {
            BetulaProjectUtil.updateLocalProperties(bp, prop);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    boolean getIsShowDisplayNames() {
        return showDisplay;
    }

    void setShowDisplayNames(boolean show) {
        boolean old = showDisplay;
        showDisplay = show;
        pSupport.firePropertyChange(SHOW_DISPLAY_NAMES, old, showDisplay);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

}
