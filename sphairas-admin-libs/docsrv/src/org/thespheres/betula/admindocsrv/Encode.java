/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
public class Encode {

    private Encode() {
    }

    public static String getTermIdEncoded(Term term) {
        try {
            String v = Integer.toString(term.getScheduledItemId().getId());
            return URLEncoder.encode(v, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    public static String getTermAuthorityEncoded(Term term) {
        try {
            return URLEncoder.encode(term.getScheduledItemId().getAuthority(), "utf-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    public static String getUnitIdEncoded(UnitId unit) {
        try {
            return URLEncoder.encode(unit.getId(), "utf-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    public static String getUnitAuthorityEncoded(UnitId unit) {
        try {
            return URLEncoder.encode(unit.getAuthority(), "utf-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    public static String getDocumentIdEncoded(DocumentId document) {
        try {
            return URLEncoder.encode(document.getId(), "utf-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    public static String getDocumentAuthorityEncoded(DocumentId document) {
        try {
            return URLEncoder.encode(document.getAuthority(), "utf-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    public static String getStudentIdEncoded(StudentId sid) {
        try {
            final String v = Long.toString(sid.getId());
            return URLEncoder.encode(v, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    public static String getStudentAuthorityEncoded(StudentId sid) {
        try {
            return URLEncoder.encode(sid.getAuthority(), "utf-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
}
