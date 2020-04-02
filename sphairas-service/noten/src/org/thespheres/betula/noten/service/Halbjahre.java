/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.service;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Tag;
import org.thespheres.betula.tag.AbstractTag;
import org.thespheres.betula.tag.TagConvention;

/**
 *
 * @author boris.heithecker
 */
@Messages({"Halbjahre.displayName=Halbjahre",
    "Halbjahre.displayName.halbjahr=1. Halbjahr",
    "Halbjahre.displayName.halbjahr.short=1. Hj.",
    "Halbjahre.displayName.schuljahr=2. Halbjahr",
    "Halbjahre.displayName.schuljahr.short=2. Hj."})
public class Halbjahre extends TagConvention<Tag> {

    static final String CONVENTION_NAME = "de.halbjahre";
    static final String[] ID = {"halbjahr", "schuljahr"};

    public Halbjahre() {
        super(CONVENTION_NAME, ID);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(Halbjahre.class, "Halbjahre.displayName");
    }

    @Override
    protected Tag create(String id) {
        return new Halbjahr(id);
    }

    private static class Halbjahr extends AbstractTag {

        private Halbjahr(String gradeId) {
            super(CONVENTION_NAME, gradeId);
        }

        @Override
        public String getShortLabel() {
            return NbBundle.getMessage(Halbjahre.class, "Halbjahre.displayName." + markerId + ".short");
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return NbBundle.getMessage(Halbjahre.class, "Halbjahre.displayName." + markerId);
        }

    }
}
