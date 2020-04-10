package org.thespheres.betula.adminconfig.impl;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.adminconfig.ConfigurationRegistration;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes("org.thespheres.betula.adminconfig.ConfigurationRegistration")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ConfigurationRegistrationLayerGenerator<C> extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (final Element e : roundEnv.getElementsAnnotatedWith(ConfigurationRegistration.class)) {
            final ConfigurationRegistration r = e.getAnnotation(ConfigurationRegistration.class);
            if (e instanceof ExecutableElement && e.getKind().equals(ElementKind.METHOD) && e.getModifiers().contains(Modifier.STATIC)) {
                final String method = e.getSimpleName().toString();
                final Element ee = e.getEnclosingElement();
                if (ee instanceof TypeElement) {
                    final String clz = this.processingEnv.getElementUtils().getBinaryName((TypeElement) ee).toString();
                    writeOne(r, e, clz, method);
                    continue;
                }
            }
            throw new LayerGenerationException("ConfigurationRegistration can be used only on static method top level class.");
        }
        return true;
    }

    protected void writeOne(final ConfigurationRegistration r, final Element e, final String clz, final String method) throws LayerGenerationException, IllegalArgumentException {
        final String resource = r.resource();
        final String resName = r.resourceName();
        final String syncedPath = "SyncedFiles/" + resource;
        final LayerBuilder.File file = layer(e).file(syncedPath);
        file.contents(resource);
        file.stringvalue("access-class", clz);
        file.stringvalue("access-method", method);
        if (!"".equals(resName)) {
            file.stringvalue("resource-name", resName);
        }
        file.write();
    }

}
