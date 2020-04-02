/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.timetbl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.AsyncGUIJob;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.icalendar.CalendarBuilderProvider;
import org.thespheres.betula.project.local.unit.LocalUnit;
import org.thespheres.betula.ui.util.LogLevel;

/**
 *
 * @author boris.heithecker
 */
@Messages({"TimetablePanel.Loader.load.error=Der Studenplan konnte nicht geladen werden.",
    "TimetablePanel.message.invalidClassSchedule=Die Stundentafel ist ungültig."})
class TimetablePanel extends javax.swing.JPanel {

    private TimetablePanelModel model;
    private final TimetablePanelColumnFactory colFactory = new TimetablePanelColumnFactory();
    private final URI configPath;
    private final ProjectCustomizer.Category category;
    private final Loader loader;
    private static JAXBContext jaxb;

    @SuppressWarnings({"LeakingThisInConstructor"})
    TimetablePanel(URI config, UnitId unit, ProjectCustomizer.Category category) {
        this.configPath = config;
        this.category = category;
        this.loader = new Loader(unit);
        this.category.setStoreListener(loader);
        initComponents();
        Utilities.attachInitJob(this, loader);
    }

    static JAXBContext getJAXBContext() {
        synchronized (LocalUnit.class) {
            if (jaxb == null) {
                try {
                    jaxb = JAXBContext.newInstance(LocalClassSchedule.class, LocalTimetable.class);
                } catch (JAXBException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return jaxb;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bottomPanel = new javax.swing.JPanel();
        startTextField = new javax.swing.JFormattedTextField();
        startLabel = new javax.swing.JLabel();
        endLabel = new javax.swing.JLabel();
        endTextField = new javax.swing.JFormattedTextField();
        scrollPane = new javax.swing.JScrollPane();
        table = new org.jdesktop.swingx.JXTable();

        setLayout(new java.awt.BorderLayout());

        startTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("d.M.yy"))));

        org.openide.awt.Mnemonics.setLocalizedText(startLabel, org.openide.util.NbBundle.getMessage(TimetablePanel.class, "TimetablePanel.startLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(endLabel, org.openide.util.NbBundle.getMessage(TimetablePanel.class, "TimetablePanel.endLabel.text")); // NOI18N

        endTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("d.M.yy"))));

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startLabel)
                    .addComponent(endLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(endTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                    .addComponent(startTextField))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startLabel)
                    .addComponent(startTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(bottomPanel, java.awt.BorderLayout.PAGE_END);

        table.setColumnFactory(colFactory);
        scrollPane.setViewportView(table);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JLabel endLabel;
    private javax.swing.JFormattedTextField endTextField;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel startLabel;
    private javax.swing.JFormattedTextField startTextField;
    private org.jdesktop.swingx.JXTable table;
    // End of variables declaration//GEN-END:variables

    class Loader implements AsyncGUIJob, ActionListener, ChangeListener {

        private LocalClassScheduleHolder lcs;
        private LocalTimetable lt;
        private final UnitId unit;
        private final ArrayList<Exception> exceptions = new ArrayList<>(2);

        private Loader(UnitId unit) {
            this.unit = unit;
        }

        @Override
        public void construct() {
            lcs = LocalClassScheduleHolder.get(configPath);
            Path plt = Paths.get(configPath).resolve(LocalTimetable.TIMETABLE_FILE);
            if (!Files.exists(plt)) {
                lt = new LocalTimetable();
            }
            if (lt == null) {
                JAXBContext ctx = getJAXBContext();
                try {
                    lt = (LocalTimetable) ctx.createUnmarshaller().unmarshal(Files.newInputStream(plt));
                } catch (JAXBException | IOException ex) {
                    exceptions.add(ex);
                }
            }
        }

        @Override
        public void finished() {
            if (lt != null) {
                final LocalDate ldrs = lt.getStart();
                final Date drs = ldrs == null ? null : Date.from(ldrs.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                startTextField.setValue(drs);
                final LocalDate ldre = lt.getEnd();
                final Date dre = ldre == null ? null : Date.from(ldre.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                endTextField.setValue(dre);
                try {
                    model = new TimetablePanelModel(lcs.getLocalClassSchedule(), lt);
                    table.setModel(model);
                    model.addChangeListener(this);
                } catch (IOException ioex) {
                    exceptions.add(ioex);
                }
            }
            if (!exceptions.isEmpty()) {
                final String msg = NbBundle.getMessage(TimetablePanel.class, "TimetablePanel.Loader.load.error");
                category.setErrorMessage(msg);
            } else {
                category.setErrorMessage(null);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            category.setValid(model.isValid());
            if (!model.isValid()) {
                final String msg = NbBundle.getMessage(TimetablePanel.class, "TimetablePanel.message.invalidClassSchedule");
                category.setErrorMessage(msg);
            } else {
                category.setErrorMessage(null);
            }
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (lt != null) {
                final Date dvs = (Date) startTextField.getValue();
                final LocalDate lds = dvs == null ? null : LocalDate.from(dvs.toInstant().atZone(ZoneId.systemDefault()));
                lt.setStart(lds);
                final Date dve = (Date) endTextField.getValue();
                final LocalDate lde = dve == null ? null : LocalDate.from(dve.toInstant().atZone(ZoneId.systemDefault()));
                lt.setEnd(lde);
            }
            JAXBContext ctx = getJAXBContext();
            if (lcs != null) {
                Path plcs = Paths.get(configPath).resolve(LocalClassSchedule.CLASS_SCHEDULE_FILE);
                Path backuplcs = null;
                if (Files.exists(plcs)) {
                    Path ba = plcs.resolveSibling(LocalClassSchedule.CLASS_SCHEDULE_FILE + ".bak");
                    try {
                        backuplcs = Files.copy(plcs, ba, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        Logger.getLogger(LocalUnit.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
                    }
                }
                try (OutputStream os = Files.newOutputStream(plcs)) {
                    final Marshaller m = ctx.createMarshaller();
                    m.setProperty("jaxb.formatted.output", Boolean.TRUE);
                    m.marshal(lcs.getLocalClassSchedule(), os);
                    if (backuplcs != null) {
                        try {
                            Files.deleteIfExists(backuplcs);
                        } catch (IOException ex) {
                            Logger.getLogger(LocalUnit.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
                        }
                    }
                } catch (IOException | JAXBException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    lcs.invalidate();
                }
            }
            if (lt != null) {
                Path plt = Paths.get(configPath).resolve(LocalTimetable.TIMETABLE_FILE);
                Path backupllt = null;
                if (Files.exists(plt)) {
                    Path ba = plt.resolveSibling(LocalTimetable.TIMETABLE_FILE + ".bak");
                    try {
                        backupllt = Files.copy(plt, ba, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        Logger.getLogger(LocalUnit.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
                    }
                }
                try (OutputStream os = Files.newOutputStream(plt)) {
                    final Marshaller m = ctx.createMarshaller();
                    m.setProperty("jaxb.formatted.output", Boolean.TRUE);
                    m.marshal(lt, os);
                    if (backupllt != null) {
                        try {
                            Files.deleteIfExists(backupllt);
                        } catch (IOException ex) {
                            Logger.getLogger(LocalUnit.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
                        }
                    }
                } catch (IOException | JAXBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            Path lics = Paths.get(configPath).resolve("local.ics");
            if (Files.exists(lics) && unit != null) {
                try {
                    final FileObject fo = URLMapper.findFileObject(lics.toUri().toURL());
                    if (fo != null) {
                        final DataObject ical = DataObject.find(fo);
                        if (ical != null) {
                            final CalendarBuilderProvider cb = ical.getLookup().lookup(CalendarBuilderProvider.class);
                            if (cb != null && !cb.getCalendarBuilders().isEmpty()) {
                                lt.toCalendar(lcs.getLocalClassSchedule(), unit, null, cb.getCalendarBuilders().get(0));
                                cb.setModified();
                            }
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
