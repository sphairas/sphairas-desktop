/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ws.push;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public interface PushEvent<T> {

    public T getEventItem();

}
