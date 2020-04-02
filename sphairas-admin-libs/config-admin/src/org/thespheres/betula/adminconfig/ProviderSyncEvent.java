/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

/**
 *
 * @author boris.heithecker
 */
public interface ProviderSyncEvent {

    public void runLater(Runnable run);

    public String getResource();
}
