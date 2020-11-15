/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.services.CommonTargetProperties;
import org.thespheres.betula.document.model.DocumentDefaults;
import org.thespheres.betula.xmlimport.model.XmlTargetImportSettings;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.ImportTargetFactory;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.Product;

/**
 *
 * @author boris.heithecker
 */
public class ConfigurableImportTarget extends AbstractImportTarget implements ImportTarget, CommonTargetProperties, DocumentDefaults<Grade, TargetDocument> {

    private final static JAXBContext JAXB;
    private XmlTargetImportSettings defaultGrades;
    protected final Map<String, String> properties = new HashMap<>();
    private final DocumentsModel dtdb = new DocumentsModel();

    static {
        try {
            JAXB = JAXBContext.newInstance(XmlTargetImportSettings.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }
    private NameParserSettings nameParserSettings;

    public ConfigurableImportTarget(String provider, Product prod) {
        super(provider, prod);
    }

    public void initialize(Map<String, String> properties) {
        this.authority = properties.get("authority");
        this.termSchemeProviderUrl = properties.get("termSchedule.providerURL");
        this.termSchemeId = properties.getOrDefault("termSchemeId", TermSchedule.DEFAULT_SCHEME);
        this.namingProviderUrl = properties.get("naming.providerUrl");
        final String assessmentConvention = properties.get(LocalFileProperties.PROP_ASSESSMENT_CONVENTIONS);
        if (!StringUtils.isBlank(assessmentConvention)) {
            this.assessmentConventionNames = assessmentConvention.split(",");
        } else {
            this.assessmentConventionNames = new String[0];
        }
        final String subjects = properties.get(LocalFileProperties.PROP_UNIQUE_SUBJECT_CONVENTIONS);
        if (!StringUtils.isBlank(subjects)) {
            this.subjectConventions = subjects.split(",");
        } else {
            this.subjectConventions = new String[0];
        }
        final String realmMarker = properties.get(LocalFileProperties.PROP_REALM_CONVENTIONS);
        if (!StringUtils.isBlank(realmMarker)) {
            this.realmConventions = realmMarker.split(",");
        } else {
            this.realmConventions = new String[0];
        }
        dtdb.initialize(properties);
        //initialize defaultGrades: register layer file (overridable) load
        this.properties.putAll(properties);
    }

    @Override
    public ProviderInfo getProviderInfo() {
        ProviderInfo pi = super.getProviderInfo();
        return pi != null ? pi : getWebServiceProvider().getInfo();
    }

    public Units getUnits() {
        return Units.get(getWebProviderUrl()).orElseThrow(IllegalStateException::new);
    }

    @Override
    public TargetDocumentProperties[] createTargetDocuments(final ImportTargetsItem item) {
        final String type = StringUtils.capitalize(dtdb.getSuffix(item.getTargetDocumentIdBase()));
        class TDoc extends TargetDocumentProperties {

            private TDoc() {
                super(item.getTargetDocumentIdBase(), item.allMarkers(), type, item.getPreferredConvention(), item.getDeleteDate());
//                getProcessorHints().put("process-bulk", "true");
                getProcessorHints().put("update-pu-links", "true");
//                getSignees().put("entitled.signee", imp.getSignee());
            }

            @Override
            public Grade getDefaultGrade() {
                return null;
            }

            @Override
            public boolean isFragment() {
                return false;
            }

        }
        return new TargetDocumentProperties[]{new TDoc()};
    }

    @Override
    public Grade getDefaultValue(DocumentId id, TargetDocument document) {
        if (defaultGrades == null) {
            final InputStream is = ConfigurableImportTarget.class.getResourceAsStream("defaultGrades.xml");
            try {
                defaultGrades = (XmlTargetImportSettings) JAXB.createUnmarshaller().unmarshal(is);
            } catch (JAXBException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return defaultGrades.getDefaultValue(id, document);
    }

    public String getDefaultSigneeSuffix() {
        return properties.get("signee.suffix");
    }

    public String getStudentsAuthority(Object... params) {
        return properties.get("students.authority");
    }

    public NameParserSettings getNameParserSettings() {
        if (nameParserSettings == null) {
            nameParserSettings = new NameParserSettings(properties);
        }
        return nameParserSettings;
    }

    public static ConfigurableImportTarget find(final String url) {

        return Lookup.getDefault().lookupAll(ImportTargetFactory.class).stream()
                .filter(ConfigurableImportTarget.Factory.class::isInstance)
                .map(ConfigurableImportTarget.Factory.class::cast)
                .filter(sbit -> sbit.getProduct() == null || sbit.getProduct().equals(Product.NO))
                .flatMap(sbit -> sbit.available(ConfigurableImportTarget.class).stream())
                .filter(p -> p.getProvider().equals(url))
                .collect(CollectionUtil.requireSingleton())
                .map(sbit -> {
                    try {
                        return sbit.createInstance();
                    } catch (IOException e) {
                        final String msg = NbBundle.getMessage(ImportTargetFactory.class, "ImportTargetFactory.createInstance.exception.message", url, Product.NO.getDisplay());
                        PlatformUtil.getCodeNameBaseLogger(ImportTargetFactory.class).log(Level.SEVERE, msg, e);
                        return null;
                    }
                })
                .orElseThrow(() -> new NoProviderException(ConfigurableImportTarget.Factory.class, url));
    }

    public static abstract class Factory extends ImportTargetFactory<ConfigurableImportTarget> {

        protected Factory() {
            super(Product.NO, ConfigurableImportTarget.class);
        }

    }

}
