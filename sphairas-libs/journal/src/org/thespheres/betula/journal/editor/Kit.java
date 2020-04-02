/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.editor;

import org.netbeans.modules.editor.NbEditorKit;
import org.thespheres.betula.journal.module.Constants;

/**
 *
 * @author boris.heithecker
 */
public class Kit extends NbEditorKit {

    @Override
    public String getContentType() {
        return Constants.JOURNAL_MINUTES_EDITOR_MIME;
    }
}
