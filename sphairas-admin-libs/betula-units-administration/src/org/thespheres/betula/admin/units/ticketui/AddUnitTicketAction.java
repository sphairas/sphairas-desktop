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
import java.text.Collator;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.logging.Level;
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
import org.openide.util.NbPreferences;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.Ticket;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.ticketui.SetExemptedStudentsVisualPanel.SetExemptedStudentsWizardPanel;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.GenericXmlTicket;
import org.thespheres.betula.document.util.TicketEntry;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ui.util.CalendarHttpUtilities;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;
import org.thespheres.ical.UID;
import org.thespheres.ical.util.IComponentUtilities;

@ActionID(category = "Betula", id = "org.thespheres.betula.admin.units.ticketui.AddUnitTicketAction")
@ActionRegistration(displayName = "#AddUnitTicketAction.title",
        iconBase = "org/thespheres/betula/admin/units/resources/calendar--plus.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-data/Actions", position = 5500, separatorBefore = 5000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({"AddUnitTicketAction.title=Neue Berechtigung",
    "AddUnitTicketAction.action.multipleTargets.scope={0} Klassen"})
//"AddUnitTicketAction.action.warning.loadingIncomplete=Die Klasse ist nicht vollständig geladen. Einige Listentypen können fehlen."
public final class AddUnitTicketAction implements ActionListener {

    private final List<PrimaryUnitOpenSupport> context;
//    private final SchemeProvider schemeProvider;

    public AddUnitTicketAction(List<PrimaryUnitOpenSupport> uos) throws IOException {
        this.context = uos;
    }

//    @Override
//    public void actionPerformed(ActionEvent e) {
//        CreateTicketWizardIterator iterator = new CreateTicketWizardIterator();
//        WizardDescriptor wiz = new WizardDescriptor(iterator);
//        wiz.putProperty(Iterators.PROP_UNITOPENSUPPORT, context);
//        class PCL implements PropertyChangeListener {
//
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                if (Iterators.PROP_ISUNITTICKET.equals(evt.getPropertyName())) {
//                    boolean isUnitTicket = (boolean) evt.getNewValue();
//                    iterator.setShowExemptedStudentsPanel(isUnitTicket);
//                }
//            }
//
//        }
//        wiz.addPropertyChangeListener(new PCL());
//        wiz.setTitleFormat(new MessageFormat(NbBundle.getMessage(AddUnitTicketAction.class, "AddUnitTicketAction.step")));
//        wiz.setTitle(NbBundle.getMessage(AddUnitTicketAction.class, "AddUnitTicketAction.title"));
//        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
//            PrimaryUnitOpenSupport uos = (PrimaryUnitOpenSupport) wiz.getProperty(Iterators.PROP_UNITOPENSUPPORT);
////            TermId term = schemeProvider.getScheme(TermSchedule.DEFAULT_SCHEME, TermSchedule.class).getCurrentTerm().getScheduledItemId();
//            TermId term = ((Term) wiz.getProperty(Iterators.SELECTED_TERM)).getScheduledItemId();
//            RemoteLookup remoteLookup;
//            try {
//                remoteLookup = uos.findRemoteLookup();
//                // use wiz.getProperty to retrieve previous panel state
//            } catch (IOException ex) {
//                throw new IllegalStateException(ex);
//            }
//            TicketsCalendarBean ticketsBean = remoteLookup.lookup(TicketsCalendarBean.class);
//            if (ticketsBean == null) {
//                throw new IllegalStateException("No DefaultTicketsCalendarBean.");
//            }
//            boolean addToEvent = (boolean) wiz.getProperty(Iterators.PROP_ADDTOEVENT);
//            boolean isUnitTicket = (boolean) wiz.getProperty(Iterators.PROP_ISUNITTICKET);
//            UID uid;
//            if (addToEvent) {
//                TicketEntry event = (TicketEntry) wiz.getProperty(Iterators.PROP_ADDTOEVENTCOMPONENT);
//                uid = event.uid;
//            } else {
//                String summary = (String) wiz.getProperty(Iterators.PROP_NEWSUMMARY);
//                Date date = (Date) wiz.getProperty(Iterators.PROP_NEWDATETIME);
//                String categories = (String) wiz.getProperty(Iterators.PROP_NEWCAT);
//                Integer priority = (Integer) wiz.getProperty(Iterators.PROP_NEWPRIO);
//
//                ICalendarBuilder cb = new ICalendarBuilder();
//                try {
//                    ICalendarBuilder.CalendarComponentBuilder ccb = cb.addComponent(CalendarComponent.VEVENT, null)
//                            .addProperty(CalendarComponentProperty.DTSTART, IComponentUtilities.DATE_TIME.format(date))
//                            .addProperty(CalendarComponentProperty.SUMMARY, summary)
//                            .addProperty(CalendarComponentProperty.CATEGORIES, categories);
//                    if (priority != null) {
//                        ccb.addProperty(CalendarComponentProperty.PRIORITY, Integer.toString(priority));
//                    }
//                } catch (InvalidComponentException icex) {
//                    Logger.getLogger(AddStudentsTicketAction.class.getName()).log(LogLevel.INFO_WARNING, icex.getMessage(), icex);
//                    return;
//                }
//                UID[] added = ticketsBean.put(cb.toICalendar());
//                uid = added[0];
//            }
//            String signeeType = (String) wiz.getProperty(Iterators.PROP_SIGNEETYPE);
//            Ticket ret;
//            if (isUnitTicket) {
//                UnitTicketBean ticketBean = remoteLookup.lookup(UnitTicketBean.class);
//                if (ticketBean == null) {
//                    throw new IllegalStateException("No UnitTicketsBean.");
//                }
//                UnitId unit = (UnitId) wiz.getProperty(Iterators.PROP_UNITID);
//                StudentId[] list = (StudentId[]) wiz.getProperty(Iterators.PROP_EXEMPTED_STUDENTS);
//                try {
//                    Ticket[] t = ticketBean.getTickets(unit, term, signeeType, true);
//                    ret = singleTicketOrException(t);
//                    if (list != null) {
//                        ticketBean.setExemptedStudents(ret, list);
//                    }
//                } catch (NoEntityFoundException ex) {
//                    throw new IllegalStateException(ex);
//                }
//            } else {
//                TargetAssessmentTicketBean ticketBean = remoteLookup.lookup(TargetAssessmentTicketBean.class);
//                if (ticketBean == null) {
//                    throw new IllegalStateException("No TargetTicketsBean.");
//                }
//                DocumentId doc = (DocumentId) wiz.getProperty(Iterators.PROP_TARGETDOCUMENT);
//                StudentId student = (StudentId) wiz.getProperty(Iterators.PROP_STUDENTID);
//                try {
//                    Ticket[] t = ticketBean.getTickets(doc, term, student, signeeType, true);
//                    ret = singleTicketOrException(t);
//                } catch (NoEntityFoundException ex) {
//                    throw new IllegalStateException(ex);
//                }
//                Long delInterval = (Long) wiz.getProperty(Iterators.PROP_DELETE_INTERVAL);
//                if (delInterval != null && student != null) {
//                    try {
//                        ticketBean.setDeleteInterval(ret, delInterval.intValue());
//                    } catch (NoEntityFoundException ex) {
//                        throw new IllegalStateException(ex);
//                    }
//                }
//            }
//            ticketsBean.addTicket(uid, ret);
//        }
//    }
//
//    private Ticket singleTicketOrException(Ticket[] t) {
//        if (t == null || t.length != 1) {
//            throw new IllegalStateException("Tickets is null or not of length one.");
//        }
//        return t[0];
//    }
    @Override
    public void actionPerformed(ActionEvent event) {
//        final Set<String> targetTypes = new HashSet<>();
//        String targetTypeWarning = null;
//        for (final PrimaryUnitOpenSupport puos : context) {
//            final RemoteUnitsModel rum;
//            try {
//                rum = puos.getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.NO_INITIALISATION);
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//                return;
//            }
//            if (!rum.getInitialization().satisfies(RemoteUnitsModel.INITIALISATION.MAXIMUM)) {
//                //Log target types list may be incomplete
//                targetTypeWarning = NbBundle.getMessage(AddUnitTicketAction.class, "AddUnitTicketAction.action.warning.loadingIncomplete");
//                //add target types from documents model
//                try {
//                    final String sfx = rum.getUnitOpenSupport().findBetulaProjectProperties().getProperty(DocumentsModel.PROP_DOCUMENT_SUFFIXES);
//                    Arrays.stream(sfx.split(","))
//                            .forEach(targetTypes::add);
//                } catch (IOException ex) {
//                    throw new IllegalStateException(ex);
//                }
//            }
//            rum.getTargets().stream()
//                    .map(TargetDocument::getTargetType)
//                    .forEach(targetTypes::add);
//        }
        final Date wd = Lookup.getDefault().lookup(WorkingDate.class).getCurrentWorkingDate();
        final Map<String, List<PrimaryUnitOpenSupport>> m = context.stream()
                .collect(Collectors.groupingBy(puos -> {
                    try {
                        return puos.findWebServiceProvider().getInfo().getURL();
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }));
        for (Map.Entry<String, List<PrimaryUnitOpenSupport>> e : m.entrySet()) {
            final WebServiceProvider ws = WebProvider.find(e.getKey(), WebServiceProvider.class);
            final LocalProperties lp = LocalProperties.find(e.getKey());
            final String sfx = lp.getProperty(DocumentsModel.PROP_DOCUMENT_SUFFIXES);
            final Set<String> targetTypes = Arrays.stream(sfx.split(","))
                    .collect(Collectors.toSet());
            final Map<String, List<PrimaryUnitOpenSupport>> mi = e.getValue().stream()
                    .collect(Collectors.groupingBy(puos -> {
                        try {
                            return puos.findTermSchedule().getName();
                        } catch (IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }));
            for (Map.Entry<String, List<PrimaryUnitOpenSupport>> ei : mi.entrySet()) {
                final TermSchedule ts;
                try {
                    ts = ei.getValue().get(0).findTermSchedule();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                final Term term = ts.getTerm(wd);
                RemoteTicketModel2 rm;
                try {
                    rm = RemoteTicketModel2.get(e.getKey());
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                final Set<UnitId> units = ei.getValue().stream()
                        .map(PrimaryUnitOpenSupport::getUnitId)
                        .collect(Collectors.toSet());
                final StringJoiner sj = new StringJoiner(", ");
                ei.getValue().stream()
                        .map(puos -> {
                            try {
                                return puos.findNamingResolverResult().getResolvedName(term);
                            } catch (IOException ex) {
                                return puos.getUnitId().getId();
                            }
                        })
                        .sorted(Collator.getInstance(Locale.getDefault()))
                        .forEach(sj::add);
                final PrimaryUnitOpenSupport singleUos = ei.getValue().size() == 1 ? ei.getValue().iterator().next() : null;
                try {
                    processSelection(ws, singleUos, units, sj.toString(), term, targetTypes, rm, null);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private static void processSelection(final WebServiceProvider ws, final PrimaryUnitOpenSupport singleUos, final Set<UnitId> units, final String display, final Term tm, final Set<String> targetTypesAvailable, final RemoteTicketModel2 rm, final String targetTypeWarning) throws IOException {
        final WizardDescriptor wiz = new WizardDescriptor(new CreateTicketWizardIterator2());
        wiz.putProperty(Iterators.PROP_UNITOPENSUPPORT, singleUos);
        wiz.putProperty(Iterators.PROP_TARGETTYPESAVAILABLE, targetTypesAvailable);
        wiz.putProperty(Iterators.PROP_REMOTETICKETMODEL, rm);
        wiz.putProperty(Iterators.PROP_DELETE_INTERVAL_ENABLED, Boolean.FALSE);
        wiz.putProperty(Iterators.PROP_STUDENTID_ENABLED, Boolean.FALSE);
        wiz.putProperty(Iterators.PROP_TARGETTYPE_WARNING, targetTypeWarning);
        wiz.setTitleFormat(new MessageFormat(NbBundle.getMessage(Iterators.class, "TicketWizardAction.step")));
        wiz.setTitle(NbBundle.getMessage(Iterators.class, "TicketWizardAction.title"));
        wiz.putProperty(Iterators.PROP_SCOPE_MESSAGE, display);
        final String prefSigneeType = NbPreferences.forModule(AddUnitTicketAction.class).get(Iterators.PROP_SIGNEETYPES, null);
        if (prefSigneeType != null) {
            wiz.putProperty(Iterators.PROP_SIGNEETYPES, prefSigneeType.split(","));
        }
        final String prefComp = NbPreferences.forModule(AddUnitTicketAction.class).get(Iterators.PROP_ADDTOEVENTCOMPONENT, null);
        if (prefComp != null) {
            try {
                final UID prefUID = UID.parse(prefComp);
                wiz.putProperty(Iterators.PROP_ADDTOEVENTCOMPONENT_UID, prefUID);
            } catch (ParseException ex) {
                PlatformUtil.getCodeNameBaseLogger(AddUnitTicketAction.class).log(Level.FINE, ex.getLocalizedMessage(), ex);
            }
        }
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final String[] signeeType = (String[]) wiz.getProperty(Iterators.PROP_SIGNEETYPES);
            final String[] targetTypes = (String[]) wiz.getProperty(Iterators.PROP_TARGETTYPES);
            final boolean addToEvent = (boolean) wiz.getProperty(Iterators.PROP_ADDTOEVENT);
            final CreateTicketComponentVisualPanel.TicketEntry2 event = (CreateTicketComponentVisualPanel.TicketEntry2) wiz.getProperty(Iterators.PROP_ADDTOEVENTCOMPONENT);
            final String summary = (String) wiz.getProperty(Iterators.PROP_NEWSUMMARY);
            final LocalDateTime date = (LocalDateTime) wiz.getProperty(Iterators.PROP_NEWDATETIME);
            final String categories = (String) wiz.getProperty(Iterators.PROP_NEWCAT);
            final Integer priority = (Integer) wiz.getProperty(Iterators.PROP_NEWPRIO);
            final StudentId[] exempt = (StudentId[]) wiz.getProperty(Iterators.PROP_EXEMPTED_STUDENTS);

            final ICalendarBuilder cb = new ICalendarBuilder();
            final ICalendarBuilder.CalendarComponentBuilder ccb;
            final UID uid = addToEvent ? event.uid : null;
            try {
                cb.addProperty("METHOD", "UPDATE");
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
                    run(ws, rm.getTicketsUrl(), units, exempt, tm, signeeType, targetTypes, ccb, uid, cb);
                } catch (IOException ex) {
                    RemoteTicketModel2.notifyError(ex, ex.getLocalizedMessage());
                }
            });
        }
    }

    private static void run(final WebServiceProvider ws, final String http, final Set<UnitId> units, final StudentId[] exempt, final Term tm, final String[] signeeType, final String[] targetTypes, final ICalendarBuilder.CalendarComponentBuilder ccb, final UID comp, final ICalendarBuilder cb) throws IOException {
        final Map<UnitId, Ticket> m = postCreateTicket(ws, units, exempt, tm.getScheduledItemId(), signeeType, targetTypes);
        for (Ticket t : m.values()) {
            try {
                ccb.addProperty("X-TICKET", Long.toString(t.getId()), new Parameter("x-ticket-authority", t.getAuthority()));
            } catch (InvalidComponentException ex) {
                throw new IOException(ex);
            }
        }
        CalendarHttpUtilities.putCalendar(ws, http, cb.toICalendar());
        if (signeeType != null) {
            final String arr = Arrays.stream(signeeType).collect(Collectors.joining(","));
            NbPreferences.forModule(AddUnitTicketAction.class).put(Iterators.PROP_SIGNEETYPES, arr);
        }
        if (comp != null) {
            final String uid = comp.toString();
            NbPreferences.forModule(AddUnitTicketAction.class).put(Iterators.PROP_ADDTOEVENTCOMPONENT_UID, uid);
        }
    }

    static Map<UnitId, Ticket> postCreateTicket(final WebServiceProvider service, final Set<UnitId> units, final StudentId[] exempt, final TermId term, String[] signeeType, String[] targetTypes) throws IOException {
        ContainerBuilder builder = new ContainerBuilder();
        units.forEach(pu -> {
            final TicketEntry te = new TicketEntry(Action.FILE, null, "unit-ticket", "1.0");
            final List<GenericXmlTicket.XmlTicketScope> scope = te.getValue().getScope();
            scope.add(new GenericXmlTicket.XmlTicketScope("unit", pu, "include"));
            if (term != null) {
                scope.add(new GenericXmlTicket.XmlTicketScope("term", term, "include"));
            }
            if (signeeType != null) {
                Arrays.stream(signeeType).forEach(e -> scope.add(new GenericXmlTicket.XmlTicketScope("entitlement", e, "include")));
            }
            if (targetTypes != null) {
                Arrays.stream(targetTypes).forEach(e -> scope.add(new GenericXmlTicket.XmlTicketScope("target-type", e, "include")));
            }
            if (exempt != null) {
                Arrays.stream(exempt).forEach(sid -> scope.add(new GenericXmlTicket.XmlTicketScope("student", sid, "exclude")));
            }
            builder.add(te, Paths.TICKETS_PATH);
        });
        Container ret = null;
        try {
            ret = service.createServicePort().solicit(builder.getContainer());
        } catch (ServiceException ex) {
            throw new IOException(ex);
        }
        if (ret != null) {
            final List<Envelope> l = DocumentUtilities.findEnvelope(ret, Paths.TICKETS_PATH);
            final Map<Ticket, TicketEntry> entries = l.stream()
                    //                    .map(Envelope::getChildren)
                    //                    .flatMap(Collection::stream)
                    .filter(org.thespheres.betula.document.util.TicketEntry.class::isInstance)
                    .map(org.thespheres.betula.document.util.TicketEntry.class::cast)
                    .filter(TicketActionUtil::checkException)
                    .filter(e -> Action.CONFIRM.equals(e.getAction()))
                    .collect(Collectors.toMap(org.thespheres.betula.document.util.TicketEntry::getIdentity, Function.identity()));
            return units.stream()
                    .collect(Collectors.toMap(Function.identity(), did -> {
                        return entries.entrySet().stream()
                                .filter(e -> e.getValue().getValue().getScope().stream().anyMatch(s -> "unit".equals(s.getScope()) && did.equals(s.getValue())))
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
            panels.add(new CreateTicketVisualPanel2.CreateTicketPanel2());
            panels.add(new SetExemptedStudentsWizardPanel());
            panels.add(new CreateTicketComponentVisualPanel.CreateTicketComponentWizardPanel2());
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
