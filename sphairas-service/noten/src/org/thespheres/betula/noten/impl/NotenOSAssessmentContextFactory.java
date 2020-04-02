/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;
import org.thespheres.betula.util.Int2;

/**
 *
 * @author Boris Heithecker
 */
public class NotenOSAssessmentContextFactory implements AssessmentContext.Factory<StudentId, Int2> {

    public Grade parseGrade(String text) throws GradeParsingException {
        return NotenOSAssessment.parse(text);
    }


    @Override
    public AssessmentContext<StudentId, Int2> create() {
        return new NotenOSAssessment(Int2.fromInternalValue(20));
    }

    protected AssessmentContext createAssessmentContext(Int2 max, Int2[] floorValues, String defDist) {
        AssessmentContext ret = new NotenOSAssessment(max, floorValues, defDist);
        return ret;
    }

//    @Override
//    public AssessmentContext<StudentId, Int2> read(org.w3c.dom.Document doc) throws IOException {
//        try {
//            JAXBContext ctx = JAXBContext.newInstance(NotenOSAssessmentContextXmlAdapter.class);
//            NotenOSAssessmentContextXmlAdapter adapter = (NotenOSAssessmentContextXmlAdapter) ctx.createUnmarshaller().unmarshal(doc);
//            Int2[] floorValues = adapter.getFloorValues();
//            Int2 max = adapter.getRangeMaximum();
//            String defDist = adapter.getDefaultDistribtution();
//            AssessmentContext ret = createAssessmentContext(max, floorValues, defDist);
//            return ret;
//        } catch (JAXBException ex) {
//            throw new IOException(ex);
//        }
//    }

    @Override
    public String getName() {
        return NotenOSAssessment.NAME;
    }

    @Override
    public String getDisplayName() {
        return NotenOSAssessment.displayName();
    }

//    @Override
//    public String namespaceURI() {
//        return null;
//    }
//
//    @Override
//    public String documentElementLocalName() {
//        return NotenOSAssessmentContextXmlAdapter.LOCALNAME;
//    }
}
