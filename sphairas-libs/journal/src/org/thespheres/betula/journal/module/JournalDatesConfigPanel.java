/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.module;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.ui.ConfigurationPanelComponent;
import org.thespheres.betula.ui.ConfigurationPanelComponentProvider;

/**
 *
 * @author boris.heithecker
 */
@Messages({"JournalDatesConfigPanel.name=Zeitraum"})
public class JournalDatesConfigPanel extends ConfigurationPanelComponent implements ActionListener {

    private EditableJournal current;
    private final DatesConfigPanel configPanel;

    @SuppressWarnings({"LeakingThisInConstructor"})
    JournalDatesConfigPanel(DatesConfigPanel component) {
        super(component);
        configPanel = component;
    }

    @Override
    public void panelDeactivated() {
        configPanel.startDatePicker.removeActionListener(this);
        configPanel.endDatePicker.removeActionListener(this);
        current = null;
    }

    @Override
    public void panelActivated(Lookup context) {
        current = context.lookup(EditableJournal.class);
        if (current != null) {
            Date sd = null;
            final LocalDate js = current.getJournalStart();
            if (js != null) {
                sd = Date.from(js.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            }
            configPanel.startDatePicker.setDate(sd);
            final LocalDate je = current.getJournalEnd();
            Date ed = null;
            if (je != null) {
                ed = Date.from(je.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            }
            configPanel.endDatePicker.setDate(ed);
        }
        configPanel.startDatePicker.addActionListener(this);
        configPanel.endDatePicker.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (current != null) {
            if (e.getSource() == configPanel.startDatePicker) {
                final Date d = configPanel.startDatePicker.getDate();
                final Instant instant = Instant.ofEpochMilli(d.getTime());
                LocalDate res = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
                current.setJournalStart(res);
            } else if (e.getSource() == configPanel.endDatePicker) {
                final Date d = configPanel.endDatePicker.getDate();
                final Instant instant = Instant.ofEpochMilli(d.getTime());
                LocalDate res = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
                current.setJournalEnd(res);
            }
        }
    }

    /**
     *
     * @author boris.heithecker
     */
    @MimeRegistration(mimeType = "text/betula-journal-file+xml", position = 1000, service = ConfigurationPanelComponentProvider.class)
    public static class Registration implements ConfigurationPanelComponentProvider {

        @Override
        public ConfigurationPanelComponent createConfigurationPanelComponent() {
            final DatesConfigPanel panel = new DatesConfigPanel();
            final String n = NbBundle.getMessage(JournalDatesConfigPanel.class, "JournalDatesConfigPanel.name");
            panel.setName(n);
            return new JournalDatesConfigPanel(panel);
        }
    }

}
