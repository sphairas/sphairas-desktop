/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportUtil;

/**
 *
 * @author boris.heithecker
 */
public class VCardStudentsUtil {

    private VCardStudentsUtil() {
    }

    public static VCardStudents findFromConfiguration(final ImportTarget p) throws IOException {
        final String purl = p.getProviderInfo().getURL();
        final LocalFileProperties prop = LocalFileProperties.find(purl);
        return VCardStudents.get(prop);
    }

    public static URI findStudentsURI(final ImportTarget p) {
        final String purl = p.getProviderInfo().getURL();
        final LocalFileProperties prop = LocalFileProperties.find(purl);
        final String studentsUrl = URLs.students(prop);
        return studentsUrl != null ? URI.create(studentsUrl) : null;
    }

    public static List<VCardStudent> find(final VCardStudents from, final ImportStudentKey k) {
        final StudentId id = k.getStudentId();
        if (id == null) {
            return tryFindNoStudentId(from, k);
        }
        return from.getStudents().stream()
                .filter(vcs -> id.equals(vcs.getStudentId()))
                .collect(Collectors.toList());
    }

    private static List<VCardStudent> tryFindNoStudentId(final VCardStudents from, final ImportStudentKey k) {
        return from.getStudents().stream()
                .filter(vcs -> k.compareDateOfBirth(vcs) && considerNamesEqual(vcs, k))
                .collect(Collectors.toList());
    }

    @NbBundle.Messages({"VCardStudents.info.resolve=Assoziere SiBank-Name „{0}“ mit „{1}“"})
    private static boolean considerNamesEqual(final VCardStudent vcs, final ImportStudentKey k) {
        final String sb = k.getSourceName();
        if (vcs.getDirectoryName().equals(sb)) {
            return true;
        } else {//TODO: use NICKNAME
            final String[] pp = sb.split(", ");
            if (pp.length == 2) {
                final String sbs = pp[0];
                final String sbn = pp[1];
                if (sbs.equals(vcs.getSurname()) && vcs.getGivenNames().contains(sbn)) {
                    final String msg = NbBundle.getMessage(VCardStudentsUtil.class, "VCardStudents.info.resolve", sb, vcs.getDirectoryName());
                    ImportUtil.getIO().getOut().println(msg);
                    return true;
                }
            }
        }
        return false;
    }
}
