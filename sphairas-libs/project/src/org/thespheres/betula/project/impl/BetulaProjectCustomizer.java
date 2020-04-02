/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thespheres.betula.project.impl;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.HelpCtx;

/**
 *
 * @author boris.heithecker
 */
public class BetulaProjectCustomizer implements CustomizerProvider {

    private Dialog dialog;
    private final Project project;

    public BetulaProjectCustomizer(Project project) {
        this.project = project;
    }

    @Override
    public void showCustomizer() {
        class OKActionL implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.hide();
                dialog.dispose();
            }

        }
        dialog = ProjectCustomizer.createCustomizerDialog("Projects/org-thespheres-betula-project-local/Customizer", project.getLookup(), null, new OKActionL(), null, HelpCtx.DEFAULT_HELP);
        dialog.setVisible(true);
    }

}
