/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar.SetDateVisualPanel.SetDateWizardPanel;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;
import org.thespheres.ical.util.IComponentUtilities;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar.SetZeugniskonfDateAction")
@ActionRegistration(displayName = "#SetZeugniskonfDateAction")
@ActionReferences({
    @ActionReference(path = "Menu/units-administration", position = 2000, separatorBefore = 1750),
    @ActionReference(path = "Loaders/application/betula-unit-data/Actions", position = 6000)
})
@Messages({"SetZeugniskonfDateAction=Termin der Zeugniskonferenz setzen",
    "SetZeugniskonfDateAction.title=Termin der Zeugniskonferenz"})
public final class SetZeugniskonfDateAction implements ActionListener {

    public static final String CAL_PRODID = "thespheres.org";
    public static final String PROP_NEWSUMMARY = "new.summary";
    public static final String PROP_NEWDATETIME = "new.datetime";
    public static final String PROP_DATEONLY = "dateonly";
    private final List<PrimaryUnitOpenSupport> context;

    public SetZeugniskonfDateAction(final List<PrimaryUnitOpenSupport> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new SetDateWizardPanel());
        final String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            final Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        final WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        final Date wd = Lookup.getDefault().lookup(WorkingDate.class).getCurrentWorkingDate();
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(SetZeugniskonfDateAction.class, "SetZeugniskonfDateAction.title"));
        wiz.putProperty(SetZeugniskonfDateAction.PROP_NEWSUMMARY, "Zeugniskonferenz");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final String summary = (String) wiz.getProperty(PROP_NEWSUMMARY);
            final LocalDateTime date = (LocalDateTime) wiz.getProperty(PROP_NEWDATETIME);

            for (final PrimaryUnitOpenSupport uos : context) {
                final TermId term;
                try {
                    term = uos.findTermSchedule().getTerm(wd).getScheduledItemId();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                final ZeugnisCalendarLookup2 clkp = uos.getLookup().lookup(ZeugnisCalendarLookup2.class);
                if (clkp == null) {
                    throw new IllegalStateException("No ZeugnisCalendarLookup2.");
                }
                putCalendarData(date, summary, term, uos.getUnitId(), clkp);
            }

        }
    }

    private void putCalendarData(final LocalDateTime date, final String summary, final TermId term, final UnitId unit, final ZeugnisCalendarLookup2 clkp) {
        final String dtStart = IComponentUtilities.DATETIME_FORMATTER.format(date);
        final ICalendarBuilder cb = new ICalendarBuilder();
        try {
            cb.addProperty(CalendarComponent.PRODID, CAL_PRODID)
                    .addProperty(CalendarComponent.VERSION, "2.0");
            cb.addComponent(CalendarComponent.VEVENT, null)
                    .addProperty(CalendarComponentProperty.DTSTART, dtStart)
                    .addProperty(CalendarComponentProperty.SUMMARY, summary)
                    .addProperty("X-UNIT", unit.getId(), new Parameter("x-authority", unit.getAuthority()))
                    .addProperty("X-TERM", Integer.toString(term.getId()), new Parameter("x-authority", term.getAuthority()));
        } catch (InvalidComponentException icex) {
            Logger.getLogger(SetZeugniskonfDateAction.class.getName()).log(Level.SEVERE, icex.getMessage(), icex);
            return;
        }
        clkp.createZeugnisEvent(cb);
    }
}
