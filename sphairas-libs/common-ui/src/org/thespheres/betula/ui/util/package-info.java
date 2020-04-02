/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
@OptionsPanelController.ContainerRegistration(id = "Security",
        categoryName = "#OptionsCategory_Name_Security",
        iconBase = "org/thespheres/betula/ui/resources/key.png",
        keywords = "#OptionsCategory_Keywords_Security",
        keywordsCategory = "Security",
        position = 150)
@NbBundle.Messages(value = {"OptionsCategory_Name_Security=Sicherheit",
    "OptionsCategory_Keywords_Security=security"})
package org.thespheres.betula.ui.util;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
