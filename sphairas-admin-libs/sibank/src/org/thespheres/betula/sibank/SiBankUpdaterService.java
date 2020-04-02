/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;

/**
 *
 * @author boris.heithecker
 */
public interface SiBankUpdaterService {

    public Exception callService(ContainerBuilder builder, SiBankImportTarget config, TargetItemsUpdater<SiBankKlasseItem> update);
}
