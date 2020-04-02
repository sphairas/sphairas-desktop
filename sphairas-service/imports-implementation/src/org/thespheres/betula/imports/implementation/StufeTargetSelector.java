/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.XmlTargetSelector;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
@JAXBUtil.JAXBRegistrations({
    @JAXBUtil.JAXBRegistration(target = "XmlTargetImportSettings"),
    @JAXBUtil.JAXBRegistration(target = "XmlTargetProcessorHintsSettings")})
@XmlRootElement(name = "stufe-target-selector") //, namespace = "http://www.thespheres.org/xsd/betula/target-settings-defaults.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class StufeTargetSelector extends XmlTargetSelector {

    @XmlList
    @XmlAttribute(name = "stufe", required = true)
    private int[] stufe;

    public StufeTargetSelector() {
        super();
    }

    public StufeTargetSelector(String id) {
        super(id);
    }

    public int[] getStufe() {
        return stufe;
    }

    public void setStufe(int[] stufe) {
        this.stufe = stufe;
    }

    @Override
    public boolean applies(ImportTargetsItem item) {
        final Object up = item.getClientProperty(ImportItem.PROP_IMPORT_TARGET);
        final Object tp = item.getClientProperty(ImportTargetsItem.PROP_SELECTED_TERM);
        if (up instanceof ConfigurableImportTarget && tp instanceof Term) {
            final ConfigurableImportTarget configuration = (ConfigurableImportTarget) up;
            final NamingResolver nr = configuration.getNamingResolver();
            final DocumentId target = item.getTargetDocumentIdBase();
            if (nr != null && target != null) {
                final NamingResolver.Result resolve;
                try {
                    resolve = nr.resolveDisplayNameResult(target);
                    resolve.addResolverHint("naming.only.level");
                    final String result = resolve.getResolvedName((Term) tp);
                    final Integer i = Integer.parseUnsignedInt(result);
                    return Arrays.stream(getStufe()).anyMatch(i::equals);
                } catch (IllegalAuthorityException | NumberFormatException ex) {
                    Logger.getLogger(StufeTargetSelector.class.getName()).log(Level.FINE, ex.getLocalizedMessage(), ex);
                }
            }
        }
        return false;
    }

}
