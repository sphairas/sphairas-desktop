/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest;

/**
 *
 * @author boris.heithecker
 */
public interface Assessable {

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public String getId();

    interface Problem extends Assessable {

        public final static int PROBLEM_DEFAULT_MAX = 5;

        public int getMaxScore();

        public void setMaxScore(int maxScore);
    }
}
