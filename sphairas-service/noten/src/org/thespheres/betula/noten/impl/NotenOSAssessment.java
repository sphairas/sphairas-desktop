/*
 * NotenAssessmentContext.java
 *
 * Created on 17. November 2007, 17:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.MissingResourceException;
import javax.swing.JComponent;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.thespheres.betula.assess.AssessmentContext.CustomizerProvider;
import org.thespheres.betula.assess.Distribution;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;
import org.thespheres.betula.assess.GroupingGrades;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.Int2;

/**
 *
 * @author Boris Heithecker
 */
public class NotenOSAssessment extends AbstractInt2Assessment implements CustomizerProvider, GroupingGrades { //, ClassGraphProvider {

    public static final String NAME = "de.notensystem.os";
    private Distribution[] defaultDistributions;
    private NotenOSAssessToolPanel userPanel;

    public NotenOSAssessment() {
        super(null);
    }

    public NotenOSAssessment(Int2 rangeMaximum) {
        super(rangeMaximum);
        //this.marginModel = new MarginModel(this);
        this.allocator = new AllocatorImpl(this, this, Verteilungen.OBERSTUFE, ceiling);
        this.assessorList = new AssessorListImpl(this);
    }

    public NotenOSAssessment(Int2 rangeMaximum, Int2[] floorValues, String defaultDistribution) {
        super(rangeMaximum);
        //this.marginModel = new MarginModel(this, model, marginValue);
        this.allocator = new AllocatorImpl(this, this, ceiling, floorValues, defaultDistribution);
        this.assessorList = new AssessorListImpl(this);
    }

    @Override
    public Grade[] getAllGrades() {
        return NotenOS.ALL;
    }

    @Override
    public Grade[] getAllGradesReverseOrder() {
        return NotenOS.ALL_REV;
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        return parse(text);
    }

    static Grade parse(String text) throws GradeParsingException {
        final String trimText = text.trim();
        Grade grade = Arrays.stream(NotenOS.ALL)
                .filter(g -> g.getLongLabel().equals(trimText) || g.getShortLabel().equals(trimText))
                .collect(CollectionUtil.singleOrNull());
        if (grade != null) {
            return grade;
        }
        try {
            final int iv = Integer.parseInt(trimText);
            grade = Arrays.stream(NotenOS.ALL)
                    .filter(g -> ((NotenOS.NotenOSGradeImpl) g).val == iv)
                    .collect(CollectionUtil.singleOrNull());
        } catch (NumberFormatException e) {
        }
        if (grade != null) {
            return grade;
        }
        throw new GradeParsingException(NAME, text);
    }

    @Override
    public Grade find(String id) {
        for (Grade g : NotenOS.ALL) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public Grade getFloorUnbiased() {
        return NotenOS.P0;
    }

    @Override
    public Grade getCeilingUnbiased() {
        return NotenOS.P15;
    }

    @Override
    public Grade[] getAllGradesUnbiased() {
        return NotenOS.ALL;
    }

    @Override
    public Distribution<Int2>[] getDefaultDistributions() {
        if (defaultDistributions == null) {
            defaultDistributions = new Distribution[]{
                Verteilungen.OBERSTUFE
            };
        }
        return defaultDistributions;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        try {
            JAXBContext ctx = JAXBContext.newInstance(NotenOSAssessmentContextXmlAdapter.class);
            NotenOSAssessmentContextXmlAdapter adapter = createAdapter();
            ctx.createMarshaller().marshal(adapter, os);
        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public NotenOSAssessmentContextXmlAdapter createAdapter() {
        Int2[] floorVal = new Int2[16];
        Iterator<Grade> it = linkedGradesIterator();
        for (int i = 0; i < 16; i++) {
            floorVal[i] = getAllocator().getFloor(it.next());
        }
        String defaultDist = null;
        for (Distribution d : defaultDistributions) {
            if (d.equals(getCurrentDistribution())) {
                defaultDist = d.toString();
            }
        }
        NotenOSAssessmentContextXmlAdapter adapter = new NotenOSAssessmentContextXmlAdapter(getRangeMaximum(), floorVal, defaultDist);
        return adapter;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Int2[] floorValues = new Int2[16];
        int i = 0;
        for (Grade g : this) {
            floorValues[i++] = getAllocator().getFloor(g);
        }
        String defDist = null;
        for (Distribution d : getDefaultDistributions()) {
            if (d.equals(getCurrentDistribution())) {
                defDist = d.toString();
            }
        }
        NotenOSAssessment ret = new NotenOSAssessment(this.ceiling, floorValues, defDist);
        return ret;
    }

    @Override
    public JComponent getCustomizer() {
        if (userPanel != null) {
            return userPanel;
        } else {
            return userPanel = new NotenOSAssessToolPanel(this);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(NotenAssessment.class, "de.notensystem.os.displayName");
    }

    static String displayName() throws MissingResourceException {
        return NbBundle.getMessage(NotenAssessment.class, "de.notensystem.os.displayName");
    }

    @Override
    public GradeGroup findGroup(Grade g) {
        if (g == null || !g.getConvention().equals(getName())) {
            return null;
        }
        return Arrays.stream(NoteOSGroup.ALL)
                .filter(ng -> Arrays.stream(ng.grades).anyMatch(g::equals))
                .collect(CollectionUtil.singleOrNull());
    }
}
