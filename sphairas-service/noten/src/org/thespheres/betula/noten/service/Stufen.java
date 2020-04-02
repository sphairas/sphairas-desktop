/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.service;

import java.util.stream.IntStream;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Tag;
import org.thespheres.betula.tag.AbstractTag;
import org.thespheres.betula.tag.TagConvention;

/**
 *
 * @author boris.heithecker
 */
@Messages({"Stufen.displayName=Stufen",
    "Stufen.displayName.stufe={0}. Jahrgang"})
public class Stufen extends TagConvention<Tag> {

    static final String CONVENTION_NAME = "de.stufen";
    static final String[] ID = IntStream.range(1, 13)
            .mapToObj(Integer::toString)
            .toArray(String[]::new);

    public Stufen() {
        super(CONVENTION_NAME, ID);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(Stufen.class, "Stufen.displayName");
    }

    @Override
    protected Tag create(String id) {
        return new Stufe(id);
    }

    private static class Stufe extends AbstractTag {

        private Stufe(String gradeId) {
            super(CONVENTION_NAME, gradeId);
        }

        @Override
        public String getShortLabel() {
            return markerId;
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return NbBundle.getMessage(Stufen.class, "Stufen.displayName.stufe", markerId);
        }

    }
}
