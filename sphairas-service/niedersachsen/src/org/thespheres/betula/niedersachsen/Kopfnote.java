/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import org.openide.util.NbBundle;
import org.thespheres.betula.assess.AbstractGrade;

/**
 *
 * @author boris.heithecker
 */
class Kopfnote extends AbstractGrade {

    public Kopfnote(String gradeConvention, String gradeId) {
        super(gradeConvention, gradeId);
    }

    @Override
    public String getShortLabel() {
        return gradeId.toUpperCase();
    }

    @Override
    public String getLongLabel(Object... formattingArgs) {
        String arg;
        if (formattingArgs.length != 0 && formattingArgs[0] instanceof String) {
            arg = (String) formattingArgs[0];
        } else {
            arg = NbBundle.getMessage(Kopfnote.class, "avsv.defaultLongLabelArg");
        }
        String key = getConvention().substring(14) + ".longlabel." + getId();
        return NbBundle.getMessage(Kopfnote.class, key, arg);
    }

}
