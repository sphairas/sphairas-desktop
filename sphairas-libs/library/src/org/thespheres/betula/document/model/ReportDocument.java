/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Document;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 * @param <S> The subject class
 */
public interface ReportDocument<S> extends Document {

    public DocumentId getDocumentId();

    public String getDisplayLabel();

    public Set<S> getSubjects();

    public Grade select(S subject);

    public Map<String, SigneeInfo> getSigneeInfos(); //;Jahrgangsl., Klassenlehre, SL
    //AV, SV, fehltage, entschuldigt etc

    public <C> C getProperty(String name, Class<C> type, C defaultValue) throws IOException;

    //Null if unspecified
    public TermId getTerm();

    //Null if unspecified
    public LocalDate getReportDate();

}
