/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project;

/**
 *
 * @author boris.heithecker
 */
public interface BetulaProjectType {

    public static BetulaProjectType LOCAL = new BetulaProjectType() {
    };
    public static BetulaProjectType PROVIDER = new BetulaProjectType() {
    };
}
