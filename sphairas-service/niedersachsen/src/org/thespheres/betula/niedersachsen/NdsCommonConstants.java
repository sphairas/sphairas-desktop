/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import org.thespheres.betula.services.LocalFileProperties;

/**
 *
 * @author boris.heithecker
 */
public class NdsCommonConstants {

    public static final String DEFAULT_NAMES_AUTHORITY = "thespheres.org/default-names/de.niedersachsen";
    public static final String SUFFIX_AV = "arbeitsverhalten";
    public static final String SUFFIX_ZEUGNISNOTEN = "zeugnisnoten";
    public static final String SUFFIX_SV = "sozialverhalten";
    public static final String SUFFIX_BERICHTE = "berichte";
//    public static final String PROP_CROSSMARK_SUBJECT_CONVENTIONS = "crossmark.subject.conventions";
    public static final String ANKREUZZEUGNISSE_FILE = "public/Ankreuzzeugnisse.xml";

    public static LocalFileProperties getDefaultProperties() {
        return NdsDefaultProperties.get();
    }
}
