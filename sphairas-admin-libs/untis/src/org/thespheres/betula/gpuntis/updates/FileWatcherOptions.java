/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.updates;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.thespheres.betula.services.ProviderInfo;

/**
 *
 * @author boris.heithecker
 */
public abstract class FileWatcherOptions extends OptionsPanelController {

    protected static String PREFERENCE_UNTIS_UPDATE_WATCH_FOLDER_ENABLED = "untis.update.watch.folder.enabled";
    protected static String PREFERENCE_UNTIS_UPDATE_WATCH_FOLDER_PATH = "untis.update.watch.folder.path";
    private FolderLocationPanel panel;
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    protected boolean changed;

    @Override
    public final void update() {
        getPanel().load();
        changed = false;
    }

    @Override
    public final void applyChanges() {
        SwingUtilities.invokeLater(() -> {
            getPanel().store();
            final FileObject folder = getWatchedFolder();
//            UpdatesFileWatcher.getInstance(getProvider().getURL()).setFolder(folder);
            changed = false;
        });
    }

    @Override
    public final void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public final boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public final boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    private FolderLocationPanel getPanel() {
        if (panel == null) {
            panel = new FolderLocationPanel(this);
        }
        return panel;
    }

    final void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    protected boolean isWatchFolder() {
        return NbPreferences.forModule(getClass()).getBoolean(getIsWatchFolderPreferencesKey(), false);
    }

    protected void setWatchFoler(boolean watch) {
        NbPreferences.forModule(getClass()).putBoolean(getIsWatchFolderPreferencesKey(), watch);
    }

    protected FileObject getWatchedFolder() {
        final String path = NbPreferences.forModule(getClass()).get(getWatchFolderPathPreferencesKey(), null);
        if (path != null) {
            final FileObject ret = FileUtil.toFileObject(new File(path));
            if (ret.isFolder()) {
                return ret;
            }
        }
        return null;
    }

    protected void setWatchedFolder(FileObject folder) {
        if (folder.isFolder()) {
            final String path = folder.getPath();
            NbPreferences.forModule(getClass()).put(getWatchFolderPathPreferencesKey(), path);
        }
    }

    protected abstract ProviderInfo getProvider();

    protected void setProvider(String provider) {
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected String getIsWatchFolderPreferencesKey() {
        return PREFERENCE_UNTIS_UPDATE_WATCH_FOLDER_ENABLED;
    }

    protected String getWatchFolderPathPreferencesKey() {
        return PREFERENCE_UNTIS_UPDATE_WATCH_FOLDER_PATH;
    }

}
