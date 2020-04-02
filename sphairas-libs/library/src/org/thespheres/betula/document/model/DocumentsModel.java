/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
public class DocumentsModel extends GroupingByIdentity<DocumentId, DocumentId> {

    public static final String PROP_DOCUMENT_SUFFIXES = "documents.model.suffixes";
    public static final String PROP_DOCUMENT_PRIMARY_SUFFIX = "documents.model.primary.suffix";
    public static final String PROP_DOCUMENT_SUFFIX_STUDENTS = "documents.model.suffix.students";
    public static final String DEFAULT_STUDENTS_SUFFIX = "students";
    public static final String PROP_AUTHORITY = "authority";
    protected String studentsSuffix;
    protected String primarySuffix;

    public DocumentsModel() {
        super();
    }

    public void initialize(Map<String, String> props) {
        synchronized (this) {
            final String sfx = props.get(PROP_DOCUMENT_SUFFIXES);
            primarySuffix = props.get(PROP_DOCUMENT_PRIMARY_SUFFIX);
            String studsfx = props.get(PROP_DOCUMENT_SUFFIX_STUDENTS);
            if (studsfx == null) {
                studsfx = DEFAULT_STUDENTS_SUFFIX;
            }
            studentsSuffix = studsfx;
            authority = props.get(PROP_AUTHORITY);
            if (!StringUtils.isBlank(sfx)) {
                final StringJoiner patJoin = new StringJoiner("|", "-(", ")\\z");
                Arrays.stream(sfx.split(","))
                        .forEach(patJoin::add);
                patJoin.add(studentsSuffix);
                pattern = Pattern.compile(patJoin.toString());
                if (primarySuffix == null) {
                    primarySuffix = StringUtils.trimToNull(sfx.split(",")[0]);
                }
            } else {
                Logger.getLogger(DocumentsModel.class.getCanonicalName()).log(Level.CONFIG, "Property documents.model.suffixes not found.");
            }
            checkInitialized();
        }
    }

    public String getModelPrimarySuffix() {
        checkInitialized();
        return primarySuffix;
    }

    public String getSuffix(DocumentId s) {
        String ret = super.match(s);
        return ret != null ? ret.substring(1) : null;
    }

    public Marker[] parseIdentity(DocumentId did) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected DocumentId createIdentity(DocumentId did, String id) throws IllegalArgumentException {
        return new DocumentId(did.getAuthority(), id, did.getVersion());
    }

    public UnitId convertToUnitId(DocumentId s) {
        DocumentId base = convert(s);
        return new UnitId(base.getAuthority(), base.getId());
    }

    public DocumentId convertToUnitDocumentId(UnitId unit) {
        //TODO: überlegen, ob wirklich die ude automatisch generiert wird, oder muss sie vorher angelegt sein?
        checkInitialized();
        if (getAuthority() != null && !unit.getAuthority().equals(getAuthority())) {
            return null;
        }
        String uid = unit.getId();
        final String suffix = "-" + studentsSuffix;
        if (!uid.endsWith(suffix)) {
            uid += suffix;
        }
        return new DocumentId(unit.getAuthority(), uid, DocumentId.Version.LATEST);
    }
}
