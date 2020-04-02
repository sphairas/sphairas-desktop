/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.action;

import java.awt.event.ActionEvent;
import java.net.URI;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.junit.Test;

/**
 *
 * @author boris.heithecker
 */
public class CouchDBMappingsTest {
    
    public CouchDBMappingsTest() {
    }
    
    @Test
    public void testFile() {
        CouchDBMappings.getInstance();
    }
    
    @Test
    public Action testAction() {
        URI uri = null;
        MyActionListenerImpl al = new MyActionListenerImpl(uri);
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                al.actionPerformed(e);
            }
            
        };
    }
    
}
