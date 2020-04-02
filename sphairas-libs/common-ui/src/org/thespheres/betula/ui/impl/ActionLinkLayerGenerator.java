/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.impl;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang3.StringUtils;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.ui.util.ActionLink;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes({"org.thespheres.betula.ui.util.ActionLink"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ActionLinkLayerGenerator extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(ActionLink.class)) {
            ActionLink r = e.getAnnotation(ActionLink.class);
            writeOneActionLink(r, (ExecutableElement) e);
        }
        return true;
    }

    protected void writeOneActionLink(ActionLink r, ExecutableElement e) throws LayerGenerationException, IllegalArgumentException {
        String cat = r.category();
        if (StringUtils.isBlank(cat)) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        String id = r.id();
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        int pos = r.position();
        if (!e.getModifiers().contains(Modifier.STATIC)) {
            throw new IllegalArgumentException("Annotated element must be static method");
        }
        TypeMirror actionListenerType = processingEnv.getElementUtils().getTypeElement("java.awt.event.ActionListener").asType();
        if (!processingEnv.getTypeUtils().isAssignable(e.getReturnType(), actionListenerType)) {
            throw new IllegalArgumentException("Return type must implements java.awt.event.ActionListener");
        }
        if (e.getParameters().size() > 1) {
            throw new IllegalArgumentException("Annotated method must not have more than one parameter");
        }
        String path = ActionLinkInstance.findPath(cat, id);
        String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e.getEnclosingElement()).toString();
        String method = e.getSimpleName().toString();
        String basename = clazz.replace('.', '-');
        String file = path + "/" + basename + ".instance";
        layer(e).file(file)
                .methodvalue("instanceCreate", "org.thespheres.betula.ui.impl.ActionLinkInstance", "create")
                .stringvalue("class", clazz)
                .stringvalue("method", method)
                .intvalue("position", pos)
                .write();
    }

}
