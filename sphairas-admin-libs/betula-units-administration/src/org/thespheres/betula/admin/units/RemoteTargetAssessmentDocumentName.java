/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.beans.PropertyChangeListener;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
public interface RemoteTargetAssessmentDocumentName extends Comparable<RemoteTargetAssessmentDocumentName> {

    public static final String PROP_DISPLAYNAME = "display-name";
    public static final String PROP_SEARCHEABLE_STRING = "searchabel.string";

    public String getDisplayName(Term current);

    public String getColumnLabel();

    //Immutable text as column tooltip cannot be updated
    public String getToolTipText();

    public String getSearchableString(Term c);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);
    
}
