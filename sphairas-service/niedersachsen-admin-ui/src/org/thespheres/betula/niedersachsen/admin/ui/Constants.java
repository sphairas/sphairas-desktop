/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui;

import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.niedersachsen.Ersatzeintrag;
import org.thespheres.betula.niedersachsen.Uebertrag;

/**
 *
 * @author boris.heithecker
 */
public class Constants {

    public static final String UNIT_NDS_ZEUGNIS_SETTINGS_MIME = "application/betula-unit-nds-zeugnis-settings";
    public static final String UNIT_NDS_ZEUGNIS_NOTES_MIME = "application/betula-unit-nds-zeugnis-notes";
    public static final Grade PENDING = GradeFactory.find(Ersatzeintrag.NAME, "pending");
    public static final Grade NE = GradeFactory.find(Ersatzeintrag.NAME, "ne");
    public static final Grade UEBERTRAG = GradeFactory.find(Uebertrag.NAME, "uebertrag");
    public static final Grade NB = GradeFactory.find(Ersatzeintrag.NAME, "nb");
    public static final Grade VORSCHLAG = GradeFactory.find("niedersachsen.avsvvorschlag", "vorschlag");

    private Constants() {
    }

}
