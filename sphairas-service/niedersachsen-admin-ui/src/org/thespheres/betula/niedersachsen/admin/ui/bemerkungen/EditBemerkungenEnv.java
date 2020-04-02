/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import com.google.common.collect.Sets;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.util.AbstractReloadableMarkerConvention;

/**
 *
 * @author boris.heithecker
 */
@Messages("EditBemerkungenEnv.displayName=Zeungis-Bemerkungen für {0}")
class EditBemerkungenEnv implements Runnable {

    private static final String DEFAULT_TEMPLATE_PATH = "signee/bemerkungen.xml";
    private static final String DEFAULT_PROPERTIES_PATH = "signee/custom-report-notes.properties";
    private final String provider;
    private TermReportNoteSetTemplate template;
    private EditableProperties texte;
    private final static Map<String, WeakReference<EditBemerkungenEnv>> MAP = new HashMap<>();
    final RequestProcessor.Task init;
    final String urlBase;
    final RequestProcessor RP = new RequestProcessor();
    private final String[] customNotesConvention;
    private NodeDelegate node;
    private final String displayName;
    private final Set<String> modified = Sets.newHashSet();
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    public static final String PROP_MODIFIED = "modified";
    private EditBemerkungenOpenSupport openSupport;
    private final Lookup lookup;

    @SuppressWarnings(value = {"LeakingThisInConstructor"})
    private EditBemerkungenEnv(String provider, String base, String[] customNotesConvention) {
        this.provider = provider;
        final ProviderInfo pi = ProviderRegistry.getDefault().get(provider);
        final String name = pi != null ? pi.getDisplayName() : provider;
        displayName = NbBundle.getMessage(EditBemerkungenEnv.class, "EditBemerkungenEnv.displayName", name);
        this.urlBase = base;
        this.customNotesConvention = customNotesConvention;
        this.openSupport = new EditBemerkungenOpenSupport(provider);
        this.lookup = Lookups.fixed(this, openSupport);
        this.init = RP.post(this);
    }

    static EditBemerkungenEnv find(final String provider) throws IOException {
        synchronized (MAP) {
            final EditBemerkungenEnv wr = Optional.ofNullable(MAP.get(provider))
                    .map(WeakReference::get)
                    .orElse(null);
            if (wr == null) {
                final EditBemerkungenEnv b = createImpl(provider);
                MAP.put(provider, new WeakReference<>(b));
                return b;
            } else {
                return wr;
            }
        }
    }

    public String getProvider() {
        return provider;
    }

    public TermReportNoteSetTemplate getTemplate() {
        if (!init.isFinished()) {
            init.waitFinished();
        }
        return template;
    }

    public EditableProperties getProperties() {
        if (!init.isFinished()) {
            init.waitFinished();
        }
        return texte;
    }

    private static EditBemerkungenEnv createImpl(final String provider) throws IOException {
        final LocalFileProperties lp = LocalFileProperties.find(provider);
        if (lp != null) {
            final String customNotesConvention = lp.getProperty("custom.report.notes.conventions", "");
            final String[] arr = Arrays.stream(customNotesConvention.split(","))
                    .map(StringUtils::trimToNull)
                    .filter(Objects::nonNull)
                    .toArray(String[]::new);
            final String base = URLs.adminResourcesDavBase(lp);
            final EditBemerkungenEnv ret = new EditBemerkungenEnv(provider, base, arr);
            return ret;
        }
        throw new IOException("LocalFileProperties not found for: " + provider);
    }

    void setModified(final String modif) {
        final boolean changed;
        synchronized (modified) {
            changed = modified.add(modif);
        }
        if (changed) {
            pSupport.firePropertyChange(PROP_MODIFIED, false, true);
        }
    }

    boolean isModified(final String modif) {
        synchronized (modified) {
            return modif != null ? modified.contains(modif) : !modified.isEmpty();
        }
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private String getTemplatePath() {
        return LocalProperties.find(provider).getProperty("report.notes.template.path", DEFAULT_TEMPLATE_PATH);
    }

    private String getPropertiesPath() {
        return LocalProperties.find(provider).getProperty("custon.report.notes.path", DEFAULT_PROPERTIES_PATH);
    }

    private void runImpl() throws IOException {
        final WebProvider service = WebProvider.find(provider, WebProvider.class);
        final EditableProperties eprops = HttpUtil.fetchResourceBundle(service, urlBase + getPropertiesPath());
        final TermReportNoteSetTemplate trt = HttpUtil.fetchTemplate(service, urlBase + getTemplatePath());
        if (eprops != null) {
            this.texte = eprops;
        } else {
            this.texte = new EditableProperties(false);
        }
        this.template = trt;
    }

    public RequestProcessor.Task saveProperties() {
        return RP.post(this::saveImpl);
    }

    private void saveImpl() {
        final WebProvider service = WebProvider.find(provider, WebProvider.class);
        try {
            HttpUtil.storeResourceBundle(service, urlBase + getPropertiesPath(), texte);
            requestUpdateCustomConvention();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void requestUpdateCustomConvention() {
        Arrays.stream(customNotesConvention)
                .map(MarkerFactory::findConvention)
                .filter(AbstractReloadableMarkerConvention.class::isInstance)
                .map(AbstractReloadableMarkerConvention.class::cast)
                .forEach(AbstractReloadableMarkerConvention::markForReload);
    }

    public RequestProcessor.Task saveTemplate() {
        return RP.post(this::saveTemplateImpl);
    }

    private void saveTemplateImpl() {
        final WebProvider service = WebProvider.find(provider, WebProvider.class);
        try {
            HttpUtil.storeTemplate(service, urlBase + getTemplatePath(), template);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    synchronized Node getNodeDelegate() {
        if (node == null) {
            node = new NodeDelegate();
        }
        return node;
    }

    public Lookup getLookup() {
        return lookup;
    }

    EditBemerkungenOpenSupport getOpenSupport() {
        return openSupport;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

    private class NodeDelegate extends AbstractNode {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        NodeDelegate() {
            super(Children.LEAF, EditBemerkungenEnv.this.getLookup());
            setName(EditBemerkungenEnv.class.getName() + ":" + provider);
            setDisplayName(displayName);
            setIconBaseWithExtension("org/thespheres/betula/niedersachsen/admin/ui/resources/blue-document-sticky-note.png");
        }

    }
}
