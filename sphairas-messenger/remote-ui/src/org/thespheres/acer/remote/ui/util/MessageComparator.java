/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.util;

import java.time.LocalDateTime;
import java.util.Comparator;
import org.thespheres.acer.remote.ui.AbstractMessage;

/**
 *
 * @author boris.heithecker
 */
public class MessageComparator implements Comparator<AbstractMessage> {

    @Override
    public int compare(AbstractMessage m1, AbstractMessage m2) {
        int prio1 = findPrio(m1);
        int prio2 = findPrio(m2);
        if (prio1 != prio2) {
            return prio2 - prio1;
        }
        LocalDateTime ts1 = findTimestamp(m1);
        LocalDateTime ts2 = findTimestamp(m2);
        return ts2.compareTo(ts1);
    }

    private LocalDateTime findTimestamp(AbstractMessage m1) {
        LocalDateTime ret = m1.getCreation();
        if (ret == null) {
            ret = LocalDateTime.now();
        }
        return ret;
    }

    private int findPrio(AbstractMessage m1) {
        int prio1 = m1.getPriority();
        prio1 = prio1 != 0 ? prio1 : 5;
        return prio1;
    }

}
