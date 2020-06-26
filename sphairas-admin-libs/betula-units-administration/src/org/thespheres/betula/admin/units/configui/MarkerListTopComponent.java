/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.configui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.AbstractListModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.model.MarkerDecoration;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
//@ConvertAsProperties(
//        dtd = "-//org.thespheres.betula.admin.units.configui//MarkerList//EN",
//        autostore = false
//)
@TopComponent.Description(
        preferredID = "MarkerListTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "markers", openAtStartup = false)
@ActionID(category = "Window", id = "org.thespheres.betula.admin.units.configui.MarkerListTopComponent")
@ActionReference(path = "Menu/Window/betula-beans-services-windows" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#MarkerListTopComponent.action.displayName",
        preferredID = "MarkerListTopComponent")
@NbBundle.Messages({
    "MarkerListTopComponent.action.displayName=Listenmarkierungen",
    "MarkerListTopComponent.displayName=Listenmarkierungen",
    "MarkerListTopComponent.contextDisplayName=Listenmarkierungen - {0}",
    "MarkerListTopComponent.numDisplayName=Listenmarkierungen - {0} Listen",
    "MarkerListTopComponent.toolTip=Dies ist das Fenster zur Ansicht der Listenmarkierungen."})
public class MarkerListTopComponent extends TopComponent {

    private final List<ListPanel> panels = new ArrayList<>(10);
    private final Listener listener = new Listener();
    private RemoteTargetAssessmentDocument[] current;
    private ListPanel selectedPanel;

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public MarkerListTopComponent() {
        setLayout(new org.jdesktop.swingx.VerticalLayout());
        addPanels(5);
        setName(NbBundle.getMessage(MarkerListTopComponent.class, "MarkerListTopComponent.displayName"));
        setToolTipText(NbBundle.getMessage(MarkerListTopComponent.class, "MarkerListTopComponent.toolTip"));
    }

    private void addPanels(int num) {
        for (int i = 0; i < num; i++) {
            final ListPanel p = new ListPanel();
            panels.add(p);
            add(p);
        }
    }

    @Override
    protected void componentHidden() {
        TopComponent.getRegistry().removePropertyChangeListener(listener);
        onChange();

    }

    @Override
    protected void componentShowing() {
        onChange();
        TopComponent.getRegistry().addPropertyChangeListener(listener);
    }

    private synchronized void onChange() {
        final RemoteTargetAssessmentDocument[] arr = Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
                .flatMap(n -> n.getLookup().lookupAll(RemoteTargetAssessmentDocument.class).stream())
                .toArray(RemoteTargetAssessmentDocument[]::new);
//        MarkerSetProvider selected = arr.length == 1 ? arr[0] : null;
        EventQueue.invokeLater(() -> setCurrentList(arr));
    }

    private void setCurrentList(final RemoteTargetAssessmentDocument[] selected) {
        if (current != null) {
            Arrays.stream(current)
                    .forEach(rtad -> rtad.removePropertyChangeListener(listener));
        }
        current = selected;
        updatePanels();
        Arrays.stream(current)
                .forEach(rtad -> rtad.addPropertyChangeListener(listener));
        if (current != null && current.length != 0) {
            final String dn = Arrays.stream(current)
                    .map(rtad -> rtad.getName().getDisplayName(null))
                    .distinct()
                    .collect(CollectionUtil.singleOrNull());
            if (dn != null) {
                setName(NbBundle.getMessage(MarkerListTopComponent.class, "MarkerListTopComponent.contextDisplayName", dn));
            } else {
                setName(NbBundle.getMessage(MarkerListTopComponent.class, "MarkerListTopComponent.numDisplayName", current.length));
            }
        } else {
            setName(NbBundle.getMessage(MarkerListTopComponent.class, "MarkerListTopComponent.displayName"));
        }
    }

    private void updatePanels() {
        final Map<String, Set<Marker>> sets;
        if (current == null || current.length == 0) {
            sets = Collections.EMPTY_MAP;
        } else {
            final Set<Marker> all = Arrays.stream(current)
                    .flatMap(p -> Arrays.stream(p.markers()))
                    .collect(Collectors.toSet());
            Arrays.stream(current)
                    .map(p -> Arrays.asList(p.markers()))
                    .forEach(all::retainAll);
            sets = all.stream()
                    .filter(m -> !Marker.isNull(m))
                    .collect(Collectors.groupingBy(Marker::getConvention, Collectors.toSet()));
        }
        final List<MarkerConvention> conventions = sets.keySet().stream()
                .map(MarkerFactory::findConvention)
                .sorted(Comparator.comparing(MarkerConvention::getDisplayName, Collator.getInstance(Locale.GERMANY)))
                .collect(Collectors.toList());
        if (conventions.size() > panels.size()) {
            addPanels(conventions.size() - panels.size());
        }
        for (int i = 0; i < panels.size(); i++) {
            ListPanel p = panels.get(i);
            if (i < conventions.size()) {
                final MarkerConvention c = conventions.get(i);
                p.initialize(c, sets.get(c.getName()));
            } else {
                p.reset();
            }
        }
    }

    private void updateSelection(ListPanel m) {
        if (selectedPanel != m) {
            if (selectedPanel != null) {
                selectedPanel.clearSelection();
            }
            selectedPanel = m;
        }
//        boolean ena = current.length != 0;
//        ena = ena && selectedPanel.list.getSelectedIndices().length != 0;
//        deleteMarkerAction.setEnabled(ena);

    }
//    void writeProperties(java.util.Properties p) {
//        // better to version settings since initial version as advocated at
//        // http://wiki.apidesign.org/wiki/PropertyFiles
//        p.setProperty("version", "1.0");
//        // TODO store your settings
//    }
//
//    void readProperties(java.util.Properties p) {
//        String version = p.getProperty("version");
//        // TODO read your settings according to their version
//    }

    private final class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case TopComponent.Registry.PROP_ACTIVATED_NODES:
                    onChange();
                    break;
                case TopComponent.Registry.PROP_TC_CLOSED:
                    final TopComponent closed = (TopComponent) evt.getNewValue();
                    if (closed != null && current != null) {
                        boolean changed = closed.getLookup().lookupAll(MarkerDecoration.class).stream()
                                .anyMatch(msp -> Arrays.stream(current).anyMatch(c -> c == msp));
                        if (changed) {
                            setCurrentList(null);
                        }
                    }
                    break;
                case RemoteTargetAssessmentDocument.PROP_MARKERS:
                    EventQueue.invokeLater(() -> updatePanels());
                    break;
            }
        }

    }
//
//    @Messages("AddMarkerAction.shortDescription=Markierung hinzufügen")
//    private class AddMarkerAction extends AbstractAction {
//
//        @SuppressWarnings("OverridableMethodCallInConstructor")
//        public AddMarkerAction() {
//            super(null, ImageUtilities.loadImageIcon("org/thespheres/betula/admin/units/resources/plus-button.png", true));
//            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AddMarkerAction.class, "AddMarkerAction.shortDescription"));
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//
//        }
//
//    }
//
//    @Messages("DeleteMarkerAction.shortDescription=Ausgewählte Markierung löschen")
//    private class DeleteMarkerAction extends AbstractAction {
//
//        @SuppressWarnings("OverridableMethodCallInConstructor")
//        public DeleteMarkerAction() {
//            super(null, ImageUtilities.loadImageIcon("org/thespheres/betula/admin/units/resources/cross-button.png", true));
//            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AddMarkerAction.class, "DeleteMarkerAction.shortDescription"));
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            Arrays.stream(selectedPanel.list.getSelectedValues())
//                    .map(Marker.class::cast)
//                    .forEach((m) -> {
//                        Arrays.stream(current).forEach(msp -> {
//                            try {
////                                msp.removeMarker(m);
//                            } catch (Exception ex) {
//                            }
//                        });
//                    });
//
//        }
//
//    }

    private final class ListPanel extends JXTitledPanel implements ListSelectionListener {

        private final ConventionModel model = new ConventionModel();
        private final JXList list = new JXList();

        @SuppressWarnings(value = {"OverridableMethodCallInConstructor",
            "LeakingThisInConstructor"})
        private ListPanel() {
            getContentContainer().setLayout(new java.awt.BorderLayout());
            final MattePainter p = new MattePainter(UIManager.getColor("Label.background"));
            setTitlePainter(p);
            list.setModel(model);
            list.getSelectionModel().addListSelectionListener(this);
            list.setCellRenderer(new DefaultListRenderer(o -> ((Marker) o).getLongLabel()));
            getContentContainer().add(list, java.awt.BorderLayout.CENTER);
        }

        private void initialize(MarkerConvention mc, Set<Marker> set) {
            setTitle(mc.getDisplayName());
            setVisible(true);
            model.initialize(set);
        }

        private void reset() {
            setTitle(null);
            setVisible(false);
            model.initialize(Collections.EMPTY_SET);
        }

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            if (evt.getValueIsAdjusting()) {
                return;
            }
            final int sel[] = list.getSelectedIndices();
            if (sel.length != 0) {
                MarkerListTopComponent.this.updateSelection(this);
            }
        }

        private void clearSelection() {
            list.setSelectedIndices(new int[0]);
        }

    }

    private final class ConventionModel extends AbstractListModel {

        private Marker[] markers;
        private MarkerConvention mc;

        private void initialize(Set<Marker> m) {
            int old = 0;
            if (this.markers != null) {
                old = this.markers.length;
            }
            markers = m.stream()
                    .sorted(Comparator.comparing(Marker::getLongLabel, Collator.getInstance(Locale.GERMANY)))
                    .toArray(Marker[]::new);
            fireContentsChanged(this, 0, Math.max(old, this.markers.length));
        }

        @Override
        public int getSize() {
            return markers != null ? markers.length : 0;
        }

        @Override
        public Object getElementAt(int index) {
            return markers[index];
        }

    }
}
