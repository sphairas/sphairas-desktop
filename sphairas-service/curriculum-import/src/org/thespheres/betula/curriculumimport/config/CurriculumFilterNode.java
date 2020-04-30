/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.config;

import java.util.Optional;
import javax.swing.Action;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.thespheres.betula.ui.FileInfo;

/**
 *
 * @author boris.heithecker
 */
public class CurriculumFilterNode extends FilterNode {

    public CurriculumFilterNode(final Node original) {
        super(original);
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public void setName(String s) {
        super.setName(s); //To change body of generated methods, choose Tools | Templates.
    }

    //    @Override
//    public boolean canDestroy() {
//        return true;
//    }
//    @Override
//    public void destroy() throws IOException {
//        super.destroy();
//    }
    
    @Override
    public String getDisplayName() {
        return Optional.ofNullable(getLookup().lookup(FileInfo.class))
                .map(FileInfo::getFileDisplayName)
                .orElse(super.getDisplayName());
    }

    @Override
    public Action[] getActions(boolean context) {
        return super.getActions(context);
    }
}
