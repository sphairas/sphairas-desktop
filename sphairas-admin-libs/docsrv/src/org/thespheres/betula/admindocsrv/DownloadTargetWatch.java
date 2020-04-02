/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author boris.heithecker
 */
final class DownloadTargetWatch implements Runnable {

    private final ExecutorService executor;
    private final WatchService watcher;
    private static final Logger LOGGER = Logger.getLogger(DownloadTargetWatch.class.getName());
    private final DownloadTargetFolders parent;

    @SuppressWarnings("LeakingThisInConstructor")
    DownloadTargetWatch(final Path dir, final DownloadTargetFolders parent) throws IOException {
        this.parent = parent;
        final FileSystem fs = dir.getFileSystem();
        watcher = fs.newWatchService();
        dir.register(watcher, StandardWatchEventKinds.ENTRY_DELETE);
        executor = Executors.newSingleThreadExecutor();
        executor.submit(this);
    }

    void cleanup() {
        try {
            watcher.close();
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Error closing layer file watcher.");
        }
        executor.shutdown();
    }

    @Override
    public void run() {
        while (true) {
            WatchKey key;
            try {
                // wait for a key to be available
                key = watcher.take();
            } catch (InterruptedException ex) {
                continue;
            }
            key.pollEvents().stream()
                    .filter(e -> (e.kind() != StandardWatchEventKinds.OVERFLOW))
                    .filter(e -> {
                        final Path changed = (Path) e.context();
                        return changed.getNameCount() == 1 && !changed.endsWith(".files");
                    })
                    .forEach(e -> {
                        final WatchEvent.Kind<?> kind = e.kind();
                        LOGGER.log(Level.INFO, "Detected layer file change: {0}", kind.name());
                        parent.removed(((Path) e.context()).toString());
                    });
            // IMPORTANT: The key must be reset after processed
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }

    }

}
