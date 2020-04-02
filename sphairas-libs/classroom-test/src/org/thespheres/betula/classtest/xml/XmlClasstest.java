package org.thespheres.betula.classtest.xml;

import java.util.HashMap;
import org.thespheres.betula.classtest.ClassTest;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.Student;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "classtest", namespace = "http://www.thespheres.org/xsd/betula/classtest.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlClasstest implements ClassTest<XmlProblem, StudentScoresImpl> {

    @XmlElement(name = "record")
    private RecordId rec;
    @XmlElement(name = "problems")
    @XmlJavaTypeAdapter(value = ProblemsAdapter.class)
    protected Map<String, XmlProblem> problems  = new HashMap<>();
    @XmlElement(name = "students")
    @XmlJavaTypeAdapter(value = StudentsAdapter.class)
    protected Map<Student, StudentScoresImpl> students  = new HashMap<>();

    @Override
    public RecordId getRecord() {
        return rec;
    }

    public void setRecord(RecordId r) {
        this.rec = r;
    }

    @Override
    public Map<String, XmlProblem> getProblems() {
        return problems;
    }

    @Override
    public Map<Student, StudentScoresImpl> getStudentScores() {
        return students;
    }
}
