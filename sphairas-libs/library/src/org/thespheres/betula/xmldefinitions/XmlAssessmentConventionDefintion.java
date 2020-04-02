/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmldefinitions;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlSeeAlso(XmlGradeItem.class)
@XmlRootElement(name = "assessment-convention-definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlAssessmentConventionDefintion extends AbstractXmlConvention<Grade> implements AssessmentConvention.OfBiasable {

    @XmlElementWrapper(name = "grade-definitions")
    @XmlElementRef
    private final List<XmlGradeItem> list = new ArrayList<>();
    @XmlElement(name = "description")
    private final List<XmlDescription> description = new ArrayList<>();

    public XmlAssessmentConventionDefintion() {
    }

    public XmlAssessmentConventionDefintion(final String name) {
        super(name);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<XmlGradeItem> getGrades() {
        return list;
    }

    public List<XmlDescription> getDescription() {
        return description;
    }

    @Override
    public final Grade find(final String id) {
        final Grade ret;
        synchronized (list) {
            ret = list.stream()
                    .filter(m -> m.getId().equals(id))
                    .collect(CollectionUtil.requireSingleOrNull());
        }
        return ret;
    }

    @Override
    public Grade[] getAllGrades() {
        return list.stream()
                .toArray(Grade[]::new);
    }

    @Override
    public Grade[] getAllGradesUnbiased() {
        return getAllGrades();
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        return list.stream()
                .filter(e -> text.equalsIgnoreCase(e.getLongLabel()) || text.equalsIgnoreCase(e.getShortLabel()))
                .findAny().orElseThrow(() -> new GradeParsingException(getName(), text));
    }

}
