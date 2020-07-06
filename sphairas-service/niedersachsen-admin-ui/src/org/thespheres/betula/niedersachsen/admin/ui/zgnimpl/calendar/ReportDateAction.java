/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.niedersachsen.zeugnis.Constants;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.ui.util.MultiContextSensitiveAction;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris
 */
class ReportDateAction extends MultiContextSensitiveAction {

    private final boolean abschlussZeugnis;

    ReportDateAction(final boolean abschlussZeugnis) {
        this.abschlussZeugnis = abschlussZeugnis;
    }

    @Override
    protected String getName(final boolean enabled) {
        final Collection<? extends ReportData2> rs = currentContext.lookupAll(ReportData2.class);
        if (enabled) {
            final int s = rs.size();
            switch (s) {
                case 0:
                    return abschlussZeugnis ? NbBundle.getMessage(ReportDateActions.class, "ReportDateActions.display.context.unitTermAbschluss") : NbBundle.getMessage(ReportDateActions.class, "ReportDateActions.display.context.unitTerm");
                case 1:
                    return NbBundle.getMessage(ReportDateActions.class, "ReportDateActions.display.context.student", rs.iterator().next().getRemoteStudent().getDisplayName());
                default:
                    return NbBundle.getMessage(ReportDateActions.class, "ReportDateActions.display.context.multiple", Integer.toString(s));
            }
        }
        return NbBundle.getMessage(ReportDateActions.class, "ReportDateActions.display");
    }

    @Override
    public void actionPerformed(final ActionEvent e, final Lookup context) {
        final Collection<? extends PrimaryUnitOpenSupport> puos = context.lookupAll(PrimaryUnitOpenSupport.class);
        final RemoteReportsModel2 rum = context.lookup(RemoteReportsModel2.class);
        final Collection<? extends ReportData2> studentReport = context.lookupAll(ReportData2.class);
        final List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new SetDateVisualPanel.SetDateWizardPanel());
        final String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            final Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) {
                // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        final WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
//        wiz.putProperty(SetZeugniskonfDateAction.PROP_UNITOPENSUPPORT, uos);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(SetZeugniskonfDateAction.class, "ReportDateActions.title"));
        wiz.putProperty(SetZeugniskonfDateAction.PROP_NEWSUMMARY, "Zeugnisausgabe");
        wiz.putProperty(SetZeugniskonfDateAction.PROP_DATEONLY, true);
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final String summary = (String) wiz.getProperty(SetZeugniskonfDateAction.PROP_NEWSUMMARY);
            final LocalDateTime date = (LocalDateTime) wiz.getProperty(SetZeugniskonfDateAction.PROP_NEWDATETIME);
            if (rum != null && !studentReport.isEmpty()) {
                final PrimaryUnitOpenSupport uos = rum.getPrimaryUnitOpenSupport();
                final Map<TermId, ? extends List<? extends ReportData2>> g = studentReport.stream().collect(Collectors.groupingBy(sr -> sr.getTerm()));
                for (final Map.Entry<TermId, ? extends List<? extends ReportData2>> entry : g.entrySet()) {
                    putCalendarData(uos, entry.getKey(), summary, date, entry.getValue());
                }
            } else {
                for (final PrimaryUnitOpenSupport uos : puos) {
                    final LocalDate today = Lookup.getDefault().lookup(WorkingDate.class).getCurrentWorkingLocalDate();
                    try {
                        final TermId term = uos.findTermSchedule().termOf(today).getScheduledItemId();
                        putCalendarData(uos, term, summary, date, null);
                    } catch (final IOException | IllegalStateException ex) {
                        PlatformUtil.getCodeNameBaseLogger(ReportDateAction.class).log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            }

        }
    }

    private void putCalendarData(final PrimaryUnitOpenSupport uos, final TermId term, final String summary, final LocalDateTime date, final Collection<? extends ReportData2> studentReport) throws IllegalStateException {
        final String dtStart = IComponentUtilities.DATETIME_FORMATTER.format(date);
        final ICalendarBuilder cb = new ICalendarBuilder();
        try {
            cb.addProperty(CalendarComponent.PRODID, SetZeugniskonfDateAction.CAL_PRODID).addProperty(CalendarComponent.VERSION, "2.0");
            final ICalendarBuilder.CalendarComponentBuilder builder = cb.addComponent(CalendarComponent.VEVENT, null).addProperty(CalendarComponentProperty.DTSTART, dtStart).addProperty(CalendarComponentProperty.SUMMARY, summary).addProperty(CalendarComponentProperty.CATEGORIES, Constants.CATEGORY_ZEUGNISAUSGABE).addProperty("X-UNIT", uos.getUnitId().getId(), new Parameter("x-authority", uos.getUnitId().getAuthority())).addProperty("X-TERM", Integer.toString(term.getId()), new Parameter("x-authority", term.getAuthority()));
            if (abschlussZeugnis) {
                builder.addProperty(CalendarComponentProperty.CATEGORIES, Constants.ABSCHLUSSZEUGNISSE);
            }
            if (studentReport != null) {
                for (final ReportData2 r : studentReport) {
                    final DocumentId rid = r.getDocumentId();
                    builder.addProperty("X-DOCUMENT", rid.getId(), new Parameter("x-authority", rid.getAuthority()), new Parameter("x-version", rid.getVersion().getVersion()));
                }
            }
        } catch (final InvalidComponentException icex) {
            PlatformUtil.getCodeNameBaseLogger(SetZeugniskonfDateAction.class).log(Level.SEVERE, icex.getMessage(), icex);
            return;
        }
        final ZeugnisCalendarLookup2 clkp = uos.getLookup().lookup(ZeugnisCalendarLookup2.class);
        if (clkp == null) {
            throw new IllegalStateException("No ZeugnisCalendarLookup2.");
        }
        clkp.createZeugnisEvent(cb);
    }

}
