/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.MissingResourceException;
import java.util.StringJoiner;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.OutputWriter;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Description;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.ExceptionMessage;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ui.util.AppProperties;
import org.thespheres.betula.services.ui.util.ContainerUtil;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportUtil;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
public abstract class AbstractUpdater<I extends ImportItem> implements Runnable {

    protected final I[] items;
    protected OutputWriter err = ImportUtil.getIO().getErr();
    private boolean dryRun = false;
    private FileObject dumpContainerFolder;
    protected final static EventBus EVENTS = new EventBus("updater");

    protected AbstractUpdater(I[] items) {
        this.items = items;
    }

    public I[] getItems() {
        return items;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public FileObject getDumpContainerFolder() {
        return dumpContainerFolder;
    }

    public void setDumpContainerFolder(FileObject dumpContainerFolder) {
        this.dumpContainerFolder = dumpContainerFolder;
    }

    public OutputWriter getErrorWriter() {
        return err;
    }

    public static void registerEventListener(final Object object) {
        EVENTS.register(object);
    }

    @Override
    public abstract void run();

    @Messages({"AbstractUpdater.message.remote.error.header====== Server-Fehler =====",
        "AbstractUpdater.message.remote.error.node=== [Ebene {0}] {1} ==",
        "AbstractUpdater.message.remote.error.finisher====== Ende ====="})
    public static void handleExceptions(final Container ret) {
        if (ret != null) {
            ret.getEntries().stream()
                    .filter(t -> t.getException() != null)
                    .forEach(AbstractUpdater::processException);
        }
    }

    private static void processException(final Template<?> t) throws MissingResourceException {
        final ExceptionMessage pre = t.getException();
        final StringJoiner sj = new StringJoiner("\n");
        sj.add(NbBundle.getMessage(TargetItemsUpdater.class, "AbstractUpdater.message.remote.error.header"));
        sj.add(NbBundle.getMessage(TargetItemsUpdater.class, "AbstractUpdater.message.remote.error.node", Integer.toString(0), createNodeIdentifier(t)));
        t.getChildren().stream()
                .forEach(c -> sj.add(NbBundle.getMessage(TargetItemsUpdater.class, "AbstractUpdater.message.remote.error.node", Integer.toString(1), createNodeIdentifier(c))));
        sj.add(pre.getUserMessage());
        sj.add(pre.getLogMessage());
        sj.add(pre.getStackTraceElement());
        sj.add(NbBundle.getMessage(TargetItemsUpdater.class, "AbstractUpdater.message.remote.error.finisher"));
        ImportUtil.getIO().getErr().println(sj.toString());
    }

    private static String createNodeIdentifier(final Template<?> t) {
        final StringJoiner sj = new StringJoiner(" : ");
        if (t instanceof Entry) {
            final Entry<?, ?> e = (Entry<?, ?>) t;
            if (e.getIdentity() != null) {
                sj.add(e.getIdentity().toString());
            } else {
                sj.add("null");
            }
        }
        if (t != null) {
            if (t.getValue() != null) {
                sj.add(t.getValue().toString());
            } else {
                sj.add("null");
            }
            t.getDescription().stream()
                    .map(Description::getDescription)
                    .forEach(sj::add);
        }
        return sj.toString();
    }

    protected void dumpContainer(final Container container, final WebServiceProvider provider) {
        writeDump(container, "import");
        final FileObject folder = getDumpContainerFolder();
        if (folder != null) {
            final String filename = FileUtil.findFreeFileName(folder, "import", "xml");
            final LocalProperties prop = LocalProperties.find(provider.getInfo().getURL());
            final String privateKeyAlias = AppProperties.privateKeyAlias(prop, provider.getInfo().getURL());
            try {
                ContainerUtil.write(container, getDumpContainerFolder(), filename, privateKeyAlias);
            } catch (IOException ex) {
                ex.printStackTrace(getErrorWriter());
            }
        }
    }

    protected void dumpReturnContainer(final Container container, final WebServiceProvider provider) {
        writeDump(container, "import-return");
    }

    private void writeDump(final Container container, final String baseName) {
        final String nbuser = System.getProperty("netbeans.user");
        if (nbuser != null) {
            final Path dir = Paths.get(nbuser, "var/log");
            final Path backup2 = dir.resolve(baseName + ".2.xml");
            final Path backup1 = dir.resolve(baseName + ".1.xml");
            final Path target = dir.resolve(baseName + ".xml");
            if (Files.exists(backup1)) {
                try {
                    Files.copy(backup1, backup2, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    PlatformUtil.getCodeNameBaseLogger(AbstractUpdater.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }
            if (Files.exists(target)) {
                try {
                    Files.copy(target, backup1, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    PlatformUtil.getCodeNameBaseLogger(AbstractUpdater.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }
            try {
                ContainerUtil.write(container, target);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(AbstractUpdater.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
