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
public interface LayerFileSystem extends LayerFolder {

    public abstract LayerFile getFile(final String path, final boolean create);

}
