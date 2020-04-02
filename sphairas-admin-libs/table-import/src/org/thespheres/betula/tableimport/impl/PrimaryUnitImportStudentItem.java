/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.io.IOException;
import java.util.Optional;
import org.openide.util.Exceptions;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.xmlimport.ImportStudentItem;
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
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            vCard = Optional.ofNullable(c);
        }
        return vCard.orElse(null);
    }

    void setOriginalVCard(VCard vCard) {
        this.dvVCard = vCard;
    }

    public boolean isVCardUpdated() {
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

    protected VCard createVCardImpl() throws IOException {
        final VCardBuilder vb = new VCardBuilder();
        try {
            final String n = ImportItemsUtil.findN(source);
            final String fn = ImportItemsUtil.findFNfromN(n); //key.getSourceName()
            vb.addProperty(VCard.FN, fn)
                    .addProperty(VCard.N, n)
                    .addProperty(VCard.GENDER, ImportItemsUtil.findGender(source.getSourceGender()))
                    .addProperty(VCard.BDAY, key.getDateOfBirth().format(IComponentUtilities.DATE_FORMATTER))
                    .addProperty(VCard.BIRTHPLACE, source.getPlaceOfBirth());
        } catch (InvalidComponentException icex) {
            throw new IOException(icex);
        }
        return vb.toVCard();
    }

}
