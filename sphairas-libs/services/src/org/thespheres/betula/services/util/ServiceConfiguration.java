/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import org.openide.util.NbPreferences;

/**
 *
 * @author boris.heithecker
 */
public class ServiceConfiguration {

    private static final ServiceConfiguration INSTANCE = new ServiceConfiguration();

    private ServiceConfiguration() {
    }

    public static ServiceConfiguration getInstance() {
        return INSTANCE;
    }

    public int[] getRetryTimes() {
        final int v0 = NbPreferences.forModule(ServiceConfiguration.class).getInt("getRetryTimes_0", 0);
        final int v1 = NbPreferences.forModule(ServiceConfiguration.class).getInt("getRetryTimes_1", 1000);
        final int v2 = NbPreferences.forModule(ServiceConfiguration.class).getInt("getRetryTimes_2", 2700);
        return new int[]{v0, v1, v2};
    }

    public long getMaxWaitTimeInEDT() {
        return NbPreferences.forModule(ServiceConfiguration.class).getLong("getMaxWaitTimeInEDT", 10000);
    }
}
