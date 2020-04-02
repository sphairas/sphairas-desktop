/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

/**
 *
 * @author boris.heithecker
 * @param <V>
 */
public class PolicyRun<V extends VersetzungsValidation> {

    boolean matchShortLabel = false;
    boolean unbias = true;

    protected PolicyRun(Policy policy, V validation) {
        unbias = validation.findBooleanProperty("unbias", policy);
        matchShortLabel = validation.findBooleanProperty("match.short.label", policy);
    }

    protected void log(String message, Exception ex) {
    }

}
