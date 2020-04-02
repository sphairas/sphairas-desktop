/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ticketui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.Ticket;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.GenericXmlTicket;
import org.thespheres.betula.document.util.TicketEntry;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ui.util.CalendarHttpUtilities;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;
import org.thespheres.ical.UID;
import org.thespheres.ical.util.IComponentUtilities;

// An example action demonstrating how the wizard could be called from within
// your code. You can move the code below wherever you need, or register an action:
@ActionID(category = "Betula", id = "org.thespheres.betula.admin.units.ticketui.AddStudentsTicketAction")
@ActionRegistration(displayName = "#CTL_CreateStudentsTicketWizardAction", iconBase = "org/thespheres/betula/admin/units/resources/calendar--plus.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-students/Actions", position = 5500, separatorBefore = 5000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({"CTL_CreateStudentsTicketWizardAction=Neue Berechtigung",
    "CreateStudentsTicketWizardAction.step=Schritt {0}",
    "CreateStudentsTicketWizardAction.title=Berechtigung (S/S') erstellen"})
public final class AddStudentsTicketAction implements ActionListener {

    private final List<RemoteStudent> context;
    private final RemoteUnitsModel unitsModel;
    private final WebServiceProvider ws;
    private final RemoteTicketModel2 model;
    private final TermSchedule ts;

    public AddStudentsTicketAction(final List<RemoteStudent> ctx) throws IOException {
        context = ctx;
        unitsModel = Utilities.actionsGlobalContext().lookup(RemoteUnitsModel.class);
        if (unitsModel == null) {
            throw new IOException("No RemoteUnitsModel in Utilities.actionsGlobalContext()");
        }
        ws = unitsModel.getUnitOpenSupport().findWebServiceProvider();
        model = RemoteTicketModel2.get(unitsModel.getUnitOpenSupport().findWebServiceProvider().getInfo().getURL());
        ts = unitsModel.getUnitOpenSupport().findTermSchedule();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new CreateTicketVisualPanel2.CreateTicketPanel2());
        panels.add(new CreateTicketComponentVisualPanel.CreateTicketComponentWizardPanel2());
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
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat(NbBundle.getMessage(AddStudentsTicketAction.class, "CreateStudentsTicketWizardAction.step")));
        wiz.setTitle(NbBundle.getMessage(AddStudentsTicketAction.class, "CreateStudentsTicketWizardAction.title"));
        wiz.putProperty(Iterators.PROP_UNITOPENSUPPORT, unitsModel.getUnitOpenSupport());
        wiz.putProperty(Iterators.PROP_REMOTETICKETMODEL, model);

        wiz.putProperty(Iterators.PROP_DELETE_INTERVAL_ENABLED, Boolean.FALSE);
        wiz.putProperty(Iterators.PROP_STUDENTID_ENABLED, Boolean.FALSE);
        final String scopeName = context.stream()
                .map(s -> s.getFullName())
                .collect(Collectors.joining(", "));
        wiz.putProperty(Iterators.PROP_SCOPE_MESSAGE, scopeName);

        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final Date wd = Lookup.getDefault().lookup(WorkingDate.class).getCurrentWorkingDate();
            final Term term = ts.getTerm(wd);
            final boolean addToEvent = (boolean) wiz.getProperty(Iterators.PROP_ADDTOEVENT);
            final CreateTicketComponentVisualPanel.TicketEntry2 event = (CreateTicketComponentVisualPanel.TicketEntry2) wiz.getProperty(Iterators.PROP_ADDTOEVENTCOMPONENT);
            final String summary = (String) wiz.getProperty(Iterators.PROP_NEWSUMMARY);
            final LocalDateTime date = (LocalDateTime) wiz.getProperty(Iterators.PROP_NEWDATETIME);
            final String categories = (String) wiz.getProperty(Iterators.PROP_NEWCAT);
            final Integer priority = (Integer) wiz.getProperty(Iterators.PROP_NEWPRIO);
            final String[] signeeType = (String[]) wiz.getProperty(Iterators.PROP_SIGNEETYPES);

            final ICalendarBuilder cb = new ICalendarBuilder();
            final ICalendarBuilder.CalendarComponentBuilder ccb;
            try {
                cb.addProperty("METHOD", "UPDATE");
                final UID uid = addToEvent ? event.uid : null;
                ccb = cb.addComponent(CalendarComponent.VEVENT, uid);
                if (!addToEvent) {
                    ccb.addProperty(CalendarComponentProperty.DTSTART, IComponentUtilities.DATETIME_FORMATTER.format(date))
                            .addProperty(CalendarComponentProperty.SUMMARY, summary)
                            .addProperty(CalendarComponentProperty.CATEGORIES, categories);
                    //add ticket-ids
                    if (priority != null) {
                        ccb.addProperty(CalendarComponentProperty.PRIORITY, Integer.toString(priority));
                    }
                }
            } catch (InvalidComponentException icex) {
                Logger.getLogger(AddStudentsTicketAction.class.getName()).log(LogLevel.INFO_WARNING, icex.getMessage(), icex);
                return;
            }

//                UID[] added = ticketsBean.put(cb.toICalendar());
//                uid = added[0];
//                
//                
//                StudentsTicketBean ticketBean = remoteLookup.lookup(StudentsTicketBean.class);
//                if (ticketBean == null) {
//                    throw new IllegalStateException("No UnitTicketsBean.");
//                }
//                StudentId[] studs = entry.getValue().stream().map(RemoteStudent::getStudentId).toArray(StudentId[]::new);
//                Ticket ret = ticketBean.createTicket(studs, term.getScheduledItemId(), signeeType);
//                ticketsBean.addTicket(uid, ret);
            model.rp.post(() -> {
                try {
                    run(term.getScheduledItemId(), signeeType, ccb, cb);
                } catch (IOException ex) {
                    RemoteTicketModel2.notifyError(ex, ex.getLocalizedMessage());
                }
            });
        }

    }

    private void run(final TermId term, final String[] signeeType, final ICalendarBuilder.CalendarComponentBuilder ccb, final ICalendarBuilder cb) throws IOException {
        final Ticket m = postCreateTicket(term, signeeType);
        try {
            ccb.addProperty("X-TICKET", Long.toString(m.getId()), new Parameter("x-ticket-authority", m.getAuthority()));
        } catch (InvalidComponentException ex) {
            throw new IOException(ex);
        }
        CalendarHttpUtilities.putCalendar(ws, model.getTicketsUrl(), cb.toICalendar());
    }

    private Ticket postCreateTicket(final TermId term, String[] signeeType) throws IOException {
        final Set<StudentId> students = context.stream()
                .map(RemoteStudent::getStudentId)
                .collect(Collectors.toSet());
        final ContainerBuilder builder = new ContainerBuilder();
        final TicketEntry te = new TicketEntry(Action.FILE, null, "student-ticket", "1.0");
        final List<GenericXmlTicket.XmlTicketScope> scope = te.getValue().getScope();
        if (term != null) {
            scope.add(new GenericXmlTicket.XmlTicketScope("term", term, "include"));
        }
        if (signeeType != null) {
            Arrays.stream(signeeType).forEach(e -> scope.add(new GenericXmlTicket.XmlTicketScope("entitlement", e, "include")));
        }
        students.forEach(s -> scope.add(new GenericXmlTicket.XmlTicketScope("student", s, "include")));
        builder.add(te, Paths.TICKETS_PATH);
        Container ret = null;
        try {
            ret = ws.createServicePort().solicit(builder.getContainer());
        } catch (ServiceException ex) {
            throw new IOException(ex);
        }
        if (ret != null) {
            final List<Envelope> l = DocumentUtilities.findEnvelope(ret, Paths.TICKETS_PATH);
            final TicketEntry re = l.stream()
                    .filter(TicketEntry.class::isInstance)
                    .map(TicketEntry.class::cast)
                    .filter(TicketActionUtil::checkException)
                    .filter(e -> Action.CONFIRM.equals(e.getAction()))
                    .collect(CollectionUtil.singleOrNull());
            boolean allMatched = re != null && students.stream()
                    .allMatch(sid -> {
                        return re.getValue().getScope().stream()
                                .filter(s -> "student".equals(s.getScope()))
                                .map(s -> s.getValue())
                                .filter(StudentId.class::isInstance)
                                .map(StudentId.class::cast)
                                .anyMatch(sid::equals);
                    });
            if (allMatched) {
                return re.getIdentity();
            }
        }
        throw new IOException("Invalid or no return");
    }
}
