/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.util.AbstractCreationInfo;
import org.thespheres.betula.document.util.AbstractDocumentValidity;

/**
 *
 * @author boris.heithecker
 */
public abstract class TargetDocumentProperties implements TargetDocument {

    private final DocumentId document;
    private final Marker[] markers;
    private final String targetType;
    private final LocalDate deleteDate;
    private final Map<String, String> hints = new HashMap<>();
    private final Map<String, Signee> signees = new HashMap<>();
    private final String preferredConvention;
    private final SigneeInfo creationInfo = new AbstractCreationInfo();
    private final boolean isTextValueTarget;

    protected TargetDocumentProperties(final DocumentId document, final Marker[] markers, final String targetType, final String preferredConvention, final LocalDate deleteDate) {
        this(document, markers, targetType, false, preferredConvention, deleteDate);
    }

    protected TargetDocumentProperties(final DocumentId document, final Marker[] markers, final String targetType, final LocalDate deleteDate) {
        this(document, markers, targetType, true, null, deleteDate);
    }

    protected TargetDocumentProperties(final DocumentId document, final Marker[] markers, final String targetType, final boolean isText, final String preferredConvention, final LocalDate deleteDate) {
        this.document = document;
        this.markers = markers;
        this.targetType = targetType;
        this.isTextValueTarget = isText;
        this.preferredConvention = preferredConvention;
        this.deleteDate = deleteDate;
    }

    public DocumentId getDocument() {
        return document;
    }

    @Override
    public abstract boolean isFragment();

    //kurssgl, fach
    @Override
    public Marker[] markers() {
        return markers != null ? Arrays.copyOf(markers, markers.length) : null;
    }

    @Override
    public Validity getDocumentValidity() {
        return new AbstractDocumentValidity() {

            @Override
            public ZonedDateTime getExpirationDate() {
                return getDeleteDate().atStartOfDay().atZone(ZoneId.systemDefault());
            }

        };
    }

    @Override
    public SigneeInfo getCreationInfo() {
        return creationInfo;
    }

    @Override
    public String getTargetType() {
        return targetType;
    }

    public LocalDate getDeleteDate() {
        return deleteDate;
    }

    public boolean isTextValueTarget() {
        return this.isTextValueTarget;
    }

    public Grade getDefaultGrade() {
        if (this.isTextValueTarget()) {
            throw new UnsupportedOperationException("Method not supported for text value target.");
        }
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getDefaultText() {
        if (!this.isTextValueTarget()) {
            throw new UnsupportedOperationException("Method not supported for non-text value target.");
        }
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Map<String, Signee> getSignees() {
        return signees;
    }

    public Map<String, String> getProcessorHints() {
        return hints;
    }

    @Override
    public String getPreferredConvention() {
        return preferredConvention;
    }

}
