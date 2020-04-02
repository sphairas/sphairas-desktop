/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.timetbl;

import java.time.LocalDateTime;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.scheme.spi.ClassSchedule;
import org.thespheres.betula.services.scheme.spi.Period;
import org.thespheres.betula.services.scheme.spi.PeriodId;

/**
 *
 * @author boris.heithecker
 */
@Messages({"PeriodImpl.displayName={0,number,integer}.\u00A0Stunde"})
class LocalPeriod implements Period {

    private final LocalDateTime start;
    private final LocalDateTime end;
    private final PeriodId period;

    LocalPeriod(LocalDateTime start, LocalDateTime end, PeriodId period) {
        this.start = start;
        this.end = end;
        this.period = period;
    }

    @Override
    public ClassSchedule getSchedule() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(LocalPeriod.class, "PeriodImpl.displayName", period.getId());
    }

    @Override
    public LocalDateTime resolveStart() {
        return start;
    }

    @Override
    public LocalDateTime resolveEnd() {
        return end;
    }

    @Override
    public PeriodId getScheduledItemId() {
        return period;
    }

    @Override
    public int getRecurrence() {
        return -1;
    }

}
