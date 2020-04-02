/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.timetbl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import org.openide.util.ChangeSupport;
import org.thespheres.betula.project.local.timetbl.LocalClassSchedule.Time;
import org.thespheres.betula.project.local.timetbl.LocalTimetable.Lesson;
import org.thespheres.betula.services.scheme.spi.PeriodId;

/**
 *
 * @author boris.heithecker
 */
class TimetablePanelModel extends AbstractTableModel {

    private boolean valid = true;
    private final LocalClassSchedule classSchedule;
    private final LocalTimetable timetable;
    private final ChangeSupport cSupport = new ChangeSupport(this);

    TimetablePanelModel(LocalClassSchedule lcs, LocalTimetable lt) {
        this.classSchedule = lcs;
        this.timetable = lt;
        updateValid();
    }

//    @Override
//    public void taskFinished(Task task) {
//        task.removeTaskListener(this);
//        EventQueue.invokeLater(() -> {
//            classSchedule = loader.getLocalClassSchedule();
//            timetable = loader.getLocalTimeTable();
//            fireTableStructureChanged();
//        });
//    }
    private Optional<LocalClassSchedule> getSchedule() {
        return Optional.ofNullable(classSchedule);
    }

    private Optional<LocalTimetable> getTimetable() {
        return Optional.ofNullable(timetable);
    }

//    LocalDate getTimetableStart() {
//        return getTimetable().map(tt -> tt.getStart()).orElse(null);
//    }
//
//    void setTimetableStart(LocalDate date) {
//        getTimetable().ifPresent(tt -> tt.setStart(date));
//    }
//
//    LocalDate getTimetableEnd() {
//        return getTimetable().map(tt -> tt.getEnd()).orElse(null);
//    }
//
//    void setTimetableEnd(LocalDate date) {
//        getTimetable().ifPresent(tt -> tt.setEnd(date));
//    }
    @Override
    public int getRowCount() {
        return getSchedule().map(lcs -> lcs.getTimes().size()).orElse(0);
    }

    @Override
    public int getColumnCount() {
        return 9;
    }

    @Override
    public Object getValueAt(int ri, int ci) {
        if (ci < 3) {
            return schedule(ri, ci);
        } else {
            return timetable(ri, ci - 3);
        }
    }

    private Object schedule(int ri, int ci) {
        return getSchedule().map(lcs -> {
            Time t = lcs.getTimes().get(ri);
            switch (ci) {
                case 0:
                    return t.getPeriod();
                case 1:
                    return t.getBegin();
                case 2:
                    return t.getEnd();
                default:
                    return null;
            }
        }).orElse(null);
    }

    private void schedule(int ri, int ci, Date v) {
        getSchedule().ifPresent(lcs -> {
            Time t = lcs.getTimes().get(ri);
            LocalTime lt = v.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            switch (ci) {
                case 1:
                    t.setBegin(lt);
                    break;
                case 2:
                    t.setEnd(lt);
                    break;
            }
            updateValid();
        });
    }

    private boolean timetable(int ri, int ci) {
        final DayOfWeek d = DayOfWeek.of(ci + 1);
        return getSchedule()
                .map(lcs -> lcs.getTimes().get(ri))
                .map(t -> getTimetable().map(tt -> tt.getLessons().stream()
                        .anyMatch(l -> l.getDay().equals(d) && l.getPeriod().getId() == t.getPeriod())).orElse(Boolean.FALSE))
                .orElse(Boolean.FALSE);
    }

    private void timetable(int ri, int ci, boolean v) {
        DayOfWeek d = DayOfWeek.of(ci + 1);
        getSchedule().map(lcs -> lcs.getTimes().get(ri)).ifPresent(t -> {
            if (v) {
                PeriodId pid = new PeriodId("local", t.getPeriod(), PeriodId.Version.UNSPECIFIED);
                getTimetable().ifPresent(tt -> tt.getLessons().add(new Lesson(pid, d)));
            } else {
                Iterator<Lesson> it = getTimetable().map(tt -> tt.getLessons().iterator()).orElse(null);
                while (it != null && it.hasNext()) {
                    Lesson l = it.next();
                    if (l.getDay().equals(d) && l.getPeriod().getId() == t.getPeriod()) {
                        it.remove();
                        break;
                    }
                }
            }
        });

    }

    @Override
    public boolean isCellEditable(int ri, int ci) {
        return ci != 0;
    }

    @Override
    public void setValueAt(Object v, int ri, int ci) {
        if (ci < 3) {
            if (v instanceof Date) {
                schedule(ri, ci, (Date) v);
            }
        } else {
            timetable(ri, ci - 3, (Boolean) v);
        }
    }

    boolean isValid() {
        return valid;
    }

    private void updateValid() {
        final boolean before = this.valid;
        this.valid = classSchedule.validate();
        if (before != this.valid) {
            cSupport.fireChange();
        }
    }

    void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

}
