/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.awt.Image;
import javax.swing.event.ChangeListener;

/**
 *
 * @author boris.heithecker
 */
public interface IconAnnotator {

    public Image annotateIcon(Image original, boolean openedNode);

    public String annotateHtml(String originalDisplayName, String originalHtml);

    public void addChangeListener(ChangeListener listener);

    public void removeChangeListener(ChangeListener listener);
}
