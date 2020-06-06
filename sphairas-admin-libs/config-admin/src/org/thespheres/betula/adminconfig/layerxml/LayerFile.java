/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig.layerxml;

/**
 *
 * @author boris.heithecker
 */
public abstract class LayerFile extends AbstractLayerFile {

    protected LayerFile() {
    }

    protected LayerFile(String name) {
        super(name);
    }

    public abstract String getUrl();

    public abstract void setUrl(String value);

    public abstract void setValue(String value);

    public abstract void setAttribute(final String name, final String valueType, final String value);

}
