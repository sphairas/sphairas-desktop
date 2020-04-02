/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

/**
 *
 * @author boris.heithecker
 */
public interface LoginBean {

    public static final long serialVersionUID = 1L;

    public String[] getGroups(String prefix, String suffix);

    public String[] getGroups(String ldapName);

}
