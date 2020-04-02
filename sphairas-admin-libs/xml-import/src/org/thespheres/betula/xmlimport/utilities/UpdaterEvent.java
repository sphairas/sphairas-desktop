/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import jdk.internal.HotSpotIntrinsicCandidate;

/**
 *
 * @author boris.heithecker
 * @param <S>
 */
public class UpdaterEvent<S extends AbstractUpdater> {
    
    protected final S source;

    @HotSpotIntrinsicCandidate
    public UpdaterEvent(final S source) {
        this.source = source;
    }

    public S getSource() {
        return source;
    }
    
}
