/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.config;

import javax.swing.Action;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author boris.heithecker
 */
class CurriculumFilterNode extends FilterNode {

    CurriculumFilterNode(final Node original) {
        super(original);
    }

//    @Override
//    public boolean canDestroy() {
//        return true;
//    }
    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

//    @Override
//    public void destroy() throws IOException {
//        super.destroy();
//    }
    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public void setName(String s) {
        super.setName(s);
    }

    @Override
    public Action[] getActions(boolean context) {
        return super.getActions(context);
    }
}
