/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.unit;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.util.XmlStudent;

/**
 *
 * @author boris.heithecker
 */
public class LocalStudents {

    private static LocalStudents instance;
    private final LocalDateTime base;
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private final Long[] COUNT = new Long[]{1l};
    private final NumberFormat NF = NumberFormat.getInstance(Locale.getDefault());

    private LocalStudents() {
        base = LocalDateTime.now();
        NF.setMinimumIntegerDigits(5);
        NF.setGroupingUsed(false);
    }

    public static LocalStudents getInstance() {
        synchronized (LocalStudents.class) {
            if (instance == null) {
                instance = new LocalStudents();
            }
            return instance;
        }
    }
    
    public Student newStudent(String dirname) {
        long num;
        synchronized(COUNT) {
            num = COUNT[0]++;
        }
        final String idtext = base.format(DTF) + NF.format(num);
        Long id = Long.valueOf(idtext);
        StudentId sid = new StudentId("local", id);
        return new XmlStudent(sid, dirname);
    }
}
