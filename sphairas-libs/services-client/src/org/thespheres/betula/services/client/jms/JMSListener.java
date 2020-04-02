/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.client.jms;

import org.thespheres.betula.services.jms.JMSEvent;

/**
 *
 * @author boris.heithecker
 * @param <E>
 */
public interface JMSListener<E extends JMSEvent> {

    public void addNotify();

    public void removeNotify();

    public void onMessage(E event);
}
