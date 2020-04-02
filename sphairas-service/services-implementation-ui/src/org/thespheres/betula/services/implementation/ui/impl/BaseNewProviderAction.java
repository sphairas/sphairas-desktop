/*
 * To change this license header, choose License Headers in Project AppProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import org.thespheres.betula.services.ui.util.dav.URLs;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.swing.Icon;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.EditableProperties;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.services.AppPropertyNames;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.web.SSLUtil;

/**
 *
 * @author boris.heithecker
 */
public class BaseNewProviderAction {

    protected void newProvider(final String host, final String certName) throws IOException {
        final String base = NbBundle.getMessage(URLs.class, "resources.dav.base.url", host);
        final Properties prop = fetchProperties(base, certName);
        String name = retrieveProviderName(host, certName);
        if (StringUtils.isEmpty(name)) {
            name = prop.getProperty("providerURL");
        }
        final String provider = name;
        if (StringUtils.isEmpty(provider)) {
            throw new IOException("No providerURL property found.");
        }
        if (SyncedProviderInstance.getInstances().containsKey(provider)) {
            notifyProviderExists(provider);
            return;
        }
        final Path dir = Files.createDirectories(ServiceConstants.providerConfigBase(provider));
        Files.write(dir.resolve("provider"), Lists.newArrayList(provider), StandardCharsets.UTF_8);
        final EditableProperties uprops = new EditableProperties(false);
        uprops.put(URLs.HOST, host);
        uprops.put(URLs.ADMINURLS, Boolean.TRUE.toString());
        uprops.put(AppPropertyNames.LP_CERTIFICATE_NAME, certName);
        uprops.store(Files.newOutputStream(dir.resolve("user.properties")));
        prop.store(Files.newOutputStream(dir.resolve("default.properties")), null);
        copyLayer(base, certName, dir);
        SyncedProviderInstance.add(provider, dir, true);
//        SyncedProviderInstance.getInstances().get(provider).enqueue(1500);
    }

    String retrieveProviderName(final String host, final String certName) throws IOException {
        final String url = NbBundle.getMessage(URLs.class, "provider.name.info", host);
        final URI uri = URI.create(url);
        final SSLContext ssl = SSLUtil.createSSLContext(certName);
        class Access implements WebProvider.SSL {

            @Override
            public SSLContext getSSLContext() {
                return ssl;
            }

            @Override
            public RequestProcessor getDefaultRequestProcessor() {
                throw new UnsupportedOperationException("Not called.");
            }

            @Override
            public ProviderInfo getInfo() {
                throw new UnsupportedOperationException("Not called.");
            }

        }
        return HttpUtilities.get(new Access(), uri, null, false);
    }

    void copyLayer(final String base, final String certName, final Path dir) throws IOException {
        final String url = base + "layer.xml";
        final URI uri = URI.create(url);
        final SSLContext ssl = SSLUtil.createSSLContext(certName);
        class Access implements WebProvider.SSL {

            @Override
            public SSLContext getSSLContext() {
                return ssl;
            }

            @Override
            public RequestProcessor getDefaultRequestProcessor() {
                throw new UnsupportedOperationException("Not called.");
            }

            @Override
            public ProviderInfo getInfo() {
                throw new UnsupportedOperationException("Not called.");
            }

        }
        HttpUtilities.get(new Access(), uri, (lm, is) -> Files.copy(is, dir.resolve("layer.xml"), StandardCopyOption.REPLACE_EXISTING), null, false);
        LayerProv.fireUpdate();
    }

    Properties fetchProperties(final String base, final String certName) throws IOException {
        final String url = base + "default.properties";
        final URI uri = URI.create(url);
        final SSLContext ssl = SSLUtil.createSSLContext(certName);
        class Access implements WebProvider.SSL {

            @Override
            public SSLContext getSSLContext() {
                return ssl;
            }

            @Override
            public RequestProcessor getDefaultRequestProcessor() {
                throw new UnsupportedOperationException("Not called.");
            }

            @Override
            public ProviderInfo getInfo() {
                throw new UnsupportedOperationException("Not called.");
            }

        }
        final Properties ret = HttpUtilities.get(new Access(), uri, (lm, is) -> {
            final Properties p = new Properties();
            try {
                p.load(is);
            } catch (IOException ex) {
                return null;
            } finally {
                is.close();
            }
            return p;
        }, null, false);
        return ret;
    }

    @NbBundle.Messages(value = {"BaseNewProviderAction.notifyProviderExists.title=Mandatenregistrierung",
        "BaseNewProviderAction.notifyProviderExists.message=Der Mandant „{0}“ ist bereits registriert und kann nicht ein zweites Mal angelegt werden."})
    private static void notifyProviderExists(final String provider) {
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(BaseNewProviderAction.class, "BaseNewProviderAction.notifyProviderExists.title");
        final String message = NbBundle.getMessage(BaseNewProviderAction.class, "BaseNewProviderAction.notifyProviderExists.message", provider);
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.INFO);
    }
}
