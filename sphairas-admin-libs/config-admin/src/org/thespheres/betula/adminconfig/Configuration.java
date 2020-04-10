/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

/**
 *
 * @author boris
 * @param <T> The configuration clazz
 */
public interface Configuration<T> {

    public T get();

    public String[] getResources();

    public String getResourceName();

}
