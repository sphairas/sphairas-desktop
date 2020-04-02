/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui;

import java.io.IOException;
import org.thespheres.betula.services.ui.ks.BackupUtilImpl;
import org.w3c.dom.Document;

/**
 *
 * @author boris.heithecker
 */
public class BackupUtil {
    
    private static final BackupUtilImpl INSTANCE = new BackupUtilImpl();
    
    private BackupUtil() {
    }
    
    public static void cypherXMLDocument(Document document, boolean encrypt) throws IOException {
        INSTANCE.cypherXMLDocument(document, encrypt);
    }
}
