/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport;

import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author boris.heithecker
 */
public class ImportUtil {

    private static final int KEEP_YEARS = 6;//5 bis 10 plus ein jahr
    private static final Calendar CALENDAR;
    private static InputOutput io;
    private final static Boolean[] SHOW_DETAILS = new Boolean[]{Boolean.FALSE};

    static {
        CALENDAR = Calendar.getInstance();
        CALENDAR.set(Calendar.HOUR_OF_DAY, 0);
        CALENDAR.set(Calendar.MINUTE, 0);
        CALENDAR.set(Calendar.SECOND, 0);
        CALENDAR.set(Calendar.MILLISECOND, 0);
    }

    private ImportUtil() {
    }

    @NbBundle.Messages({"ImportUtil.ioTab.title=Import"})
    public static InputOutput getIO() {
        if (io == null) {
            final Action[] ac = new Action[]{new ShowDetailsAction()};
            io = IOProvider.getDefault().getIO(NbBundle.getMessage(ImportUtil.class, "ImportUtil.ioTab.title"), ac);
        }
        return io;
    }

    public static boolean showDetails() {
        synchronized (SHOW_DETAILS) {
            return SHOW_DETAILS[0];
        }
    }

    private final static class ShowDetailsAction extends AbstractAction {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private ShowDetailsAction() {
            super("set-verbose");
            final Icon icon = ImageUtilities.loadImageIcon("org/thespheres/betula/xmlimport/resources/application-detail.png", true);
            putValue(Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (SHOW_DETAILS) {
                SHOW_DETAILS[0] = !SHOW_DETAILS[0];
            }
        }

    }

    public static LocalDate calculateDeleteDate(int level, int levelBase, Month month) {
        int years = levelBase + KEEP_YEARS;
        if (level > 0) {
            years -= level;
        } else {
            years -= levelBase;
        }
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        Month currentMonth = now.getMonth();
        if (currentMonth.getValue() > month.getValue()) {
            ++currentYear;
        }
        return LocalDate.of(currentYear + years, month, 1).with(TemporalAdjusters.lastDayOfMonth());
    }
}
