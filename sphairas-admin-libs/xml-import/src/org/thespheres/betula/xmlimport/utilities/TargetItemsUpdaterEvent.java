/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import org.thespheres.betula.document.Container;

/**
 *
 * @author boris.heithecker
 */
public final class TargetItemsUpdaterEvent extends UpdaterEvent<TargetItemsUpdater> {

    private final Container containerRequest;
    private final Container containerReturn;

    public TargetItemsUpdaterEvent(TargetItemsUpdater source, Container containerRequest, Container containerReturn) {
        super(source);
        this.containerRequest = containerRequest;
        this.containerReturn = containerReturn;
    }


    public Container getContainerRequest() {
        return containerRequest;
    }

    public Container getContainerReturn() {
        return containerReturn;
    }

}
