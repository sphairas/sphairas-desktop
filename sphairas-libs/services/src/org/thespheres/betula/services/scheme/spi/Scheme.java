/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme.spi;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public interface Scheme<T extends SchemeType> {

    public static final String DEFAULT_SCHEME = "default";//id für schemeprovider

    public T getType();

    public String getName();//unique identifying name for provider

    public String getDisplayName();
}
