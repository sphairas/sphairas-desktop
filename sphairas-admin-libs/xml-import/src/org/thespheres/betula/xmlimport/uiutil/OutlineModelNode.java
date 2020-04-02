/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

/**
 *
 * @author boris.heithecker
 */
public interface OutlineModelNode {

    public Object getColumn(String id);

    public String getHtmlDisplayName();

    public String getTooltip();

}
