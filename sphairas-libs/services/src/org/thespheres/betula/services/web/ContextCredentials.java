package org.thespheres.betula.services.web;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author boris.heithecker
 */
public interface ContextCredentials {

    public String getUsername();

    public char[] getPassword();

    default public void onFailure(String message, Exception ex) {
    }

    public static interface Provider {

        public ContextCredentials getContextCredentials();
    }
}
