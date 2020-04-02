/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.util;

import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.curriculum.Section;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.MultiSubject;

/**
 *
 * @author boris.heithecker
 */
public class CurriculumUtil {

    private CurriculumUtil() {
    }

    public static String getDisplayName(final Section s) {
        return s.getBase() + ":" + s.getSequence();
    }

    @NbBundle.Messages({"CurriculumUtil.getDisplayName.formatWithRealm={0} [{1}]"})
    public static String getDisplayName(final CourseEntry c) {
        final String sl;
        final MultiSubject subject = c.getSubject();
        if (subject.getSingleSubject() != null) {
            sl = subject.getSingleSubject().getLongLabel();
        } else {
            sl = subject.getSubjectMarkerSet().stream()
                    .map(Marker::getLongLabel)
                    .collect(Collectors.joining(", ", "(", ")"));
        }
        if (subject.getRealmMarker() == null) {
            return sl;
        } else {
            return NbBundle.getMessage(CurriculumUtil.class, "CurriculumUtil.getDisplayName.formatWithRealm", sl, subject.getRealmMarker().getLongLabel());
        }
    }
}
