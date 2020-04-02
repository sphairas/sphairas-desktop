/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.util;

/**
 *
 * @author boris.heithecker
 */
public class Config {

    private static final Config INSTANCE = new Config();
    private static final int TARGET_WAIT_TIME = 60 * 1000;
    private static final int DOCUMENTS_MAX_LOAD_TIME = 5 * 60 * 1000;

    private Config() {
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public int[] getRetryTimes() {
        return new int[]{0, 1000, 2700};
    }

    public static int getTargetWaitTime() {
        return TARGET_WAIT_TIME;
    }

    public static int getDocumentsMaxLoadTime() {
        return DOCUMENTS_MAX_LOAD_TIME;
    }

}
