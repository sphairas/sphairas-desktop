/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest;

/**
 *
 * @author boris.heithecker
 */
public interface Hierarchical {

    public String getId();

    public String getParentId();

    public void moveTo(String parentId) throws HierarchyExcpeption;

}
