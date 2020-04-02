/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import org.thespheres.betula.StudentId;
import org.thespheres.betula.util.Int2;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;
import org.thespheres.betula.noten.impl.MarginModel.Model;

/**
 *
 * @author Boris Heithecker
 */
public class NotenAssessmentContextFactory implements AssessmentContext.Factory<StudentId, Int2> {

    @Override
    public String getName() {
        return NotenAssessment.NAME;
    }

    @Override
    public String getDisplayName() {
        return NotenAssessment.displayName();
    }

    public Grade parseGrade(String text) throws GradeParsingException {
        return NotenAssessment.parse(text);
    }

    @Override
    public AssessmentContext<StudentId, Int2> create() {
        return new NotenAssessment(Int2.fromInternalValue(20));
    }

    protected AssessmentContext createAssessmentContext(Int2 max, Int2[] floorValues, Model m, Int2 marginValue, String defDist) {
        AssessmentContext ret = new NotenAssessment(max, floorValues, m, marginValue, defDist);
        return ret;
    }

//    @Override
//    public AssessmentContext<StudentId, Int2> read(org.w3c.dom.Document doc) throws IOException {
//        try {
//            JAXBContext ctx = JAXBContext.newInstance(NotenAssessmentXmlAdapter.class);
//            NotenAssessmentXmlAdapter adapter = (NotenAssessmentXmlAdapter) ctx.createUnmarshaller().unmarshal(doc);
//            MarginModel.Model m = MarginModel.Model.valueOf(adapter.getMarginModel());
//            Int2 marginValue = adapter.getMarginValue();
//            Int2[] floorValues = adapter.getFloorValues();
//            Int2 max = adapter.getRangeMaximum();
//            String defDist = adapter.getDefaultDistribtution();
//            AssessmentContext ret = createAssessmentContext(max, floorValues, m, marginValue, defDist);
//            return ret;
//        } catch (JAXBException ex) {
//            throw new IOException(ex);
//        }
//    }
//
//    @Override
//    public String namespaceURI() {
//        return null;
//    }
//
//    @Override
//    public String documentElementLocalName() {
//        return NotenAssessmentXmlAdapter.LOCALNAME;
//    }
}
