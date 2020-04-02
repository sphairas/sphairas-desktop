/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import javax.swing.table.TableModel;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
public interface TargetsElementModel extends TableModel {

    public static final String TABLE_PROP_CURRENT_TERMID = "current.term.id";

    public RemoteTargetAssessmentDocument getRemoteTargetAssessmentDocumentAtColumnIndex(int index);

    public RemoteTargetAssessmentDocument getRemoteTargetAssessmentDocumentAtListIndex(int index);

    public RemoteTargetAssessmentDocument getRemoteTargetAssessmentDocumentForDocumentId(final DocumentId id);

    public int getRemoteTargetAssessmentDocumentsSize();

    public Term getCurrentIndentity();

}
