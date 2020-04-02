/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

/**
 *
 * @author boris.heithecker
 */
interface XmlContentElement<N> {

    String getKey();

    N getNode();

    void setNode(N n);

}
