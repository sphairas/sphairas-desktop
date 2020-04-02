/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "remote-reports-descriptor")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteReportsDescriptor {

    @XmlElement(name = "provider", required = true) //, required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String provider;
    @XmlElement(name = "display")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String displayName;
    @XmlTransient
    private String resolvedDisplayName;
    @XmlElementWrapper(name = "remote-term-text-targets")
    @XmlElement(name = "remote-term-text-target")
    private DocumentId[] targetSet;
    private final static Pattern DISPLAY_NAME_PATTERN = Pattern.compile("#\\{[\\w]+(-[\\w]+)*\\}", Pattern.UNICODE_CHARACTER_CLASS);

    public String getProvider() {
        return provider;
    }
    
    public String getDisplayName() {
        if (resolvedDisplayName == null) {
            if (!StringUtils.isEmpty(displayName)) {
                StringBuffer sb = new StringBuffer(displayName.length());
                final Matcher m = DISPLAY_NAME_PATTERN.matcher(displayName);
                final DocumentId[] docs = getTargetSet();
                String authority = "";
                if (docs != null) {
                    authority = Arrays.stream(docs)
                            .map(d -> d.getAuthority())
                            .collect(Collectors.reducing((value, element) -> value == null || value.equals(element) ? element : ""))
                            .orElse("");
                }
                while (m.find()) {
                    final String text = m.group();
                    try {
                        final String gr = resolve(text.substring(2, text.length() - 1), authority);
                        m.appendReplacement(sb, Matcher.quoteReplacement(gr));
                    } catch (Exception exception) {
                        PlatformUtil.getCodeNameBaseLogger(RemoteReportsDescriptor.class).log(Level.INFO, "An error has occurred parsing " + text, exception);
                        m.appendReplacement(sb, Matcher.quoteReplacement(text));
                    }
                }
                m.appendTail(sb);
                resolvedDisplayName = sb.toString();
            }
        }
        return resolvedDisplayName;
    }

    private String resolve(final String gr, final String authority) throws IllegalAuthorityException {
        if (StringUtils.isBlank(authority)) {
            return gr;
        }
        final NamingResolver nr = NamingResolver.find(provider);
        final UnitId u = new UnitId(authority, gr);
        return nr.resolveDisplayName(u);
    }

    public DocumentId[] getTargetSet() {
        return targetSet;
    }

}
