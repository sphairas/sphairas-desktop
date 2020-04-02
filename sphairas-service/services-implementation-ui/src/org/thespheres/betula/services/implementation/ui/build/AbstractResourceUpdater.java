/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.build;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Icon;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.thespheres.betula.adminconfig.ConfigurationBuildTask;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.dav.ActiveLock;
import org.thespheres.betula.services.dav.LockDiscovery;
import org.thespheres.betula.services.dav.LockInfo;
import org.thespheres.betula.services.dav.LockScope;
import org.thespheres.betula.services.dav.LockType;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;
import org.thespheres.betula.services.ui.util.LockSupport;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.util.MessageUtil;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
abstract class AbstractResourceUpdater implements Runnable, ConfigurationBuildTask {

    protected final SyncedProviderInstance instance;
    protected final WebProvider.SSL web;
    protected final String resources;
    private String currentLock;
    private final String base;

    protected AbstractResourceUpdater(final SyncedProviderInstance instance, final String resource, String providedLock) {
        this.instance = instance;
        this.resources = resource;
        this.currentLock = providedLock;
        web = instance.findWebProvider(WebProvider.SSL.class);
        base = URLs.adminResourcesDavBase(this.instance.findLocalFileProperties());
    }

    @Override
    public void run() {
        try {
            MessageUtil.suppressMessageDelivery(() -> {
                try {
                    runUpdates();
                } catch (IOException ex) {
                    notifyError(ex);
                }
                return null;
            });
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected abstract void runUpdates() throws IOException;

    protected void lock(final int seconds) throws IOException {
        final LockInfo lockInfo = new LockInfo();
        lockInfo.setLockScope(LockScope.createExclusive());
        lockInfo.setLockType(LockType.createWrite());
        LockDiscovery ld = LockSupport.lock(web, URI.create(base), lockInfo, seconds);
        final ActiveLock al = ld.getActiveLock().stream().collect(CollectionUtil.singleOrNull());
        if (al != null) {
            currentLock = al.getLockToken().getHref().stream().collect(CollectionUtil.singleOrNull());
        } else {
            currentLock = null;
            throw new IOException("Could not lock " + base);
        }
    }

    protected void unlock() throws IOException {
        if (currentLock != null) {
            LockSupport.unlock(web, URI.create(base), currentLock);
        }
    }

    @Override
    public String getLockToken() {
        return currentLock;
    }

    @Override
    public Path getProviderBasePath() {
        return ServiceConstants.providerConfigBase(instance.getProvider());
    }

    @Override
    public URI resolveResource(String resource) {
        final String url = base + resource;
        return URI.create(url);
    }

    @Override
    public WebProvider getWebProvider() {
        return web;
    }

    @Override
    public void setLastModified(String resource, String lm) {
        try {
            instance.setLastModified(resource, lm);
        } catch (IOException ex) {
            cancel(ex);
        }
    }

    @Override
    public byte[] getBytes(final List<String> list) {
        return list.stream()
                .collect(Collectors.joining("\n"))
                .getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void cancel(Exception ex) {
        throw new ExceptionHolder(ex);
    }

    @NbBundle.Messages({"AbstractResourceUpdater.error.title=Konfigurationsfehler",
        "AbstractResourceUpdater.error.message=Beim Konfigurieren der Resource(n) {0} des Mandanten {1} ist ein Ein-Ausgabe-Fehler entstanden."})
    void notifyError(Exception ex) {
        PlatformUtil.getCodeNameBaseLogger(AbstractResourceUpdater.class).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(AbstractResourceUpdater.class, "AbstractResourceUpdater.error.title");
        final String message = NbBundle.getMessage(AbstractResourceUpdater.class, "AbstractResourceUpdater.error.message", resources, instance.getProvider());
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    static class ExceptionHolder extends RuntimeException {

        final Exception original;

        ExceptionHolder(Exception ex) {
            this.original = ex;
        }
    }
}
