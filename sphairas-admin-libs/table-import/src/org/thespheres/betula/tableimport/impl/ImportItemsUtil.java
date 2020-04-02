/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.model.XmlItem;
import org.thespheres.betula.xmlimport.model.XmlSigneeItem;
import org.thespheres.betula.xmlimport.model.XmlStudentItem;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;

/**
 *
 * @author boris.heithecker
 */
class ImportItemsUtil {

    static final String DIRNAMEJOINER = ", ";
    static final char DIRNAMESEPARATOR = ',';

    static ImportStudentKey createImportStudentKey(final XmlStudentItem xmls) {
        final String sourceName = StringUtils.strip(xmls.getSourceName());
        if (StringUtils.isBlank(sourceName)) {
            //Log
            return null;
        }
        final String dirName;
        if (StringUtils.countMatches(sourceName, DIRNAMEJOINER) == 1) {
            dirName = sourceName;
        } else {
            final StringJoiner sj = new StringJoiner(DIRNAMEJOINER);
            sj.add(sourceName);
            if (!StringUtils.isBlank(xmls.getSourceGivenNames())) {
                sj.add(xmls.getSourceGivenNames().trim());
            } else {
                for (XmlItem.SourceElement se : xmls.getSource()) {
                    if (isGivenName(se)) {
                        sj.add(se.getValue().trim());
                    }
                }
            }
            dirName = sj.toString();
        }
        final XmlItem.SourceDateTime dob = xmls.getDateOfBirth();
        if (dob != null) {
            final String sd = dob.getSourceDate();
            final LocalDate zdt = LocalDate.parse(sd, TargetItemsXmlCsvItem.DTF);
            return new ImportStudentKey(dirName, sd, zdt);
        } else {
            return new ImportStudentKey(dirName);
        }
    }

    static String findN(final XmlStudentItem xmls) {
        final String sourceName = StringUtils.strip(xmls.getSourceName());
        if (StringUtils.isBlank(sourceName)) {
            return null;
        }
        final String[] split = StringUtils.split(sourceName, DIRNAMESEPARATOR);
        final String family;
        if (split.length > 1) {
            family = split[0].trim();
        } else {
            family = sourceName.trim();
        }
        final String[] given;
        final String sourceGivenNames = xmls.getSourceGivenNames();
        if (!StringUtils.isBlank(sourceGivenNames)) {
            given = StringUtils.split(sourceGivenNames);
        } else if (split.length > 1) {
            given = StringUtils.split(split[1].trim());
        } else {
            given = Arrays.stream(xmls.getSource())
                    .filter(ImportItemsUtil::isGivenName)
                    .map(se -> StringUtils.split(se.getValue()))
                    .collect(CollectionUtil.singleOrNull());
        }
        final String garr = Arrays.stream(given)
                .collect(Collectors.joining(","));
        return family + ";" + garr + ";;;";
    }

    @Messages({"ImportItemsUtil.findFNfromN={1}, {0}"})
    static String findFNfromN(final String n) {
        final String[] el = n.split(";");
        final StringJoiner ret = new StringJoiner(" ");
        Arrays.stream(el[1].split(","))
                .forEach(ret::add);
        return NbBundle.getMessage(ImportItemsUtil.class, "ImportItemsUtil.findFNfromN", ret.toString(), el[0]);
    }

    private static boolean isGivenName(XmlItem.SourceElement se) {
        return StringUtils.containsIgnoreCase(se.getSourceDefinition(), "vorname")
                || StringUtils.containsIgnoreCase(se.getSourceDefinition(), "rufname");
    }

    public static String findGender(final String source) {
        final String gender = StringUtils.stripToNull(source).toUpperCase();
        if (gender != null) {
            switch (gender) {
                case "W":
                    return "F";
                case "M":
                    return "M";
            }
        }
        return null;
    }

    static String findSigneeName(final XmlSigneeItem xmls) {
        final String sourceName = StringUtils.strip(xmls.getSourceName());
        if (!StringUtils.isBlank(sourceName)) {
            if (StringUtils.countMatches(sourceName, DIRNAMEJOINER) == 1) {
                return StringUtils.trim(sourceName);
            } else {
                final StringJoiner sj = new StringJoiner(DIRNAMEJOINER);
                sj.add(StringUtils.trim(sourceName));
                if (!StringUtils.isBlank(xmls.getSourceGivenNames())) {
                    sj.add(StringUtils.trim(xmls.getSourceGivenNames()));
                } else {
                    for (final XmlItem.SourceElement se : xmls.getSource()) {
                        if (isGivenName(se)) {
                            final String v = StringUtils.trimToNull(se.getValue());
                            if (v != null) {
                                sj.add(v);
                            }
                        }
                    }
                }
                return sj.toString();
            }
        }
        return null;
    }

}
