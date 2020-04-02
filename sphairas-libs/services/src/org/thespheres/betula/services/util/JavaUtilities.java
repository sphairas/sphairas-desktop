/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author boris.heithecker
 */
public class JavaUtilities {

    private JavaUtilities() {
    }

    public static void zip(final Path sourceDirectory, final Path targetFile) throws IOException {
        try (final ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(targetFile))) {
            try (final DirectoryStream<Path> ds = Files.newDirectoryStream(sourceDirectory)) {
                for (final Path p : ds) {
                    final ZipEntry ze = new ZipEntry(sourceDirectory.relativize(p).toString());
                    zos.putNextEntry(ze);
                    Files.copy(p, zos);
                    zos.closeEntry();
                }
            }
        }
    }

    public static Map<String, String> parseCommandLineArgs(final String[] args) {
        final Map<String, String> m = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String key;
            if (args[i].startsWith("--")) {
                key = args[i].substring(2);
            } else if (args[i].startsWith("-")) {
                key = args[i].substring(1);
            } else {
                m.put(args[i], null);
                continue;
            }
            final int index = key.indexOf('=');
            if (index == -1) {
                if ((i + 1) < args.length) {
                    final String v = args[i + 1];
                    // yes - but does the value look like a key?
                    if (!v.startsWith("-")) {
                        m.put(key, v);
                        i++;
                    } else {
                        m.put(args[i], null);
                    }
                } else {
                    m.put(args[i], null);
                }
            } else {
                key = key.substring(0, index);
                final String value = key.substring(index + 1);
                m.put(key, value);
            }
        }
        return m;
    }
}
