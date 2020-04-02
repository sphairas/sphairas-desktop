/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical;

/**
 *
 * @author boris.heithecker
 */
public interface VCard extends IComponent<CardComponentProperty> {

    public static String VCARD = "VCARD";
    public static String FN = "FN";
    public static String N = "N";
    public static String GENDER = "GENDER";
    public static String BDAY = "BDAY";
    public static String BIRTHPLACE = "BIRTHPLACE";
    public static String EMAIL = "EMAIL";

    public String getFN();

}
