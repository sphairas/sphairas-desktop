/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.unitsui;

import java.awt.EventQueue;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.AdminUnits;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
public class UnitsUITableModel extends AbstractTableModel {

    private final List<StudentUnits> items = new ArrayList<>();
    private final Map<String, List<Unit>> columns = new HashMap<>();
    private final List<String> columnNames = new ArrayList<>();

    void initialize(final RemoteUnitsModel rum) throws IOException {
        final WebServiceProvider provider = rum.getUnitOpenSupport().findWebServiceProvider();

        final NamingResolver nr = rum.getUnitOpenSupport().findNamingResolver();
        final TermSchedule ts = rum.getUnitOpenSupport().findTermSchedule();
        final WorkingDate wdi = Lookup.getDefault().lookup(WorkingDate.class);
        final Date wd = wdi.getCurrentWorkingDate();
        final Term term = ts.getTerm(wd);

        final Set<String> level = Arrays.stream(rum.getUnits())
                .map(u -> NameUtil.findKey(nr, u, term))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final long start = System.currentTimeMillis();
        final List<Unit> units = rum.getUnitOpenSupport().getUnits()
                .map(Units::getUnits)
                .orElseGet(() -> (Set<UnitId>) Collections.EMPTY_SET)
                .stream()
                .filter(u -> Arrays.stream(rum.getUnits()).noneMatch(u::equals))
                //                .peek(sl -> sl.initStudents(rum, wdi.isNow() ? null : wd))
                .filter(u -> {
                    final String l = NameUtil.findKey(nr, u, term);
                    return l != null && level.contains(l);
                })
                .map(AdminUnits.get(provider.getInfo().getURL())::getUnit)
                .distinct()
                .collect(Collectors.toList());
        final long time = System.currentTimeMillis() - start;
        PlatformUtil.getCodeNameBaseLogger(UnitsUITableModel.class).log(Level.INFO, "Initialized unit view of {0} with {1} in {2}ms.", new Object[]{rum.getUnitOpenSupport().getNodeDelegate().getDisplayName(), Integer.toString(units.size()), Long.toString(time)});

        final List<StudentUnits> studs = rum.getStudents().stream()
                .map(rs -> StudentUnits.getUnits(rs))
                .peek(su -> su.update(units))
                .collect(Collectors.toList());

        final Map<String, List<Unit>> col = units.stream()
                .collect(Collectors.groupingBy(u -> NameUtil.findColumnName(nr, u.getUnitId())));

        EventQueue.invokeLater(() -> doInitialize(studs, col));
    }

    private void doInitialize(final List<StudentUnits> studs, final Map<String, List<Unit>> col) {
        items.clear();
        columnNames.clear();
        columns.clear();
        items.addAll(studs);
        columns.putAll(col);
        columns.entrySet().stream()
                .map(e -> e.getKey())
                .filter(v -> !StringUtils.isBlank(v))
                .sorted(Collator.getInstance(Locale.getDefault()))
                .forEach(columnNames::add);
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size() + 1;
    }

    @Override
    public Object getValueAt(int ri, int ci) {
        final StudentUnits su = items.get(ri);
        if (ci == 0) {
            return su.getStudent();
        } else {
            final String n = columnNames.get(ci - 1);
            return columns.get(n).stream()
                    .filter(u -> su.getUnits().contains(u.getUnitId()))
                    .collect(Collectors.toList());
        }
    }

    static String unitsToString(final List<Unit> l) {
        return l.stream()
                .map(Unit::getDisplayName)
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        super.setValueAt(aValue, rowIndex, columnIndex); //To change body of generated methods, choose Tools | Templates.
    }

    public String getColumnNameAt(final int col) {
        return columnNames.get(col);
    }

    public List<Unit> getUnitsForColumn(final String name) {
        return columns.get(name);
    }
}
