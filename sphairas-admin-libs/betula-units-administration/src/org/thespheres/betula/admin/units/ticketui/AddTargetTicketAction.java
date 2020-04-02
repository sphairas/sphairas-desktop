/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ticketui;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.TermId;
import org.thespheres.betula.Ticket;
import org.thespheres.betula.admin.units.AbstractRemoteTargetsAction;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.ticketui.CreateTicketComponentVisualPanel.CreateTicketComponentWizardPanel2;
import org.thespheres.betula.admin.units.ticketui.CreateTicketComponentVisualPanel.TicketEntry2;
import org.thespheres.betula.admin.units.ticketui.CreateTicketVisualPanel2.CreateTicketPanel2;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.GenericXmlTicket;
import org.thespheres.betula.document.util.TicketEntry;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ui.util.CalendarHttpUtilities;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;
import org.thespheres.ical.UID;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.ical.builder.ICalendarBuilder.CalendarComponentBuilder;
import org.thespheres.ical.util.IComponentUtilities;

@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.admin.units.ticketui.AddTargetTicketAction")
@ActionRegistration(
        displayName = "#AddTargetTicketAction.action.name",
        lazy = false,
        surviveFocusChange = true,
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-target-assessment-document/Actions", position = 800)})
@NbBundle.Messages({
    "AddTargetTicketAction.action.name=Neue Berechtigung für {0}",
    "AddTargetTicketAction.action.disabledName=Neue Berechtigung",
    "AddTargetTicketAction.action.multipleTargets.scope={0} Listen"})
public final class AddTargetTicketAction extends AbstractRemoteTargetsAction {

    public AddTargetTicketAction() {
    }

    private AddTargetTicketAction(Lookup context) {
        super(context, false);
        updateEnabled();
    }

    @Override
    protected AbstractRemoteTargetsAction createAbstractRemoteTargetsAction(Lookup context) {
        return new AddTargetTicketAction(context);
    }

    @Override
    public String getName() {
        String name;
        try {
            term = findCommonTerm();
            name = NbBundle.getMessage(AddTargetTicketAction.class, "AddTargetTicketAction.action.name", term.getDisplayName());
        } catch (IOException ex) {
            setEnabled(false);
            name = NbBundle.getMessage(AddTargetTicketAction.class, "AddTargetTicketAction.action.disabledName");
        }
        return name;
    }

    @Override
    public void actionPerformed(final List<RemoteTargetAssessmentDocument> l, Optional<AbstractUnitOpenSupport> support) {
        final Term t = term;
        if (t == null) {
            //TODO notify
            return;
        }
        support.ifPresent(uos -> {
            final RemoteTicketModel2 rm;
            try {
                rm = RemoteTicketModel2.get(uos.findWebServiceProvider().getInfo().getURL());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            try {
                processSelection(uos, l, t, rm);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    private static void processSelection(final AbstractUnitOpenSupport uos, final List<RemoteTargetAssessmentDocument> l, final Term tm, final RemoteTicketModel2 rm) throws IOException {
        final WebServiceProvider ws = uos.findWebServiceProvider();
        WizardDescriptor wiz = new WizardDescriptor(new CreateTicketWizardIterator2());
        wiz.putProperty(Iterators.PROP_UNITOPENSUPPORT, uos);
        wiz.putProperty(Iterators.PROP_REMOTETICKETMODEL, rm);
        wiz.putProperty(Iterators.PROP_DELETE_INTERVAL_ENABLED, Boolean.TRUE);
        wiz.putProperty(Iterators.PROP_STUDENTID_ENABLED, Boolean.TRUE);
        wiz.setTitleFormat(new MessageFormat(NbBundle.getMessage(Iterators.class, "TicketWizardAction.step")));
        wiz.setTitle(NbBundle.getMessage(Iterators.class, "TicketWizardAction.title"));
        final String scopeName;
        if (l.size() == 1) {
            scopeName = l.get(0).getName().getDisplayName(tm);
        } else {
            scopeName = NbBundle.getMessage(AddUnitTicketAction.class, "AddTargetTicketAction.action.multipleTargets.scope", l.size());
        }
        wiz.putProperty(Iterators.PROP_SCOPE_MESSAGE, scopeName);
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final String[] signeeType = (String[]) wiz.getProperty(Iterators.PROP_SIGNEETYPES);
            final boolean addToEvent = (boolean) wiz.getProperty(Iterators.PROP_ADDTOEVENT);
            final TicketEntry2 event = (TicketEntry2) wiz.getProperty(Iterators.PROP_ADDTOEVENTCOMPONENT);
            final String summary = (String) wiz.getProperty(Iterators.PROP_NEWSUMMARY);
            final LocalDateTime date = (LocalDateTime) wiz.getProperty(Iterators.PROP_NEWDATETIME);
            final String categories = (String) wiz.getProperty(Iterators.PROP_NEWCAT);
            final Integer priority = (Integer) wiz.getProperty(Iterators.PROP_NEWPRIO);

            final Set<DocumentId> docs = l.stream()
                    .map(RemoteTargetAssessmentDocument::getDocumentId)
                    .collect(Collectors.toSet());

            final ICalendarBuilder cb = new ICalendarBuilder();
            final CalendarComponentBuilder ccb;
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
            rm.rp.post(() -> {
                try {
                    run(ws, rm.getTicketsUrl(), docs, tm, signeeType, ccb, cb);
                } catch (IOException ex) {
                    RemoteTicketModel2.notifyError(ex, ex.getLocalizedMessage());
                }
            });
        }
    }

    private static void run(final WebServiceProvider ws, final String http, final Set<DocumentId> docs, final Term tm, final String[] signeeType, final CalendarComponentBuilder ccb, final ICalendarBuilder cb) throws IOException {
        final Map<DocumentId, Ticket> m = postCreateTicket(ws, docs, tm.getScheduledItemId(), signeeType);
        for (Ticket t : m.values()) {
            try {
                ccb.addProperty("X-TICKET", Long.toString(t.getId()), new Parameter("x-ticket-authority", t.getAuthority()));
            } catch (InvalidComponentException ex) {
                throw new IOException(ex);
            }
        }
        CalendarHttpUtilities.putCalendar(ws, http, cb.toICalendar());
    }

    static Map<DocumentId, Ticket> postCreateTicket(final WebServiceProvider service, final Set<DocumentId> targets, TermId term, String[] signeeType) throws IOException {
        ContainerBuilder builder = new ContainerBuilder();
        for (DocumentId target : targets) {
            final TicketEntry te = new TicketEntry(Action.FILE, null, "target-document-ticket", "1.0");
            final List<GenericXmlTicket.XmlTicketScope> scope = te.getValue().getScope();
            scope.add(new GenericXmlTicket.XmlTicketScope("target", target, "include"));
            if (term != null) {
                scope.add(new GenericXmlTicket.XmlTicketScope("term", term, "include"));
            }
            if (signeeType != null) {
                Arrays.stream(signeeType).forEach(e -> scope.add(new GenericXmlTicket.XmlTicketScope("entitlement", e, "include")));
            }
//            final StudentId student = tgt.getStudent();
//            if (student != null) {
//                scope.add(new GenericXmlTicket.XmlTicketScope("student", student, "include"));
//            }
            builder.add(te, Paths.TICKETS_PATH);
        }
        Container ret = null;
        try {
            ret = service.createServicePort().solicit(builder.getContainer());
        } catch (ServiceException ex) {
            throw new IOException(ex);
        }
        if (ret != null) {
            final List<Envelope> l = DocumentUtilities.findEnvelope(ret, Paths.TICKETS_PATH);
            final Map<Ticket, TicketEntry> entries = l.stream()
                    .filter(TicketEntry.class::isInstance)
                    .map(TicketEntry.class::cast)
                    .filter(TicketActionUtil::checkException)
                    .filter(e -> Action.CONFIRM.equals(e.getAction()))
                    .collect(Collectors.toMap(TicketEntry::getIdentity, Function.identity()));
            return targets.stream()
                    .collect(Collectors.toMap(Function.identity(), did -> {
                        return entries.entrySet().stream()
                                .filter(e -> e.getValue().getValue().getScope().stream().anyMatch(s -> "target".equals(s.getScope()) && did.equals(s.getValue())))
                                .findAny()
                                .map(Map.Entry::getKey)
                                .orElse(null);
                    }));
        }
        return null;
    }

    static final class CreateTicketWizardIterator2 extends WizardDescriptor.ArrayIterator<WizardDescriptor> {

        @Override
        protected WizardDescriptor.Panel<WizardDescriptor>[] initializePanels() {
            List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
            panels.add(new CreateTicketPanel2());
            panels.add(new CreateTicketComponentWizardPanel2());
            String[] steps = new String[panels.size()];
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
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
            return panels.toArray(new WizardDescriptor.Panel[panels.size()]);
        }

    }
}
