/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui;

import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 *
 * @author boris.heithecker
 */
//Also used for ConfigurationPanelComponentProvider, cannot be nested class !
@MimeLocation(subfolderName = "ConfigurationPanelComponent")
public interface ConfigurationPanelComponentProvider {

    public ConfigurationPanelComponent createConfigurationPanelComponent();
    
}
