/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.config;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.ui.util.dav.URLs;

/**
 *
 * @author boris.heithecker
 */
@Messages("EditStundentafelEnv.displayName=Stundentafel für {0}")
class EditStundentafelEnv implements Runnable {

    private static final String CURR_PATH = "signee/curriculum.xml";
    private final String provider;
    private final static Map<String, WeakReference<EditStundentafelEnv>> MAP = new HashMap<>();
    final RequestProcessor.Task init;
    final String urlBase;
    final RequestProcessor RP = new RequestProcessor();
    private final String displayName;
    private Path tempFile;
    private DataObject data;

    @SuppressWarnings(value = {"LeakingThisInConstructor"})
    private EditStundentafelEnv(WebProvider service, String base) throws IOException {
        this.provider = service.getInfo().getURL();
        final String display = NbBundle.getMessage(EditStundentafelEnv.class, "EditStundentafelEnv.displayName", service.getInfo().getDisplayName());
        this.displayName = display;
        this.urlBase = base;
        this.init = RP.post(this);
    }

    static EditStundentafelEnv create(final String provider) throws IOException {
        synchronized (MAP) {
            final EditStundentafelEnv wr = Optional.ofNullable(MAP.get(provider))
                    .map(WeakReference::get)
                    .orElse(null);
            if (wr == null) {
                final EditStundentafelEnv b = createImpl(provider);
                MAP.put(provider, new WeakReference<>(b));
                return b;
            } else {
                return wr;
            }
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProvider() {
        return provider;
    }

    public DataObject getData() {
        if (!init.isFinished()) {
            init.waitFinished();
        }
        return data;
    }

    private static EditStundentafelEnv createImpl(final String provider) throws IOException {
        String base = null;
        try {
            final LocalProperties lp = LocalProperties.find(provider);
            base = URLs.adminResourcesDavBase(lp);
        } catch (ConfigurationException | NoProviderException ex) {
            throw new IOException(ex);
        }
        final WebProvider service = WebProvider.find(provider, WebProvider.class);
        return new EditStundentafelEnv(service, base);
    }

    @Override
    public void run() {
        try {
            tempFile = Files.createTempFile("curriculum", ".xml");
            tempFile.toFile().deleteOnExit();
            runImpl();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void runImpl() throws IOException {
        final WebProvider service = WebProvider.find(provider, WebProvider.class);
        final boolean res = HttpUtil.fetchFile(service, urlBase + CURR_PATH, tempFile);
        if (!res) {
            final FileObject template = FileUtil.getConfigFile("/Templates/Betula-Extension/Stundentafel.xml");
            Files.copy(template.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        final FileObject fo = FileUtil.toFileObject(tempFile.toFile());
        data = DataObject.find(fo);
    }

    public RequestProcessor.Task saveProperties() {
        return RP.post(this::saveImpl);
    }

    private void saveImpl() {
        final WebProvider service = WebProvider.find(provider, WebProvider.class);
        try {
            HttpUtil.storeFile(service, urlBase + CURR_PATH, tempFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
