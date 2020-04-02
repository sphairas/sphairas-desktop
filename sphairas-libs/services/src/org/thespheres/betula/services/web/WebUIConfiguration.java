/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.web;

/**
 *
 * @author boris.heithecker
 */
public interface WebUIConfiguration {

    public String getName();
    
    public String getLogoResource();

    public String getLoginProviderDisplayLabel();

    public String[] getCommitTargetTypes();

    public String getDefaultCommitTargetType();

    public String[] getPrimaryUnitListedTargetTypes();

    public String getProperty(String dfa);

}
