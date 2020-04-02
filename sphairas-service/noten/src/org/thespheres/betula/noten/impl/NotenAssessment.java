/*
 * NotenAssessment.java
 *
 * Created on 17. November 2007, 17:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.MissingResourceException;
import javax.swing.JComponent;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.thespheres.betula.assess.AssessmentContext.CustomizerProvider;
import org.thespheres.betula.util.Int2;
import org.thespheres.betula.assess.Distribution;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;

/**
 *
 * @author Boris Heithecker
 */
public class NotenAssessment extends AbstractInt2Assessment implements CustomizerProvider { //implements  AssessToolProvider, ClassGraphProvider {

    public static final String NAME = "de.notensystem";
    private transient Distribution[] defaultDistributions;
    private transient MarginModel marginModel;
    private transient NotenAssessToolPanel userPanel;

    public NotenAssessment() {
        super(null);
    }

    public NotenAssessment(Int2 rangeMaximum) {
        super(rangeMaximum);
        this.marginModel = new MarginModel(this);
        this.allocator = new AllocatorImpl(this, this, Verteilungen.RAHMENRICHTLINIEN, ceiling);
        this.assessorList = new AssessorListImpl(this);
    }

    //TODO wird der Konstruktor noch gebraucht???
    public NotenAssessment(Int2 rangeMaximum, Int2[] floorValues, MarginModel.Model model, Int2 marginValue, String defaultDistribution) {
        super(rangeMaximum);
        this.marginModel = new MarginModel(this, model, marginValue);
        this.allocator = new AllocatorImpl(this, this, ceiling, floorValues, defaultDistribution);
        this.assessorList = new AssessorListImpl(this);
    }

    @Override
    public Grade[] getAllGrades() {
        return NotenOld.ALL;
    }

    @Override
    public Grade[] getAllGradesReverseOrder() {
        return NotenOld.ALL_REV;
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        return parse(text);
    }

    static Grade parse(String text) throws GradeParsingException {
        String trimText = text.trim();
        for (Grade g : NotenOld.ALL) {
            if (g.getLongLabel().equalsIgnoreCase(trimText) || g.getShortLabel().equalsIgnoreCase(trimText)) {
                return g;
            }
        }
        throw new GradeParsingException(NAME, text);
    }

    @Override
    public Grade find(String id) {
        for (Grade g : NotenOld.ALL) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public Grade getFloorUnbiased() {
        return NotenOld.SECHS;
    }

    @Override
    public Grade getCeilingUnbiased() {
        return NotenOld.EINS;
    }

    @Override
    public Grade[] getAllGradesUnbiased() {
        return NotenOld.ALL_LINKED;
    }

    @Override
    public Distribution<Int2>[] getDefaultDistributions() {
        if (defaultDistributions == null) {
            defaultDistributions = new Distribution[]{
                Verteilungen.GLEICHMAESSIG, Verteilungen.RAHMENRICHTLINIEN
            };
        }
        return defaultDistributions;
    }

    public MarginModel getMarginModel() {
        return marginModel;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        try {
            JAXBContext ctx = JAXBContext.newInstance(NotenAssessmentXmlAdapter.class);
            NotenAssessmentXmlAdapter adapter = createAdapter();
            ctx.createMarshaller().marshal(adapter, os);
        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
            throw new IOException(ex);
        }
    }

    public NotenAssessmentXmlAdapter createAdapter() {
        Int2[] floorVal = new Int2[6];
        Iterator<Grade> it = linkedGradesIterator();
        for (int i = 0; i < 6; i++) {
            floorVal[i] = getAllocator().getFloor(it.next());
        }
        String defaultDist = null;
        for (final Distribution d : getDefaultDistributions()) {
            if (d.equals(getCurrentDistribution())) {
                defaultDist = d.toString();
            }
        }
        return new NotenAssessmentXmlAdapter(getRangeMaximum(), floorVal, getMarginModel().getMarginModel().toString(), getMarginModel().getMarginValue(), defaultDist);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Int2[] floorValues = new Int2[6];
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
        NotenAssessment ret = new NotenAssessment(this.ceiling, floorValues, marginModel.getMarginModel(), marginModel.getMarginValue(), defDist);
        return ret;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        super.addPropertyChangeListener(l);
        marginModel.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        super.removePropertyChangeListener(l);
        marginModel.removePropertyChangeListener(l);
    }

    @Override
    public JComponent getCustomizer() {
        if (userPanel != null) {
            return userPanel;
        } else {
            return userPanel = new NotenAssessToolPanel(this);
        }
    }

    @Override
    public String getName() {
        return NotenAssessment.NAME;
    }

    @Override
    public String getDisplayName() {
        return displayName();
    }

    static String displayName() throws MissingResourceException {
        return NbBundle.getMessage(NotenAssessment.class, "de.notensystem.displayName");
    }

}
