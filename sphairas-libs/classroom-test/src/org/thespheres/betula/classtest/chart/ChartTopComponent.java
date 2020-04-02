/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.chart;

import com.google.common.eventbus.Subscribe;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.thespheres.betula.assess.GroupingGrades;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.ui.util.UIUtilities;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.CollectionUtil;

@ConvertAsProperties(dtd = "-//org.thespheres.betula.classtest.chart//Chart//EN",
        autostore = false)
@TopComponent.Description(preferredID = "ChartTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "markers", openAtStartup = false)
@ActionID(category = "Window", id = "org.thespheres.betula.classtest.chart.ChartTopComponent")
@ActionReference(path = "Menu/Window/betula-project-local-windows", position = 1800)
@TopComponent.OpenActionRegistration(
        displayName = "#ChartTopComponent.openAction.displayName",
        preferredID = "ChartTopComponent")
@Messages({"ChartTopComponent.openAction.displayName=Statistik (Klassenarbeiten)",
    "ChartTopComponent.displayName.empty=Statistik",
    "ChartTopComponent.displayName=Statistik {0}"})
public final class ChartTopComponent extends TopComponent implements LookupListener, ActionListener, PropertyChangeListener {

    public static final String PROP_SHOW_NORMALDIST = "show.normal.distribution";
    public static final String PROP_GROUP_GRADES = "group.grades";
    private final Lookup.Result<ClassroomTestEditor2> result;
    private final DefaultCategoryDataset data = new DefaultCategoryDataset();
    private final DefaultCategoryDataset nd = new DefaultCategoryDataset();
    private final JFreeChart chart;
    private ClassroomTestEditor2 current;

    @SuppressWarnings({"OverridableMethodCallInConstructor",
        "LeakingThisInConstructor"})
    public ChartTopComponent() {
        chart = ChartUtil.createChart(data, nd);
        initComponents();
        updateName();
        result = Utilities.actionsGlobalContext().lookupResult(ClassroomTestEditor2.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }

    @Override
    public synchronized void resultChanged(LookupEvent ev) {
        final ClassroomTestEditor2 etest = result.allInstances().stream()
                .map(ClassroomTestEditor2.class::cast)
                .collect(CollectionUtil.singleOrNull());
        if (etest != null && !Objects.equals(etest, current)) {
            if (current != null) {
                current.getEditableClassroomTest().getEventBus().unregister(this);
            }
            current = etest;
//            if (current != null) {
            update();
            etest.getEditableClassroomTest().getEventBus().register(this);
//            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName()) && current != null) {
            final boolean close = !TopComponent.getRegistry().getOpened().stream()
                    .flatMap(tc -> tc.getLookup().lookupAll(ClassroomTestEditor2.class).stream())
                    .anyMatch(current::equals);
            if (close) {
                current.getEditableClassroomTest().getEventBus().unregister(this);
                current = null;
                update();
            }
        }
    }

    @Subscribe
    public void onPropertyChange(final PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (evt.getSource() instanceof EditableStudent) {
            if (EditableStudent.PROP_GRADE.equals(name) || EditableStudent.PROP_AUTODISTRIBUTING.equals(name)) {
                update();
            }
        }
    }

    @Subscribe
    public void onCollectionChange(final CollectionChangeEvent evt) {
        final String name = evt.getCollectionName();
        if (evt.getSource() instanceof EditableClassroomTest) {
            if (EditableClassroomTest.COLLECTION_STUDENTS.equals(name)) {
                update();
            }
        }
    }

    private void update() {
        data.clear();
        nd.clear();
        Mutex.EVENT.writeAccess(() -> {
            String message = null;
            boolean enableGrouping = true;
            if (current != null) {
                final int size = current.getEditableClassroomTest().getEditableStudents().size();
                message = NbBundle.getMessage(ChartUtil.class, "ChartUtil.categoryAxisLabelOverride", Integer.toString(size));
                enableGrouping = current.getAssessmentConvention() instanceof GroupingGrades;
                ChartUtil.populateData(current, data, showNDCheckBox.isSelected() ? nd : null, groupCheckBox.isSelected());
            }
            groupCheckBox.setEnabled(enableGrouping);
//            chart.setTitle(current.getDataObject().getName());
            chart.getCategoryPlot().getDomainAxis().setLabel(message);
            updateName();
        });
    }

    private void updateName() {
        if (current == null) {
            setName(NbBundle.getMessage(ChartTopComponent.class, "ChartTopComponent.displayName.empty"));
        } else {
            String dn = UIUtilities.findDisplayName(current.getContext().lookup(DataObject.class));
            setName(NbBundle.getMessage(ChartTopComponent.class, "ChartTopComponent.displayName", dn));
        }
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlsPanel = new javax.swing.JPanel();
        showNDCheckBox = new javax.swing.JCheckBox();
        groupCheckBox = new javax.swing.JCheckBox();
        panel = new ChartPanel(chart);

        setLayout(new java.awt.BorderLayout());

        controlsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING));

        org.openide.awt.Mnemonics.setLocalizedText(showNDCheckBox, org.openide.util.NbBundle.getMessage(ChartTopComponent.class, "ChartTopComponent.showNDCheckBox.text")); // NOI18N
        controlsPanel.add(showNDCheckBox);

        org.openide.awt.Mnemonics.setLocalizedText(groupCheckBox, org.openide.util.NbBundle.getMessage(ChartTopComponent.class, "ChartTopComponent.groupCheckBox.text")); // NOI18N
        groupCheckBox.setEnabled(false);
        controlsPanel.add(groupCheckBox);

        add(controlsPanel, java.awt.BorderLayout.SOUTH);

        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 390, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 258, Short.MAX_VALUE)
        );

        add(panel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JCheckBox groupCheckBox;
    private javax.swing.JPanel panel;
    private javax.swing.JCheckBox showNDCheckBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentShowing() {
        TopComponent.getRegistry().addPropertyChangeListener(this);
        result.addLookupListener(this);
        groupCheckBox.addActionListener(this);
        showNDCheckBox.addActionListener(this);
        resultChanged(null);
    }

    @Override
    public void componentClosed() {
        TopComponent.getRegistry().removePropertyChangeListener(this);
        result.removeLookupListener(this);
        showNDCheckBox.removeActionListener(this);
        groupCheckBox.removeActionListener(this);
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
        p.setProperty(PROP_SHOW_NORMALDIST, Boolean.toString(showNDCheckBox.isSelected()));
        p.setProperty(PROP_GROUP_GRADES, Boolean.toString(groupCheckBox.isSelected()));
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        String shownd = p.getProperty(PROP_SHOW_NORMALDIST, Boolean.FALSE.toString());
        showNDCheckBox.setSelected(Boolean.valueOf(shownd));
        String gg = p.getProperty(PROP_GROUP_GRADES, Boolean.FALSE.toString());
        groupCheckBox.setSelected(Boolean.valueOf(gg));
    }

}
