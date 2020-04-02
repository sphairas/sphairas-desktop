/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
@OptionsPanelController.ContainerRegistration(id = "GPUntis",
        categoryName = "#OptionsCategory_Name_Untis",
        iconBase = "org/thespheres/betula/gpuntis/resources/clock.png",
        keywords = "#OptionsCategory_Keywords_Untis",
        keywordsCategory = "Security", 
        position = 770)
@NbBundle.Messages(value = {"OptionsCategory_Name_Untis=GP Untis",
    "OptionsCategory_Keywords_Untis=untis"})
package org.thespheres.betula.gpuntis;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
