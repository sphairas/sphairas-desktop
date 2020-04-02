/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.Box.Filler;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.painter.MattePainter;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.Actions;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.termreport.NumberAssessmentProvider.ProviderReference;
import org.thespheres.betula.termreport.model.XmlNumberAssessmentProvider;
import org.thespheres.betula.termreport.model.XmlNumberAssessmentProvider.XmlProviderReference;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.thespheres.betula.termreport.ui//NumberAssessmentConfiguration//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "NumberAssessmentConfigurationTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "org.thespheres.betula.termreport.ui.NumberAssessmentConfigurationTopComponent")
@ActionReference(path = "Menu/Window/betula-project-local-windows", position = 2300, separatorBefore = 2000)
@TopComponent.OpenActionRegistration(
        displayName = "#NumberAssessmentConfigurationTopComponent.action.displayName",
        preferredID = "NumberAssessmentConfigurationTopComponent"
)
@Messages({"NumberAssessmentConfigurationTopComponent.displayName=Gewichtungen",
    "NumberAssessmentConfigurationTopComponent.current.displayName=Gewichtungen ({0})",
    "NumberAssessmentConfigurationTopComponent.action.displayName=Gewichtung (Zensurenbögen)",
    "NumberAssessmentConfigurationTopComponent.sumLbl.text=Summe: {0}%"})
public final class NumberAssessmentConfigurationTopComponent extends TopComponent implements PropertyChangeListener {

    private final NumberAssessmentConfigurationModel model;
    final List<SpinnerPanel> panels = new ArrayList<>();
    private XmlNumberAssessmentProvider current;
    private final NumberFormat sumFormat = NumberFormat.getInstance(Locale.GERMANY);

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public NumberAssessmentConfigurationTopComponent() {
        setName(NbBundle.getMessage(NumberAssessmentConfigurationTopComponent.class, "NumberAssessmentConfigurationTopComponent.displayName"));
        initComponents();
        sumFormat.setMaximumFractionDigits(2);
        Actions.connect(distributeButton, Actions.forID("Betula", "org.thespheres.betula.termreport.action.DistributeWeightsAction"));
        createPanels(10);
        model = new NumberAssessmentConfigurationModel(this);
    }

    void createPanels(int num) {
        int i = 0;
        while (i++ < num) {
            final SpinnerPanel p = new SpinnerPanel();
            panels.add(p);
            configuratorsPanel.add(p);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        distributeButton = new javax.swing.JButton();
        sumTf = new org.jdesktop.swingx.JXFormattedTextField();
        centerPanel = new javax.swing.JPanel();
        configuratorsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        distributeButton.setFocusable(false);
        distributeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        distributeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(distributeButton);

        sumTf.setEditable(false);
        sumTf.setColumns(12);
        toolbar.add(sumTf);

        add(toolbar, java.awt.BorderLayout.NORTH);

        configuratorsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        configuratorsPanel.setLayout(new javax.swing.BoxLayout(configuratorsPanel, javax.swing.BoxLayout.LINE_AXIS));
        centerPanel.add(configuratorsPanel);

        add(centerPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JPanel configuratorsPanel;
    private javax.swing.JButton distributeButton;
    private org.jdesktop.swingx.JXFormattedTextField sumTf;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        TopComponent.getRegistry().addPropertyChangeListener(model);
        model.result.addLookupListener(model);
        model.resultChanged(null);
    }

    @Override
    public void componentClosed() {
        TopComponent.getRegistry().removePropertyChangeListener(model);
        if (model.current[0] != null) {
            model.current[0].removePropertyChangeListener(model);
        }
        model.result.removeLookupListener(model);
    }

    void updateWidths() {
        updateUI();
        panels.stream()
                .filter(sp -> sp.spinner.isVisible())
                .map(sp -> sp.getContentContainer())
                .max(Comparator.comparing(Container::getWidth))
                .ifPresent(ms -> {
//                    final Dimension size = ms.getSize();
//                    panels.stream()
//                    .filter(sp -> sp.spinner.isVisible())
//                    .forEach(s -> s.getContentContainer().setPreferredSize(size));
                });
        updateUI();
    }

    void updateContext(final DataObject currentCtx, final XmlNumberAssessmentProvider np) {
        if (currentCtx == null) {
            setName(NbBundle.getMessage(NumberAssessmentConfigurationTopComponent.class, "NumberAssessmentConfigurationTopComponent.displayName"));
        } else {
            String n = UIUtilities.findDisplayName(currentCtx);
            setName(NbBundle.getMessage(NumberAssessmentConfigurationTopComponent.class, "NumberAssessmentConfigurationTopComponent.current.displayName", n));
        }
        if (current != null) {
            current.getProviderReferences()
                    .forEach(pr -> pr.removePropertyChangeListener(this));
        }
        current = np;
        if (current != null) {
            current.getProviderReferences()
                    .forEach(pr -> pr.addPropertyChangeListener(this));
            updateSum();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProviderReference.PROP_WEIGHT.equals(evt.getPropertyName())) {
            updateSum();
        }
    }

    private void updateSum() {
        final double sum = current.getProviderReferences().stream()
                .map(ProviderReference.class::cast)
                .collect(Collectors.summarizingDouble(ProviderReference::getWeight))
                .getSum();
        String v = sumFormat.format(sum * 100.0);
        sumTf.setText(NbBundle.getMessage(NumberAssessmentConfigurationTopComponent.class, "NumberAssessmentConfigurationTopComponent.sumLbl.text", v));
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    static class SpinnerPanel extends JXTitledPanel implements ChangeListener, PropertyChangeListener {

        private final JSpinner spinner;
//        private final NumberFormatter formatter = new NumberFormatter(NumberFormat.getNumberInstance(Locale.GERMANY));
//        private final JFormattedTextField numberField = new JFormattedTextField(formatter);
        private final NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
//        private final JTextField infoField = new JTextField();
        private final SpinnerNumberModel spinnerModel;
        private XmlProviderReference current;
        private boolean updating;

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        private SpinnerPanel() {
            nf.setGroupingUsed(false);
//            getContentContainer().setLayout(new java.awt.BorderLayout());
            final MattePainter p = new MattePainter(UIManager.getColor("Label.background"));
            setTitlePainter(p);
            final Filler filler = new Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(32767, 9));
            spinner = new javax.swing.JSpinner();
            final VerticalLayout layout = new VerticalLayout();
            layout.setGap(5);
            getContentContainer().setLayout(layout);
            getContentContainer().add(filler);
            setPreferredSize(new Dimension(120, 150));
            spinnerModel = new SpinnerNumberModel(0d, 0d, 1.0d, 0.05d) {
                @Override
                public void setValue(Object value) {
                    if (value instanceof Double) {
                        super.setValue((Double) value);
                    }
                }

            };
//            spinnerModel.setStepSize(0.05d);
//            spinnerModel.setMinimum(0d);
//            spinnerModel.setMaximum(1d);
            spinner.setModel(spinnerModel);
            spinner.setPreferredSize(new java.awt.Dimension(52, 26));
//            infoField.setEditable(false);
            getContentContainer().add(spinner);
//            numberField.setText("1000");
//            getContentContainer().add(numberField);
//            getContentContainer().add(infoField);
            final JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner.getEditor();
            editor.getTextField().setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(nf) {

                @Override
                public Object stringToValue(String text) throws ParseException {
                    if (!StringUtils.endsWith(text, "%")) {
                        text += "%";
                    }
                    return super.stringToValue(text);
                }

            }));
            setVisible(false);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (current != null) {
                updating = true;
                current.setWeight(spinnerModel.getNumber().doubleValue());
                updating = false;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ProviderReference.PROP_WEIGHT.equals(evt.getPropertyName()) && !updating) {
                updateValue();
            }
        }

        private void updateValue() {
            final Double w = (Double) current.getWeight();
            spinnerModel.setValue(w);
        }

        void initialize(XmlProviderReference ref) {
            if (!EventQueue.isDispatchThread()) {
                throw new IllegalStateException("NumberAssessmentConfigurationTopComponent.initialized must be called from EDT only.");
            }
            if (current != null) {
                uninitialize();
            }
            current = ref;
            setTitle(current.getReferenced().getDisplayName());
            updateValue();
            setVisible(true);
            spinnerModel.addChangeListener(this);
            current.addPropertyChangeListener(this);
        }

        void uninitialize() {
            if (!EventQueue.isDispatchThread()) {
                throw new IllegalStateException("NumberAssessmentConfigurationTopComponent.uninitialized must be called from EDT only.");
            }
            spinnerModel.removeChangeListener(this);
            if (current != null) {
                current.removePropertyChangeListener(this);
            }
            current = null;
            setVisible(false);
        }

    }
}
