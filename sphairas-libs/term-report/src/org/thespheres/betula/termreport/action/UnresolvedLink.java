/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.action;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Optional;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.thespheres.betula.termreport.model.XmlFileTargetAssessmentProvider;
import org.thespheres.betula.termreport.xml.XmlFileTargetAssessmentReference;

/**
 *
 * @author boris.heithecker
 */
@Messages({"UnresolvedLink.noRelativeUri.displayName=Kein Datei-Link"})
public class UnresolvedLink {
    
    private final XmlFileTargetAssessmentReference reference;
    private final XmlFileTargetAssessmentProvider provider;
    
    public UnresolvedLink(XmlFileTargetAssessmentReference reference, XmlFileTargetAssessmentProvider provider) {
        this.reference = reference;
        this.provider = provider;
    }
    
    public File getBaseDir() {
        return Optional.ofNullable(provider.getEnvironment().getContextLookup().lookup(DataObject.class))
                .map(dob -> FileOwnerQuery.getOwner(dob.getPrimaryFile()))
                .map(prj -> prj.getProjectDirectory())
                .map(fo -> FileUtil.toFile(fo))
                .orElse(null);
    }
    
    public void resolve(File open) throws IOException {
        provider.restoreLink(Utilities.toURI(open));
    }
    
    public String getDisplayName() {
        URI ruri = reference.getRelativeUri();
        if (ruri != null) {
            final String sv = reference.getRelativeUri().toString();
            try {
                return URLDecoder.decode(sv, "utf-8");
            } catch (UnsupportedEncodingException ex) {
                return sv;
            }
        }
        return NbBundle.getMessage(UnresolvedLink.class, "UnresolvedLink.noRelativeUri.displayName");
    }
    
}
