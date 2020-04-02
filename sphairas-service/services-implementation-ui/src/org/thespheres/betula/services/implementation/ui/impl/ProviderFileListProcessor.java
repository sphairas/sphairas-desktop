/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import com.google.common.io.Files;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.implementation.ui.ProviderFileListName;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes({"org.thespheres.betula.services.implementation.ui.ProviderFileListName"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ProviderFileListProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(ProviderFileListName.class)) {
            final ProviderFileListName r = e.getAnnotation(ProviderFileListName.class);
            final String name = StringUtils.trimToNull(r.value());
            if (name != null) {
                writeOneList(name);
            }
        }
        return true;
    }

    private void writeOneList(final String addEntry) {

        final Filer filer = processingEnv.getFiler();
        List<String> entries;
        try {
            final FileObject fo = filer.getResource(StandardLocation.CLASS_OUTPUT, "", ProviderFileLists.METAINF_PROVIDER_FILE_LISTS);
            entries = Files.readLines(Paths.get(fo.toUri()).toFile(), Charset.forName("UTF-8"));
//                final BufferedReader r = new BufferedReader(new InputStreamReader(f.openInputStream(), "UTF-8"));
//                String line;
//                while ((line = r.readLine()) != null) {
//                    e.getValue().add(line);
//                }
//                r.close();
        } catch (FileNotFoundException | NoSuchFileException x) {
            // doesn't exist
            entries = Collections.EMPTY_LIST;
        } catch (IOException x) {
            processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to load existing service definition files: " + x);
            return;
        }

        if (!entries.contains(addEntry)) {
            try {
                processingEnv.getMessager().printMessage(Kind.NOTE, ProviderFileLists.METAINF_PROVIDER_FILE_LISTS);
                final FileObject f = filer.createResource(StandardLocation.CLASS_OUTPUT, "", ProviderFileLists.METAINF_PROVIDER_FILE_LISTS);
                try (final PrintWriter pw = new PrintWriter(new OutputStreamWriter(f.openOutputStream(), "UTF-8"))) {
                    entries.stream().forEach(pw::println);
                    pw.println(addEntry);
                }
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to write service definition files: " + x);
            }
        }
    }

}
