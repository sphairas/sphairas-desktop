/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public class BetulaProjectUtil {

    private BetulaProjectUtil() {
    }

    public static void createProject(Path projectDir, Properties prop, BetulaProjectType... type) throws IOException {
        Path sphairas = projectDir.resolve(BetulaProject.SPHAIRAS_PROJECT);
        if (!Files.isDirectory(sphairas)) {
            Files.createDirectories(sphairas);
        }
        DosFileAttributeView attrs = Files.getFileAttributeView(sphairas, DosFileAttributeView.class);
        if (attrs != null) {
            attrs.setHidden(true);
        }
        for (BetulaProjectInstantiation bpi : Lookup.getDefault().lookupAll(BetulaProjectInstantiation.class)) {
            bpi.instatiate(projectDir, sphairas, prop, type);
        }
        Path properties = sphairas.resolve(BetulaProject.PROJECT_PROPERTIES_FILENAME);
        try (final OutputStream os = Files.newOutputStream(properties)) {
            prop.store(os, null);
        }
    }

    public static void updateLocalProperties(BetulaProject project, String... overrides) throws IOException {
        final FileObject fo = URLMapper.findFileObject(project.getConfigurationsPath().toURL());
        if (fo == null) {
            throw new IOException("No configurations Path found.");
        }
        final FileObject props = fo.getFileObject(BetulaProject.PROJECT_PROPERTIES_FILENAME);
        if (props == null) {
            throw new IOException("No properties file found.");
        }
        final FileLock lock = props.lock();
        String bak = FileUtil.findFreeFileName(fo, props.getName(), "bak");
        FileObject backup = FileUtil.copyFile(props, fo, bak, "bak");
        final Properties properties = new Properties();
        try (final InputStream is = props.getInputStream()) {
            properties.load(is);
        }
        Arrays.stream(overrides)
                .map(s -> s.split("="))
                .filter(a -> a.length == 2)
                .forEach(a -> properties.setProperty(a[0], StringUtils.trimToNull(a[1])));
        try (OutputStream os = props.getOutputStream(lock)) {
            properties.store(os, null);
        }
        lock.releaseLock();
        backup.delete();
    }
}
