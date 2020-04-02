/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import java.text.ParseException;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
public interface UserRepresentation<I> {

    public String getDisplayName();

    public I parse(String userRepresention) throws IllegalArgumentException, ParseException;
}
