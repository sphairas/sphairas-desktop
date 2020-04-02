/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ws;

import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.ProviderInfo;

/**
 *
 * @author boris.heithecker
 */
public interface CommonDocuments {

    public static final String STUDENT_CAREERS_DOCID = "student-bildungsgang-documentid";
    public static final String COMMON_NAMES_DOCID = "common-names-documentid";
    public static final String SUBJECT_NAMES_DOCID = "subject-names-documentid";
    
    public ProviderInfo getProviderInfo();

    public DocumentId forName(String name);
}
