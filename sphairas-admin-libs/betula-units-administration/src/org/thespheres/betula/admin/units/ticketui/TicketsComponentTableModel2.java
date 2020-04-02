/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ticketui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;
import org.thespheres.betula.Ticket;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteTicket;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.ticketui.TicketsComponentTableModel2.ColFactory;
import org.thespheres.betula.document.util.GenericXmlTicket;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel.PluggableColumnFactory;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
public class TicketsComponentTableModel2 extends AbstractPluggableTableModel<RemoteTicketModel2, RemoteTicket, PluggableTableColumn<RemoteTicketModel2, RemoteTicket>, ColFactory> implements ChangeListener {

    private final List<Ticket> current = new ArrayList<>();

    private TicketsComponentTableModel2(Set<PluggableTableColumn<RemoteTicketModel2, RemoteTicket>> s) {
        super("TicketsComponentTableModel2", s);
    }

    static TicketsComponentTableModel2 create() {
        final Set<PluggableTableColumn<RemoteTicketModel2, RemoteTicket>> set = TicketsComponentColumn.createDefaultSet();
        return new TicketsComponentTableModel2(set);
    }

    @Override
    protected ColFactory createColumnFactory() {
        return new ColFactory();
    }

    @Override
    protected int getItemSize() {
        return current.size();
    }

    @Override
    protected RemoteTicket getItemAt(final int row) {
        return model.getTicket(current.get(row));
    }

    private AbstractUnitOpenSupport currentUnitSupport() {
        return getContext().lookup(AbstractUnitOpenSupport.class);
    }

    @Override
    public synchronized void initialize(RemoteTicketModel2 model, Lookup context) {
        super.initialize(model, context);
        final AbstractUnitOpenSupport uos = currentUnitSupport();
        try {
            final RemoteUnitsModel rum = uos.getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.NO_INITIALISATION);
            rum.addPropertyChangeListener(WeakListeners.propertyChange(evt -> stateChanged(null), rum));
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(TicketsComponentTableModel2.class).log(LogLevel.INFO_WARNING, ex.getLocalizedMessage(), ex);
        }
        stateChanged(null);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Mutex.EVENT.writeAccess(() -> {
            reload();
            fireTableStructureChanged();
        });
    }

    private void reload() {
        final AbstractUnitOpenSupport uos = currentUnitSupport();
        current.clear();
        if (uos != null) {
            model.tickets().stream()
                    .filter(t -> t.getTicketDocument().getScope().stream().anyMatch(s -> scopeApplies(s, uos)))
                    .map(t -> t.getTicket())
                    .forEach(current::add);
        }
    }

    private boolean scopeApplies(GenericXmlTicket.XmlTicketScope xml, AbstractUnitOpenSupport pu) {
        final String scope = xml.getScope();
        final RemoteUnitsModel m;
        try {
            m = pu.getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.STUDENTS);
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(TicketsComponentTableModel2.class).log(LogLevel.INFO_WARNING, ex.getLocalizedMessage(), ex);
            return false;
        }
        switch (scope) {
            case "unit":
                return pu instanceof PrimaryUnitOpenSupport && xml.getValue().equals(((PrimaryUnitOpenSupport) pu).getUnitId());
            case "target":
                return m.getTargets().stream()
                        .map(RemoteTargetAssessmentDocument::getDocumentId)
                        .anyMatch(xml.getValue()::equals);
            case "student":
                return m.getStudents().stream()
                        .map(RemoteStudent::getStudentId)
                        .anyMatch(xml.getValue()::equals);
            default:
                return false;
        }
    }

    class ColFactory extends PluggableColumnFactory {
    }
}
