/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.mitarbeit.progess;

import org.thespheres.betula.assess.AbstractGrade;

/**
 *
 * @author boris.heithecker
 */
class ProgressGrade extends AbstractGrade {

    ProgressGrade(String gradeId) {
        super(Progress.NAME, gradeId);
    }

    @Override
    public String getLongLabel(Object... formattingArgs) {
        switch (getId()) {
            case Progress.ABSENT:
                return "Fehlend";
            default:
                return getId();
        }
    }

    @Override
    public String getShortLabel() {
        switch (getId()) {
            case Progress.ABSENT:
                return "f";
            default:
                return getId();
        }
    }
}
