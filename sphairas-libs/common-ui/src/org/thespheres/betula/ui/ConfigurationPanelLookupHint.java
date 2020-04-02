/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui;

/**
 *
 * @author boris.heithecker
 */
public interface ConfigurationPanelLookupHint {

    /**
     * Hint for content type that should be used in Navigator
     *
     * @return String representation of content type (in mime-type style)
     */
    public String getContentType();
    
    public String getDisplayName();
}
