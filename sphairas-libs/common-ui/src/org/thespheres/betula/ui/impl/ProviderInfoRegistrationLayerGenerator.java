package org.thespheres.betula.ui.impl;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.ProviderInfo;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes("org.thespheres.betula.services.ProviderInfo.Registration")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ProviderInfoRegistrationLayerGenerator<C> extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(ProviderInfo.Registration.class)) {
            ProviderInfo.Registration r = e.getAnnotation(ProviderInfo.Registration.class);
            writeOne(r, e);
        }
        return true;
    }

    protected void writeOne(ProviderInfo.Registration r, Element e) throws LayerGenerationException, IllegalArgumentException {
        final String path = "Provider";
        String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
        String basename = clazz.replace('.', '-');
        String file = path + "/" + basename + ".instance";
        layer(e).file(file)
                .methodvalue("instanceCreate", "org.thespheres.betula.services.impl.XmlProviderInfoEntry", "create")
                .stringvalue("url", r.url())
                .stringvalue("display-name", r.displayName())
                .write();
    }

}
