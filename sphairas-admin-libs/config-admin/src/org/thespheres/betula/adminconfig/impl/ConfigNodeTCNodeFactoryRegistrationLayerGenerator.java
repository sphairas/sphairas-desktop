package org.thespheres.betula.adminconfig.impl;

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
import org.thespheres.betula.adminconfig.ConfigNodeTopComponentNodeList;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes("org.thespheres.betula.adminconfig.ConfigNodeTopComponentNodeList.Factory.Registration")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ConfigNodeTCNodeFactoryRegistrationLayerGenerator<C> extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (final Element e : roundEnv.getElementsAnnotatedWith(ConfigNodeTopComponentNodeList.Factory.Registration.class)) {
            final ConfigNodeTopComponentNodeList.Factory.Registration r = e.getAnnotation(ConfigNodeTopComponentNodeList.Factory.Registration.class);
            writeOne(r, e);
        }
        return true;
    }

    protected void writeOne(final ConfigNodeTopComponentNodeList.Factory.Registration r, final Element e) throws LayerGenerationException, IllegalArgumentException {
        final String componentPath = "ConfigNodeTopComponentNodeFactory";
        layer(e).instanceFile(componentPath, null, ConfigNodeTopComponentNodeList.Factory.class)
                .write();
    }

}
