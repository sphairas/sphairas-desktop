/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.time.LocalDate;
import java.util.Optional;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.xmlimport.ImportStudentItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.model.XmlStudentItem;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.VCard;
import org.thespheres.ical.builder.VCardBuilder;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
public class PrimaryUnitImportStudentItem extends ImportStudentItem {

    private StudentId id;
    private boolean selected;
    private final String sourceUnit;
    private String status;
    private VCard dvVCard;
    private Optional<VCard> vCard;
    private final XmlStudentItem source;
    private final ImportStudentKey key;
    private InvalidComponentException exception;

    PrimaryUnitImportStudentItem(ImportStudentKey key, XmlStudentItem stud, String sourceUnit) {
        super(key.toString());
        this.key = key;
        this.source = stud;
        this.sourceUnit = sourceUnit;
    }

    public String getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public StudentId getStudentId() {
        return id;
    }

    protected void setStudentId(StudentId id) {
        this.id = id;
    }

    public VCard getVCard() {
        if (vCard == null) {
            VCard c = null;
            try {
                c = createVCardImpl();
            } catch (final InvalidComponentException ex) {
                this.exception = ex;
            }
            vCard = Optional.ofNullable(c);
        }
        return vCard.orElse(null);
    }

    void setOriginalVCard(VCard vCard) {
        this.dvVCard = vCard;
    }

    public boolean isVCardUpdated() {
        //Never update with erroneous or empty vCard data
        if (exception != null || vCard.isEmpty()) {
            return false;
        }
        return dvVCard == null || !IComponentUtilities.equals(dvVCard, getVCard(), new String[]{"X-STUDENT"});
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public boolean isValid() {
        return getStudentId() != null && getVCard() != null;
    }

    protected VCard createVCardImpl() throws InvalidComponentException {
        final String n = ImportItemsUtil.findN(source);
        final String fn = ImportItemsUtil.findFNfromN(n); //key.getSourceName()
        final LocalDate dateOfBirth = key.getDateOfBirth();
        final String sourceGender = source.getSourceGender();
        if (fn == null || n == null || dateOfBirth == null || sourceGender == null) {
            return null;
        }
        final VCardBuilder vb = new VCardBuilder();
        try {
            vb.addProperty(VCard.FN, fn)
                    .addProperty(VCard.N, n)
                    .addProperty(VCard.BDAY, dateOfBirth.format(IComponentUtilities.DATE_FORMATTER))
                    .addProperty(VCard.GENDER, ImportItemsUtil.findGender(sourceGender))
                    .addProperty(VCard.BIRTHPLACE, source.getPlaceOfBirth());
        } catch (final InvalidComponentException icex) {
            final String message = "Fehler beim Parsen von " + vb.toString();
            ImportUtil.getIO().getErr().println(message);
            throw icex;
        }
        return vb.toVCard();
    }

}
