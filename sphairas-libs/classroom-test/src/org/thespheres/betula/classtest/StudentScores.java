/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest;

import java.util.Set;
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author boris.heithecker
 */
public interface StudentScores {

    public Double get(String key);

    public void put(String key, Double value);

    public void remove(String key);
    
    public Set<String> keys();

    public Grade getGrade();

    public void setGrade(Grade grade);

    public boolean isAutoDistributing();

    public void setAutoDistributing(boolean autoDistributing);

    public String getNote();

    public void setNote(String note);

    public Double sum();
}
