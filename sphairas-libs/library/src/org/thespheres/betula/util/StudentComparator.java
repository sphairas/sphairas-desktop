/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Student;

/**
 *
 * @author Boris Heithecker
 */
@Messages({"StudentComparator.Adelspraedikate=von der,von,van der,van,de"})
public class StudentComparator implements Comparator<Student> {

    private final Comparator<Student> delegate = Comparator.comparing(this::sortStringFromStudent, Collator.getInstance(Locale.getDefault()));
    private final static String WHITEREGEX = "\\p{javaWhitespace}*";
//    private final static String[] ADELSPAEDIKATE = {"von der", "von", "van", "de"}; //"von der" vor (!) "von"
    private final static String[] ADELSPAEDIKATE = NbBundle.getMessage(StudentComparator.class, "StudentComparator.Adelspraedikate").split(",");

    @Override
    public int compare(Student s1, Student s2) {
        return delegate.compare(s1, s2);
    }

    private String sortStringFromStudent(Student student) {
        if (student == null) {
            return "";
        }
        String s = student.getDirectoryName();
        s = s != null ? s : student.getStudentId().toString();
        return sortStringFromDirectoryName(s);
    }

    public static String sortStringFromDirectoryName(String studentDirName) {
        String ret = StringUtils.trimToEmpty(studentDirName);
        for (final String ap : ADELSPAEDIKATE) {
            if (StringUtils.startsWithIgnoreCase(ret, ap)) {
                ret = StringUtils.removeStart(ret, ap);
                break;
            }
        }
        ret = ret.replaceAll(WHITEREGEX, "");
        return ret.replace(",", "").replace(";", "");
    }
}
