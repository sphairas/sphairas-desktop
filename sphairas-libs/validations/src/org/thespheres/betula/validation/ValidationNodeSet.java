/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation;

import java.util.List;
import javax.swing.event.ChangeListener;

/**
 *
 * @author boris.heithecker
 */
public interface ValidationNodeSet {

    public List<Validation<?, ?>> getNodes();
    
    public <V extends Validation<?, ?>> List<V> getNodes(Class<V> type);

    public void addChangeListener(ChangeListener l);

    public void removeChangeListener(ChangeListener l);
    
    
}
