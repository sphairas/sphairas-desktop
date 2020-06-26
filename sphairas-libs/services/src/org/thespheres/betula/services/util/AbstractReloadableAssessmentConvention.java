/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;
import org.thespheres.betula.xmldefinitions.XmlAssessmentConventionDefintion;
import org.thespheres.betula.xmldefinitions.XmlDescription;
import org.thespheres.betula.xmldefinitions.XmlGradeItem;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractReloadableAssessmentConvention implements AssessmentConvention {

    protected final ChangeSupport cSupport = new ChangeSupport(this);
    protected final String provider;
    protected final String name;

    protected AbstractReloadableAssessmentConvention(final String provider, final String name) {
        this.provider = provider;
        this.name = name;
    }

    public static AbstractReloadableAssessmentConvention create(final Map<String, ?> args) {
        final String name = (String) args.get("name");
        final String provider = (String) args.get("provider");
        final String resource = (String) args.get("resource");
        final Map<String, String> other = args.entrySet().stream()
                .filter(e -> !"instanceCreate".equals(e.getKey())) //recursive method invocation
                .filter(e -> e.getValue() instanceof String)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
        final Factory fac = Lookup.getDefault().lookup(Factory.class);
        return fac.create(provider, name, resource, other);
    }

    @Override
    public final String getName() {
        return name;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public String getDisplayName() {
        return getDefinition().getDisplayName();
    }

    public void setDisplayName(String displayName) {
        getDefinition().setDisplayName(displayName);
    }

    public List<XmlDescription> getDescription() {
        return getDefinition().getDescription();
    }

    public List<XmlGradeItem> getGrades() {
        return getDefinition().getGrades();
    }

    @Override
    public final Grade find(String id) {
        return getDefinition().find(id);
    }

    @Override
    public Grade[] getAllGrades() {
        return getDefinition().getAllGrades();
    }

    public Grade[] getAllGradesUnbiased() {
        return getDefinition().getAllGradesUnbiased();
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        return getDefinition().parseGrade(text);
    }

    protected abstract XmlAssessmentConventionDefintion getDefinition();

    protected abstract void markForReload();

    public void addChangeListener(final ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    public void removeChangeListener(final ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    public static abstract class Factory {

        protected abstract AbstractReloadableAssessmentConvention create(final String provider, final String name, final String resource, final Map<String, String> arg) throws IllegalStateException;
    }

}
