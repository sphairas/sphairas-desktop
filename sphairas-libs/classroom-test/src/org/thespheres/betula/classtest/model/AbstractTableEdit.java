/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import javax.swing.undo.AbstractUndoableEdit;

/**
 *
 * @author boris.heithecker
 */
abstract class AbstractTableEdit<V, E> extends AbstractUndoableEdit {

    protected final V overridden;
    protected final V override;
    protected final E item;

    AbstractTableEdit(E student, V overridden, V override) {
        this.item = student;
        this.overridden = overridden;
        this.override = override;
    }

    @Override
    public String getRedoPresentationName() {
        return getName();
    }

    @Override
    public String getUndoPresentationName() {
        return getName();
    }

    protected abstract String getName();

    @Override
    public boolean canRedo() {
        return super.canRedo();
    }

    @Override
    public boolean canUndo() {
        return super.canUndo();
    }

}
