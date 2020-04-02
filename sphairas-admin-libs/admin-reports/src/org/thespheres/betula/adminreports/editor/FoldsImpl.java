/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.editor;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldTypeProvider;
import org.thespheres.betula.reports.ReportFoldManager;

/**
 *
 * @author boris.heithecker
 */
class FoldsImpl extends ReportFoldManager {

    @MimeRegistrations({
        @MimeRegistration(mimeType = "text/betula-remote-reports", service = FoldManagerFactory.class),
        @MimeRegistration(mimeType = "text/betula-remote-reports", service = FoldTypeProvider.class)})
    public static class Factory implements FoldManagerFactory, FoldTypeProvider {

        @Override
        public FoldManager createFoldManager() {
            return new FoldsImpl();
        }

        @Override
        public Collection getValues(Class type) {
            FoldType ft = FoldType.COMMENT;
            return Collections.singleton(ft);
        }

        @Override
        public boolean inheritable() {
            return true;
        }
    }
}
