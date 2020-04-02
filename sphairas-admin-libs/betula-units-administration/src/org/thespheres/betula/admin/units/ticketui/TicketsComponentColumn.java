/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ticketui;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Identity;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudents;
import org.thespheres.betula.admin.units.RemoteTicket;
import org.thespheres.betula.document.util.GenericXmlTicket.XmlTicketScope;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.util.SigneeEntitlement;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.ButtonEditor;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
public abstract class TicketsComponentColumn extends PluggableTableColumn<RemoteTicketModel2, RemoteTicket> {

    protected TicketsComponentColumn(String id, int position, boolean editable, int width) {
        super(id, position, editable, width);
    }

    static Set<PluggableTableColumn<RemoteTicketModel2, RemoteTicket>> createDefaultSet() {
        Set<PluggableTableColumn<RemoteTicketModel2, RemoteTicket>> ret = new HashSet<>();
        ret.add(new CancelColumn());
        ret.add(new DateDueColumn());
        ret.add(new MessageScopeColumn());
        ret.add(new TermColumn());
        ret.add(new TicketIDColumn());
        return ret;
    }

    static class CancelColumn extends TicketsComponentColumn {

        private final ButtonEditor button = new ButtonEditor();

        CancelColumn() {
            super("cancel", 10, true, 16);
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public void initialize(RemoteTicketModel2 ecal, Lookup context) {
            super.initialize(ecal, context); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object getColumnValue(RemoteTicket il) {
            return (ActionListener) e -> getModel().removeTicket(il.getTicket());
        }

        @Override
        public void configureColumnWidth(TableColumnExt col) {
            ButtonEditor.configureTableColumn(col);
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<RemoteTicketModel2, RemoteTicket, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.setCellRenderer(button.createRenderer());
            col.setCellEditor(button);
        }

    }

    @Messages({"TicketsComponentColumn.DateDueColumn.displayLabel=Datum",
        "TicketsComponentColumn.message.noCalendar=Kein gültiger Kalendereintrag"})
    static class DateDueColumn extends TicketsComponentColumn implements StringValue {

        private final DateTimeFormatter format = DateTimeFormatter.ofPattern("EE., d.M.yy HH:mm");

        DateDueColumn() {
            super("date", 100, false, 150);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(CancelColumn.class, "TicketsComponentColumn.DateDueColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(RemoteTicket il) {
            if (il.getMessage() != null) {
                return il.getMessage();
            }
            final CalendarComponent ical = il.getCalendar();
            if (ical != null) {
                try {
                    return IComponentUtilities.parseLocalDateTimeProperty(ical, CalendarComponentProperty.DTSTART);
                } catch (InvalidComponentException ex) {
                }
            }
            return NbBundle.getMessage(CancelColumn.class, "TicketsComponentColumn.message.noCalendar");
        }

        @Override
        public String getString(Object value) {
            if (value instanceof LocalDateTime) {
                return format.format((LocalDateTime) value);
            } else if (value instanceof String) {
                return (String) value;
            }
            return "";
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<RemoteTicketModel2, RemoteTicket, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.setCellRenderer(new DefaultTableRenderer(this));
        }

    }

    @Messages("TicketsComponentColumn.MessageColumn.displayLabel=Bereich")
    static class MessageScopeColumn extends TicketsComponentColumn {

        MessageScopeColumn() {
            super("message-scope", 500, false, 320);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(CancelColumn.class, "TicketsComponentColumn.MessageColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(RemoteTicket il) {
            return getScopeMessage(il);
        }

        private String getScopeMessage(RemoteTicket il) {
            StringJoiner sj = new StringJoiner(", ");
            il.getTicketDocument().getScope().stream()
                    .filter(s -> !s.getScope().equals("term"))
                    .map(this::scopeToString)
                    .forEach(sj::add);
            return sj.toString();
        }

        private String scopeToString(final XmlTicketScope<?> scope) {
            final Identity idv = scope.getValue();
            final String text;
            if (idv != null) {
                final AbstractUnitOpenSupport support = getContext().lookup(AbstractUnitOpenSupport.class);
                if (idv instanceof StudentId) {
                    final StudentId sid = (StudentId) idv;
                    if (support != null) {
                        try {
                            return RemoteStudents.find(support.findWebServiceProvider().getInfo().getURL(), sid).getFullName();
                        } catch (IOException ex) {
                        }
                    }
                    return Long.toString(sid.getId());
                }
                try {
                    return support.findNamingResolver().resolveDisplayName(idv);
                } catch (IllegalAuthorityException | IOException ex) {
                    return idv.getId().toString();
                }
            } else if ((text = scope.getTextValue()) != null) {
                if ("entitlement".equals(scope.getScope())) {
                    return SigneeEntitlement.find(text)
                            .map(se -> se.getDisplayName())
                            .orElse(text);
                }
                return text;
            }
            return null;
        }
    }

    @Messages("TicketsComponentColumn.TermColumn.displayLabel=Halbjahr")
    static class TermColumn extends TicketsComponentColumn {

        private final StringValue termStringValue = o -> o instanceof Term ? ((Term) o).getDisplayName() : "";

        TermColumn() {
            super("term", 1000, false, 120);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(CancelColumn.class, "TicketsComponentColumn.TermColumn.displayLabel");
        }

        @Override
        public Term getColumnValue(RemoteTicket il) {
            final TermId term = il.getTicketDocument().getScope().stream()
                    .filter(s -> "term".equals(s.getScope()))
                    .collect(CollectionUtil.singleton())
                    .map(s -> s.getValue())
                    .filter(TermId.class::isInstance)
                    .map(TermId.class::cast)
                    .orElse(null);
            if (term != null) {
                final AbstractUnitOpenSupport support = getContext().lookup(AbstractUnitOpenSupport.class);
                try {
                    TermSchedule ts = support.findTermSchedule();
                    return ts.resolve(term);
                } catch (IOException | TermNotFoundException | IllegalAuthorityException ex) {
                    PlatformUtil.getCodeNameBaseLogger(TicketsComponentColumn.class).log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
            return null;
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<RemoteTicketModel2, RemoteTicket, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.setCellRenderer(new DefaultTableRenderer(termStringValue));
        }
    }

    @Messages("TicketsComponentColumn.TicketIDColumn.displayLabel=Berechtigung")
    static class TicketIDColumn extends TicketsComponentColumn {

        TicketIDColumn() {
            super("ticket", 5000, false, 100);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(CancelColumn.class, "TicketsComponentColumn.TicketIDColumn.displayLabel");
        }

        @Override
        public Object getColumnValue(RemoteTicket il) {
            return Long.toString(il.getTicket().getId());
        }

    }

    public static abstract class Factory extends PluggableTableColumn.Factory<PluggableTableColumn<RemoteTicketModel2, RemoteTicket>> {
    }
}
