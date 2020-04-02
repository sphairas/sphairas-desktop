package org.thespheres.betula.ui.impl;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.ui.util.JAXBUtil;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes({"org.thespheres.betula.ui.util.JAXBUtil.JAXBRegistration",
    "org.thespheres.betula.ui.util.JAXBUtil.JAXBRegistrations"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JAXBClassRegistrationLayerGenerator<C> extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(JAXBUtil.JAXBRegistration.class)) {
            JAXBUtil.JAXBRegistration r = e.getAnnotation(JAXBUtil.JAXBRegistration.class);
            writeOne(r, e);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(JAXBUtil.JAXBRegistrations.class)) {
            JAXBUtil.JAXBRegistrations rs = e.getAnnotation(JAXBUtil.JAXBRegistrations.class);
            for (JAXBUtil.JAXBRegistration r : rs.value()) {
                writeOne(r, e);
            }
        }
        return true;
    }

    protected void writeOne(JAXBUtil.JAXBRegistration r, Element e) throws LayerGenerationException, IllegalArgumentException {
        String componentPath = r.target() + "/JAXBTypes";
        String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
        String basename = clazz.replace('.', '-');
        String file = componentPath + "/" + basename + ".instance";
        //TODO: check type, example see sources...
        layer(e).file(file).methodvalue("instanceCreate", JAXBClassRegistrationLayerGenerator.class.getName(), "createJAXBType").stringvalue("jaxbType", clazz) //type.toString())
                .write();
    }

    public static <C> Class<C> createJAXBType(Map<String, ?> params) {
        String type = (String) params.get("jaxbType");
        try {
            final ClassLoader sysCl = Lookup.getDefault().lookup(ClassLoader.class);
            return (Class<C>) Class.forName(type, true, sysCl);
        } catch (ClassNotFoundException | ClassCastException ex) {
            Logger.getLogger(JAXBClassRegistrationLayerGenerator.class.getName()).log(Level.SEVERE, "An error ocurred looking up class {0}.", type);
        }
        return null;
    }
}
