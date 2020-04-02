package org.thespheres.betula.document.model;

import java.util.List;
import java.util.Set;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 * @param <S>
 * @param <R>
 */
//Key: primaryUnit
public interface History<S extends Student, R extends ReportDocument> {

    public List<S> getStudents();

    public Set<DocumentId> getReportDocuments(StudentId student);

    public R getReportDocument(final DocumentId did);

}
