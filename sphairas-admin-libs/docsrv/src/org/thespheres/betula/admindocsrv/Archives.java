/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.openide.util.NbPreferences;

/**
 *
 * @author boris.heithecker
 */
public class Archives {

    private final boolean seven7;

    public Archives() {
        seven7 = NbPreferences.forModule(DownloadTargetFolders.class).getBoolean(DownloadTargetFolders.PREF_USE_7Z, false);
    }

    public String getArchiveFileExtension() {
        return seven7 ? "7z" : "zip";
    }

    public void createArchive(final Path destination, final List<ArchiveFile> files) throws IOException {
        if (seven7) {
            create7zArchive(destination, files);
        } else {
            createZipArchive(destination, files);
        }
    }

    static void createZipArchive(final Path destination, final List<ArchiveFile> files) throws IOException {
        try (final ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(destination))) {
            for (final ArchiveFile p : files) {
                final ZipEntry ze = new ZipEntry(p.getName());
                zos.putNextEntry(ze);
                final byte[] bb = Files.readAllBytes(p.getTmp());
                zos.write(bb, 0, bb.length);
                zos.closeEntry();
            }
        }
    }

    private static void create7zArchive(Path destination, List<ArchiveFile> files) throws IOException {
        try (final SevenZOutputFile sevenZOutput = new SevenZOutputFile(destination.toFile())) {
            sevenZOutput.setContentCompression(SevenZMethod.LZMA2);
            for (final ArchiveFile p : files) {
                final SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(p.getTmp().toFile(), p.getName());
                sevenZOutput.putArchiveEntry(entry);
                final byte[] content = Files.readAllBytes(p.getTmp());
                sevenZOutput.write(content);
                sevenZOutput.closeArchiveEntry();
            }
        }
    }

    public static class ArchiveFile {

        final Path tmp;
        final String name;

        public ArchiveFile(String name, Path tmp) {
            super();
            this.tmp = tmp;
            this.name = name;
        }

        public Path getTmp() {
            return tmp;
        }

        public String getName() {
            return name;
        }

    }

}
