/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;
import org.thespheres.betula.xmlimport.ImportStudentItem;
import java.awt.Color;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.ical.VCard;

public class SiBankImportStudentItem extends ImportStudentItem implements Comparable<SiBankImportStudentItem> {

    public static final String PROP_SELECTED = "selected";
    public final ImportStudentKey student;
    private String stringPrimaryUnits = null;
    private final SiBankKlasseItem klasse;
    private String message;
    protected final VCardStudent vCardStudent;
    private boolean selected;

    public SiBankImportStudentItem(VCardStudent vcs, ImportStudentKey s, SiBankKlasseItem outer) {
        super(s.toString());
        this.vCardStudent = vcs;
        this.klasse = outer;
        this.student = s;
    }

    @Override
    public StudentId getStudentId() {
        return vCardStudent.getStudentId();
    }

    public SiBankKlasseItem getSiBankKlasseItem() {
        return klasse;
    }

    public VCard getVCard() {
        return vCardStudent.getVCard();
    }

    public String getPrimaryUnitsAsString() {
        if (getPrimaryUnits() != null && stringPrimaryUnits == null) {
            StringJoiner sj = new StringJoiner(", ");
            Arrays.stream(getPrimaryUnits())
                    .map(i -> klasse.resolveUnitDisplayName(i))
                    .filter(Objects::nonNull)
                    .forEach(sj::add);
            stringPrimaryUnits = sj.toString();
        }
        return stringPrimaryUnits;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) throws PropertyVetoException {
        boolean old = this.selected;
        this.selected = selected;
        try {
            vSupport.fireVetoableChange(PROP_SELECTED, old, this.selected);
        } catch (PropertyVetoException vex) {
            this.selected = old;
            throw vex;
        }
    }

    public void log() {
        if (!StringUtils.isBlank(message)) {
            InputOutput io = ImportUtil.getIO();
            try {
                IOColorLines.println(io, message, Color.RED);
            } catch (IOException ex) {
                io.getOut().println(message);
            }
            message = null;
        }
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int compareTo(SiBankImportStudentItem o) {
        return student.compareTo(o.student);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.student);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SiBankImportStudentItem other = (SiBankImportStudentItem) obj;
        return Objects.equals(this.student, other.student);
    }

}
