/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.imports;

import org.thespheres.betula.curriculumimport.CreateCurriculumWizardIterator;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author boris.heithecker
 */
public class StundentafelVorlagen {

    private StundentafelVorlagen() {
    }

    @TemplateRegistration(folder = "Betula-Extension",
            content = "Stundentafel.xml",
            position = 50000,
            iconBase = "org/thespheres/betula/niedersachsen/admin/ui/resources/books.png",
            requireProject = false,
            displayName = "#StundentafelVorlagen.ndsDefault.displayName",
            description = "stundentafel-template.html")
    @NbBundle.Messages({"StundentafelVorlagen.ndsDefault.displayName=Stundentafel für Niedersachsen"})
    public static CreateCurriculumWizardIterator createNdsDefault() {
        return new CreateCurriculumWizardIterator();
    }
}
