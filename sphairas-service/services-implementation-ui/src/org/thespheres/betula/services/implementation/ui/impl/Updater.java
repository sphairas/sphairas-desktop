/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import com.google.common.net.UrlEscapers;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.Icon;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.implementation.ui.build.LayerUpdater;
import org.thespheres.betula.services.jms.AppResourceEvent;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@Messages({"updater.success=Vom Mandaten {0} wurden {1} Datei(en) synchronisiert.",
    "updater.success.log=Run update of provider {0}. {1} file(s) updated."})
class Updater implements Runnable {

    static final String LAST_MODIFIED_FILE = "last-modified";
    private final SyncedProviderInstance instance;
    final RequestProcessor rp;
    private final Map<String, String> lastModified = new HashMap<>();
    final RequestProcessor.Task task;

    @SuppressWarnings({"LeakingThisInConstructor"})
    Updater(SyncedProviderInstance instance) {
        this.instance = instance;
        this.rp = new RequestProcessor("Instance-Updater: " + instance.getProvider());
        rp.post(this::init);
        this.task = rp.create(this);
    }

    private void init() {
        final Path file = instance.getBaseDir().resolve(LAST_MODIFIED_FILE);
        if (!Files.exists(file)) {
            return;
        }
        final List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(Updater.class).log(Level.WARNING, "An exception has occurred reading last-modified of " + instance.getProvider(), ex);
            return;
        }
        final Map<String, String> m = lines.stream()
                .filter(l -> !StringUtils.isBlank(l))
                .map(l -> l.split("="))
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
        lastModified.putAll(m);
        //Cannot be initialized in constructor of SyncedProviderInstance
        final JMSTopicListenerService jms = JMSTopicListenerService.find(instance.getProvider(), JMSTopic.APP_RESOURCES_TOPIC.getJmsResource());
        jms.registerListener(AppResourceEvent.class, instance.jmsListener);
    }

    @NbBundle.Messages({"Updater.error.message=Bei Synchronisieren der Datei {0} des Mandanten {1} ist ein Fehler aufgetreten."})
    @Override
    public void run() {
        int count[] = {0};
        try {
            boolean dpChanged = updateFile("default.properties", false);
            if (dpChanged) {
                count[0]++;
                final ProviderSyncEventImpl evt = new ProviderSyncEventImpl("default.properties", instance);
                instance.events.post(evt);
            }
        } catch (IOException ex) {
            final String title = NbBundle.getMessage(Updater.class, "Updater.error.message", "default.properties", instance.getProvider());
            notifyError(ex, title);
        }
        //
        final Enumeration<? extends FileObject> c = FileUtil.getConfigFile("/SyncedFiles/").getChildren(false);
        while (c.hasMoreElements()) {
            final List<String> ll;
            try {
                ll = c.nextElement().asLines("utf-8");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                continue;
            }
            for (final String f : ll) {
                try {
                    boolean changed = updateFile(f, true);
                    if (changed) {
                        count[0]++;
                        final ProviderSyncEventImpl evt = new ProviderSyncEventImpl(f, instance);
                        instance.events.post(evt);
                    }
                } catch (IOException ex) {
                    final String title = NbBundle.getMessage(Updater.class, "Updater.error.message", f, instance.getProvider());
                    notifyError(ex, title);
                }
            }
        }
        //
        String[] pfl = {};
        try {
            pfl = ProviderFileLists.findProviderFileListNames();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        for (final String pl : pfl) {
            boolean plUpdated = false;
            try {
                plUpdated = updateFile(pl, true);
            } catch (IOException ex) {
                final String title = NbBundle.getMessage(Updater.class, "Updater.error.message", pl, instance.getProvider());
                notifyError(ex, title);
            }
            final Path list = instance.getBaseDir().resolve(pl);
            if (!Files.exists(list)) {
                continue;
            }
            final List<String> ll;
            try {
                ll = Files.readAllLines(list, StandardCharsets.UTF_8).stream()
                        .filter(l -> !l.startsWith("#"))
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                continue;
            }
            for (final String f : ll) {
                try {
                    boolean changed = updateFile(f, true);
                    if (changed) {
                        count[0]++;
                        final ProviderSyncEventImpl evt = new ProviderSyncEventImpl(f, instance);
                        instance.events.post(evt);
                    }
                } catch (IOException ex) {
                    final String title = NbBundle.getMessage(Updater.class, "Updater.error.message", f, instance.getProvider());
                    notifyError(ex, title);
                }
            }
            if (plUpdated) {
                final ProviderSyncEventImpl evt = new ProviderSyncEventImpl(pl, instance);
                instance.events.post(evt);
            }
        }
        //
        try {
            boolean changed = updateFile("layer-references", false);
            if (changed) {
                count[0]++;
            }
        } catch (IOException ex) {
            final String title = NbBundle.getMessage(Updater.class, "Updater.error.message", "layer-references", instance.getProvider());
            notifyError(ex, title);
        }
        final Path layerReferences = instance.getBaseDir().resolve("layer-references");
        if (Files.exists(layerReferences)) {
            try {
                Files.lines(layerReferences)
                        .filter(l -> !l.trim().startsWith("#"))
                        .forEach(f -> {
                            try {
                                boolean changed = updateFile(f.trim(), true);
                                if (changed) {
                                    count[0]++;
                                }
                            } catch (IOException ex) {
                                final String title = NbBundle.getMessage(Updater.class, "Updater.error.message", f, instance.getProvider());
                                notifyError(ex, title);
                            }
                        });
            } catch (IOException ioex) {
                Exceptions.printStackTrace(ioex);
            }
        }
        try {
            final boolean layerChanged = updateFile(LayerUpdater.LAYER_XML_FILE, false);
            if (layerChanged) {
                count[0]++;
                LayerProv.fireUpdate();
            }
        } catch (IOException ex) {
            final String title = NbBundle.getMessage(Updater.class, "Updater.error.message", LayerUpdater.LAYER_XML_FILE, instance.getProvider());
            notifyError(ex, title);
        }
        try {
            writeLastModified();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            String cnt = Integer.toString(count[0]);
            final String log = NbBundle.getMessage(Updater.class, "updater.success.log", instance.getProvider(), cnt);
            PlatformUtil.getCodeNameBaseLogger(Updater.class).log(Level.INFO, log);
            if (count[0] > 0) {
                final ProviderInfo pi = ProviderRegistry.getDefault().get(instance.getProvider());
                final String msg = NbBundle.getMessage(Updater.class, "updater.success", pi.getDisplayName(), cnt);
                StatusDisplayer.getDefault().setStatusText(msg);
            }
            final int delay = NbPreferences.forModule(SyncedProviderInstance.class).getInt("provider.update.rate", 10 * 60 * 1000);
            if (delay >= 0) {
                instance.enqueue(delay);
            }
        }
    }

    @NbBundle.Messages({"Updater.updateFile.tempNotDeleted=Die temporäre Datei {0} im Mandantenverzeichnis von {1} konnte nicht gelöscht werden."})
    boolean updateFile(final String file, final boolean ignoreNotFound) throws IOException {
        final Path tmp = tempFile();
        try {
            return doUpdateFile(file, ignoreNotFound, tmp);
        } finally {
            final boolean deleted = Files.deleteIfExists(tmp);
            if (!deleted) {
                final String msg = NbBundle.getMessage(Updater.class, "Updater.updateFile.tempNotDeleted", tmp.toString(), instance.getProvider());
                PlatformUtil.getCodeNameBaseLogger(Updater.class).log(LogLevel.INFO_WARNING, msg);
            }
        }
    }

    private Path tempFile() throws IOException {
        final String nbuser = System.getProperty("netbeans.user");
        final Path dir;
        if (nbuser != null) {
            dir = Paths.get(nbuser, "var/tmp");
        } else {
            dir = instance.getBaseDir().resolve("tmp");
        }
        Files.createDirectories(dir);
        final Path tmp = Files.createTempFile(dir, null, null);
        tmp.toFile().deleteOnExit();
        return tmp;
    }

    private boolean doUpdateFile(final String file, final boolean ignoreNotFound, final Path tmp) throws IOException {
        final String encoded = UrlEscapers.urlFragmentEscaper().escape(file);
//        final String encoded = URLEncoder.encode(file, "utf-8");
        final String url = URLs.adminResourcesDavBase(instance.findLocalFileProperties()) + encoded;
        final URI uri = URI.create(url);
        final WebProvider.SSL web = instance.findWebProvider(WebProvider.SSL.class);
        final String last;
        synchronized (lastModified) {
            last = lastModified.get(file);
        }
        final String lmReturn = HttpUtilities.get(web, uri, (lm, is) -> {
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            lastModified.put(file, lm);
            return lm;
        }, last, ignoreNotFound);
        if (lmReturn == null) {
            return false;
        }
        final Path target = instance.getBaseDir().resolve(file);
        if (!target.getParent().equals(instance.getBaseDir())) {
            Files.createDirectories(target.getParent());
        }
        Files.copy(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    public void setLastModified(final String res, final String lm) throws IOException {
        lastModified.put(res, lm);
        writeLastModified();
    }

    private void writeLastModified() throws IOException {
        final Path file = instance.getBaseDir().resolve(LAST_MODIFIED_FILE);
        final List<String> lines = lastModified.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    @NbBundle.Messages({"Updater.error.title=Synchronisierungsfehler"})
    static void notifyError(Exception ex, String message) {
        PlatformUtil.getCodeNameBaseLogger(Updater.class).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(Updater.class, "Updater.error.title");
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

}
