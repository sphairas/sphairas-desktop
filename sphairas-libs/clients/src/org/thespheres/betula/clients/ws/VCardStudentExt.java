/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.ws;

import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.vcard.VCardStudent;

/**
 *
 * @author boris.heithecker
 */
class VCardStudentExt extends VCardStudent implements Student.PrimaryUnit {

    private UnitId pUnit;

    VCardStudentExt(StudentId id) {
        super(id);
    }

    @Override
    public UnitId getPrimaryUnit() {
        return pUnit;
    }

    void setPrimaryUnit(UnitId unit) {
        this.pUnit = unit;
    }
}
