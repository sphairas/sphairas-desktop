/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

/**
 *
 * @author boris.heithecker
 */
public class MyActionListenerImpl implements ActionListener {

    private final URI uri;

    public MyActionListenerImpl(URI uri) {
        this.uri = uri;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
