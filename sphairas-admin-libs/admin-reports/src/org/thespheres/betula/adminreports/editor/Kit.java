/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.editor;

import org.thespheres.betula.adminreports.impl.RemoteReportsSupport;
import org.netbeans.modules.editor.NbEditorKit;

/**
 *
 * @author boris.heithecker
 */
public class Kit extends NbEditorKit {

    @Override
    public String getContentType() {
//        return RemoteReportsDescriptorFileDataObject.FILE_MIME;
        return RemoteReportsSupport.EDITOR_MIME;
    }
}
