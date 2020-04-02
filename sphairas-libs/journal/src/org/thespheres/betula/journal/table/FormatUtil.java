/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.services.scheme.spi.Period;

/**
 *
 * @author boris.heithecker
 */
@Messages({"FormatUtil.recordId.withPeriod.format={0}, {1,number,integer}. Stunde",
    "FormatUtil.recordId.withPeriodDisplay={0}, {1}",
    "FormatUtil.periodOnly={0,number,integer}. Stunde"})
public class FormatUtil {

    public static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("EE., d. MMM. yyyy");
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("EE., d. MMM. yyyy' ab 'HH:mm");
    public static final NumberFormat WEIGHT_NF = NumberFormat.getNumberInstance(Locale.getDefault());

    static {
        WEIGHT_NF.setMinimumFractionDigits(0);
    }

    private FormatUtil() {
    }

    public static String formatRecordId(RecordId rec, boolean omitDate) {
        final int period = rec.getPeriod();
        if (period == -1) {
            return DTF.format(rec.getLocalDateTime());
        } else if (!omitDate) {
            String d = DF.format(rec.getLocalDate());
            return NbBundle.getMessage(FormatUtil.class, "FormatUtil.recordId.withPeriod.format", d, period);
        } else {
            return NbBundle.getMessage(FormatUtil.class, "FormatUtil.periodOnly", period);
        }
    }

    public static String formatRecordId(RecordId rec, Period period, boolean omitDate) {
        final String periodDisplay = period.getDisplayName();
        if (!omitDate) {
            String d = DF.format(rec.getLocalDate());
            return NbBundle.getMessage(FormatUtil.class, "FormatUtil.recordId.withPeriodDisplay", d, periodDisplay);
        }
        return periodDisplay;
    }

    public static boolean isSameDay(RecordId r, RecordId o) {
        return r.getLocalDate().isEqual(o.getLocalDate());
    }
}
