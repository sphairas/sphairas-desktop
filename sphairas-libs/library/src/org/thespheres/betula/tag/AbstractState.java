/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tag;

import java.io.Serializable;

/**
 *
 * @author boris.heithecker
 * @param <S> The actual subtype
 */
public class AbstractState<S extends AbstractState> extends AbstractTag implements State<S>, Serializable {

    private final int level;
    private final boolean error;

    protected AbstractState(int level, boolean error, String convention, String gradeId) {
        super(convention, gradeId);
        this.level = level;
        this.error = error;
    }

    @Override
    public boolean satisfies(S stage) {
        return stage != null && stage.getLevel() <= getLevel();
    }

    @Override
    public boolean isError() {
        return error;
    }

    protected int getLevel() {
        return level;
    }
}
