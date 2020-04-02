/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlTransient;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.model.Product;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractImportTarget implements ImportTarget {

    private final Product prod;
    protected final String provider;
    @XmlElement(name = "authority")
    protected String authority;
    protected String namingProviderUrl;
    @XmlTransient
    private NamingResolver namingServiceProvider;
    protected String webServiceProviderUrl;
    @XmlTransient
    private WebServiceProvider webServiceProvider;
    protected String termSchemeProviderUrl;
    protected String termSchemeId;
    @XmlTransient
    private SchemeProvider termSchemeProvider;
    @XmlList
    @XmlElement(name = "assessment-conventions")
    protected String[] assessmentConventionNames;
    @XmlList
    @XmlElement(name = "lesson-type-conventions")
    protected String[] realmConventions;
    @XmlList
    @XmlElement(name = "student-career-conventions")
    protected String[] studentCareerConventions;
    @XmlList
    @XmlElement(name = "subject-conventions")
    protected String[] subjectConventions;
    @XmlElement(name = "source-targets-links-webdav-url")
    protected String sourceTargetLinksWebDavUrl;
    @XmlElement(name = "source-targets-links-config-file")
    protected String sourceTargetLinksConfigFile;

    protected AbstractImportTarget(String provider, Product prod) {
        this.provider = provider;
        this.prod = prod;
    }

    @Override
    public final Product getProduct() {
        return prod;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return ProviderRegistry.getDefault().get(provider);
    }

    @Override
    public NamingResolver getNamingResolver() {
        if (namingServiceProvider == null) {
            namingServiceProvider = NamingResolver.find(getNamingProviderUrl());
        }
        return namingServiceProvider;
    }

    @Override
    public SchemeProvider getTermSchemeProvider() {
        if (termSchemeProvider == null) {
            termSchemeProvider = SchemeProvider.find(getTermSchemeProviderUrl());
        }
        return termSchemeProvider;
    }

    @Override
    public WebServiceProvider getWebServiceProvider() {
        if (webServiceProvider == null) {
            webServiceProvider = WebProvider.find(getWebProviderUrl(), WebServiceProvider.class);
        }
        return webServiceProvider;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    protected String getNamingProviderUrl() {
        return namingProviderUrl != null ? namingProviderUrl : getProviderInfo().getURL();
    }

    protected String getWebProviderUrl() {
        return webServiceProviderUrl != null ? webServiceProviderUrl : getProviderInfo().getURL();
    }

    protected String getTermSchemeProviderUrl() {
        return termSchemeProviderUrl != null ? termSchemeProviderUrl : getProviderInfo().getURL();
    }

    public String getTermSchemeId() {
        return termSchemeId;
    }

    public String[] getAssessmentConventionNames() {
        return assessmentConventionNames;
    }

    public AssessmentConvention[] getAssessmentConventions() {
        String[] n = getAssessmentConventionNames();
        if (n != null) {
            return Arrays.stream(n)
                    .filter(s -> !s.trim().isEmpty())
                    .map(GradeFactory::findConvention)
                    .filter(Objects::nonNull)
                    .toArray(AssessmentConvention[]::new);
        } else {
            return new AssessmentConvention[0];
        }
    }

    public String[] getRealmMarkerConventionNames() {
        return realmConventions;
    }

    public String[] getSubjectMarkerConventionNames() {
        return subjectConventions;
    }

    public String[] getStudentCareerConventionNames() {
        return studentCareerConventions;
    }

    public MarkerConvention[] getRealmMarkerConventions() {
        String[] n = getRealmMarkerConventionNames();
        if (n != null) {
            return Arrays.stream(n)
                    .filter(s -> !s.trim().isEmpty())
                    .map(MarkerFactory::findConvention)
                    .filter(Objects::nonNull)
                    .toArray(MarkerConvention[]::new);
        } else {
            return new MarkerConvention[0];
        }
    }

    public MarkerConvention[] getSubjectMarkerConventions() {
        String[] n = getSubjectMarkerConventionNames();
        if (n != null) {
            return Arrays.stream(n)
                    .filter(s -> !s.trim().isEmpty())
                    .map(MarkerFactory::findConvention)
                    .filter(Objects::nonNull)
                    .toArray(MarkerConvention[]::new);
        } else {
            return new MarkerConvention[0];
        }
    }

    public MarkerConvention[] getStudentCareerConventions() {
        String[] n = getStudentCareerConventionNames();
        if (n != null) {
            return Arrays.stream(n)
                    .filter(s -> !s.trim().isEmpty())
                    .map(MarkerFactory::findConvention)
                    .filter(Objects::nonNull)
                    .toArray(MarkerConvention[]::new);
        } else {
            return new MarkerConvention[0];
        }
    }

    @Override
    public String getSourceTargetLinksWebDavUrl(Term term) throws IOException {
        return sourceTargetLinksWebDavUrl;
    }

    @Override
    public String getSourceTargetLinksConfigFile(Term term) {
        return sourceTargetLinksConfigFile;
    }

}
