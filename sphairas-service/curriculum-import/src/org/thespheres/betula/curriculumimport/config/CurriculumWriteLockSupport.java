/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.config;

import com.google.common.net.UrlEscapers;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.adminconfig.ConfigurationBuilder;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.dav.ActiveLock;
import org.thespheres.betula.services.dav.LockDiscovery;
import org.thespheres.betula.services.dav.LockInfo;
import org.thespheres.betula.services.dav.LockScope;
import org.thespheres.betula.services.dav.LockType;
import org.thespheres.betula.services.dav.Multistatus;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.LockSupport;
import org.thespheres.betula.services.ui.util.MultistatusUtilities;
import org.thespheres.betula.services.ui.util.WriteLockCapability;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class CurriculumWriteLockSupport {

    private final DataObject file;
    private final String resource;
    private final String provider;
    private final FCL fcl = new FCL();
    private final PCL pcl = new PCL();
    private final URI uri;
    private final RequestProcessor rp = new RequestProcessor();
    private final RequestProcessor.Task relock;
    private final WebProvider wp;
    private String currentLock;

    CurriculumWriteLockSupport(final DataObject xmldo, final String resource, final String provider) {
        this.file = xmldo;
        this.resource = resource;
        this.provider = provider;
        final String base = URLs.adminResourcesDavBase(LocalProperties.find(provider));
        final String url = base + UrlEscapers.urlFragmentEscaper().escape(resource);
        uri = URI.create(url);
        wp = WebProvider.find(provider, WebProvider.class);
        relock = rp.create(this::refreshIfActive);
        this.file.getPrimaryFile().addFileChangeListener(fcl);
    }

    public static CurriculumWriteLockSupport create(final DataObject xmldo) {
        final WriteLockCapability wlc = xmldo.getLookup().lookup(WriteLockCapability.class);
        if (wlc != null) {
            final File jf = FileUtil.toFile(xmldo.getPrimaryFile());
            String provider = null;
            String resource = null;
            if (jf != null) {
                provider = ServiceConstants.findProviderName(jf.toPath());
                final Path pr = ServiceConstants.relativizeToProviderBase(jf.toPath());
                if (pr != null) {
                    resource = pr.toString();
                }
            }
            if (resource != null && provider != null) {
                final CurriculumWriteLockSupport ret = new CurriculumWriteLockSupport(xmldo, resource, provider);
                wlc.addVetoableChangeListener(ret.pcl);
                return ret;
            }
        }
        return null;
    }

    private void lock() throws IOException {
        final LockInfo lockInfo = new LockInfo();
        lockInfo.setLockScope(LockScope.createExclusive());
        lockInfo.setLockType(LockType.createWrite());
        final LockDiscovery ld = LockSupport.lock(wp, uri, lockInfo, 35);
        final ActiveLock al = ld.getActiveLock().stream().collect(CollectionUtil.singleOrNull());
        if (al != null) {
            currentLock = al.getLockToken().getHref().stream().collect(CollectionUtil.singleOrNull());
        } else {
            currentLock = null;
            throw new IOException("Could not lock " + uri.toString());
        }
        relock.schedule(20 * 1000);
    }

    private void refreshIfActive() {
        if (currentLock != null) {
            if (file.isModified() && file.isValid()) {
                try {
                    final LockDiscovery ld = LockSupport.refreshLock(wp, uri, currentLock, 35);
                    final ActiveLock al = ld.getActiveLock().stream().collect(CollectionUtil.singleOrNull());
                    if (al != null) {
                        currentLock = al.getLockToken().getHref().stream().collect(CollectionUtil.singleOrNull());
                    } else {
                        currentLock = null;
                        throw new IOException("Could not refresh lock " + uri.toString());
                    }
                } catch (IOException ioex) {
                }
            } else if (currentLock != null) {
                try {
                    LockSupport.unlock(wp, uri, currentLock);
                } catch (IOException ioex) {
                } finally {
                    currentLock = null;
                }
            }
        }
    }

    private void upload(final byte[] bytes) {
        final ConfigurationBuilder builder = ConfigurationBuilder.find(provider);
        if (builder != null) {
            builder.buildResources(resource, cbt -> {
                try {
                    final String encoded = UrlEscapers.urlFragmentEscaper().escape(resource);
                    HttpUtilities.put(cbt.getWebProvider(), cbt.resolveResource(encoded), bytes, null, cbt.getLockToken());
                    final Multistatus prop = HttpUtilities.getProperties(cbt.getWebProvider(), cbt.resolveResource(encoded), 1, true);
                    final String lm = MultistatusUtilities.findLastModified(prop);
                    cbt.setLastModified(resource, lm);
                } catch (IOException ex) {
                    cbt.cancel(ex);
                    notify(ex);
                }

            }, currentLock);
        }
        relock.schedule(6 * 1000);
    }

    private void delete() {
        final String rs = CurriculumConfigNodeList.PROVIDER_FILE_LIST_NAME + "," + resource;
        final ConfigurationBuilder builder = ConfigurationBuilder.find(provider);
        if (builder != null) {
            builder.buildResources(rs, cbt -> {
                try {
                    final Path res = cbt.getProviderBasePath().resolve(CurriculumConfigNodeList.PROVIDER_FILE_LIST_NAME);
                    if (Files.exists(res)) {
                        final List<String> list = Files.readAllLines(res, StandardCharsets.UTF_8);
                        final ListIterator<String> it = list.listIterator();
                        while (it.hasNext()) {
                            final String l = StringUtils.trim(it.next());
                            if (resource.equals(l)) {
                                final String replace = "#" + l + " removed: " + LocalDateTime.now().toString();
                                it.set(replace);
                                break;
                            }
                        }
                        HttpUtilities.put(cbt.getWebProvider(), cbt.resolveResource(CurriculumConfigNodeList.PROVIDER_FILE_LIST_NAME), cbt.getBytes(list), null, cbt.getLockToken());
                    }
                } catch (IOException ex) {
                    cbt.cancel(ex);
                    notify(ex);
                }
            });
            builder.buildResources(rs, cbt -> {
                try {
                    final String encoded = UrlEscapers.urlFragmentEscaper().escape(resource);
                    HttpUtilities.delete(cbt.getWebProvider(), cbt.resolveResource(encoded), cbt.getLockToken());
                    relock.cancel();
                } catch (IOException ex) {
                    cbt.cancel(ex);
                    notify(ex);
                }
            }, currentLock);
        }
    }

    @NbBundle.Messages({
        "CurriculumWriteLockSupport.Exception.error.title=Speicherfehler",
        "CurriculumWriteLockSupport.Exception.error.message=Beim Speichern von {0} ist ein Fehler aufgetreten."})
    protected void notify(final Exception ex) {
        Logger.getLogger(CurriculumWriteLockSupport.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
        Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        String title = NbBundle.getMessage(CurriculumWriteLockSupport.class, "CurriculumWriteLockSupport.Exception.error.title");
        String message = NbBundle.getMessage(CurriculumWriteLockSupport.class, "CurriculumWriteLockSupport.Exception.error.message", file.getName());
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    private final class FCL extends FileChangeAdapter {

        @Override
        public void fileChanged(final FileEvent fe) {
            fe.runWhenDeliveryOver(() -> {
                try {
                    final byte[] array = IOUtils.toByteArray(file.getPrimaryFile().getInputStream());
                    upload(array);
                } catch (IOException ex) {
                    CurriculumWriteLockSupport.this.notify(ex);
                }
            });
        }

        @Override
        public void fileDeleted(final FileEvent fe) {
            fe.runWhenDeliveryOver(() -> delete());
        }
    }

    private final class PCL implements VetoableChangeListener {

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (WriteLockCapability.PROP_LOCK_REQUEST.equals(evt.getPropertyName()) && evt.getNewValue().equals(Boolean.TRUE)) {
                try {
                    lock();
                } catch (IOException ex) {
                    throw new PropertyVetoException(ex.getLocalizedMessage(), evt);
                }
            }
        }

    }
}
