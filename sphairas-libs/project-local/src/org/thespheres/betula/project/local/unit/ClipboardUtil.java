/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.unit;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.Student;

/**
 *
 * @author boris.heithecker
 */
class ClipboardUtil {

    static List<Student> getContent() {
        List<Student> ret = new ArrayList<>();
        String data;
        try {
            data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException ex) {
            return ret;
        }
        String[] lines = StringUtils.trimToEmpty(data).split("\n");
        for (String line : lines) {
            String dirname = null;
            String unit;
            String cols[] = StringUtils.trimToEmpty(line).split("\t");
            if (cols[0].contains(",")) {
                dirname = cols[0].trim();
                if (cols.length > 1) {
                    unit = cols[1].trim();
                }
            } else if (cols.length > 1) {
                String given = cols[0].trim();
                dirname = cols[1].trim() + ", " + given;
                if (cols.length > 2) {
                    unit = cols[2].trim();
                }
            }
            if (StringUtils.isNoneBlank(dirname)) {
                Student ns = LocalStudents.getInstance().newStudent(dirname);
                ret.add(ns);
            }
        }
        return ret;
    }
}
