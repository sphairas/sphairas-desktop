/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.util.Map;
import org.thespheres.betula.document.Document;
import org.thespheres.betula.document.Signee;

/**
 *
 * @author boris.heithecker
 */
public interface TargetDocument extends Document {

    public String getPreferredConvention();

    public String getTargetType();
    
    public Map<String, Signee> getSignees();
    
}
