/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme.spi;

import org.thespheres.betula.Identity;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <S>
 */
public interface ScheduledItem<I extends Identity, S extends Schedule> {

    public I getScheduledItemId();

    public S getSchedule();

    public String getDisplayName();
}
