/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis.listen;

import java.util.Set;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListe.Column;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListe.DataLine;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListe.Footnote;

/**
 *
 * @author boris.heithecker
 */
public interface ZensurenListe<L extends DataLine, F extends Footnote, C extends Column<F>> {

    String getListName();

    void setListName(String lname);

    String getListDate();

    void setListDate(String ldate);

    F addFootnote(String text);

    L addLine(String sName);

    C setValue(L line, int tier, Set<Marker> fach, Grade g, String ifGradeNull);

    C setValue(L line, int tier, String fach, Grade g, String ifGradeNull);

    interface DataLine {

        String getNote();

        String getStudentHint();

        void setNote(String note);

        void setStudentHint(String studentHint);

    }

    interface Column<F extends Footnote> {

        String getLevel();

        void setLevel(String level);

        void setFootnote(F lbl);

    }

    interface Footnote {

        String getValue();

        void setValue(String value);

        String getIndex();

    }
}
