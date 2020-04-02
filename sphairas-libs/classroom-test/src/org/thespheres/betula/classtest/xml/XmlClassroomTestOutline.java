/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.classtest.model.ClassroomTestOutline;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableProblem;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "classtest-outline", namespace = "http://www.thespheres.org/xsd/betula/classtest.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlClassroomTestOutline implements ClassroomTestOutline<XmlProblem> {

    @Override
    public XmlProblem createProblem(EditableClassroomTest<?, ?, ?> test, EditableProblem<?> parent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
