/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.thespheres.betula.Unit;

/**
 *
 * @author boris.heithecker
 */
final class BetulaProjectInformation implements ProjectInformation {

    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    private final BetulaProject project;
    private Listener listener;
    private String displayName;
    private final ArrayList<Unit> units = new ArrayList<>();

    BetulaProjectInformation(final BetulaProject project) {
        this.project = project;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtilities.loadImage("org/thespheres/betula/project/resources/betulaproject16.png"));
    }

    @Override
    public String getName() {
        return project.properties.getName();
    }

    @Override
    public String getDisplayName() {
        initListener();
        if (displayName != null) {
            return displayName;
        }
        return getName();
    }

    private void initListener() {
        if (listener == null) {
            listener = new Listener(); //not in constructor, because will call Project.getLookup ==> circular
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pSupport.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pSupport.removePropertyChangeListener(pcl);
    }

    @Override
    public Project getProject() {
        return project;
    }

    private final class Listener implements LookupListener, PropertyChangeListener {

        private final Lookup.Result<Unit> unitResult;

        @SuppressWarnings("LeakingThisInConstructor")
        private Listener() {
            unitResult = project.getLookup().lookupResult(Unit.class);
            unitResult.addLookupListener(this);
            resultChanged(null);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            ArrayList<Unit> toRemove = new ArrayList<>();
            synchronized (units) {
                toRemove.addAll(units);
                unitResult.allInstances().stream()
                        .forEach(u -> {
                            if (!units.contains(u)) {
                                units.add(u);
                                u.addPropertyChangeListener(this);
                            } else {
                                toRemove.remove(u);
                            }
                        });
                toRemove.stream()
                        .peek(ru -> ru.removePropertyChangeListener(this))
                        .forEach(units::remove);
            }
            updateDN();
        }

        private void updateDN() {
            final String old = BetulaProjectInformation.this.displayName;
            final String joined;
            synchronized (units) {
                joined = units.stream()
                        .map(Unit::getDisplayName)
                        .sorted(Collator.getInstance(Locale.GERMANY))
                        .collect(Collectors.joining(", "));
            }
            if (!joined.isEmpty()) {
                BetulaProjectInformation.this.displayName = joined;
            } else {
                BetulaProjectInformation.this.displayName = BetulaProjectInformation.this.getName();
            }
            pSupport.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, old, BetulaProjectInformation.this.displayName);

        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(Unit.PROP_DISPLAYNAME)) {
                updateDN();
            }
        }
    }
}
