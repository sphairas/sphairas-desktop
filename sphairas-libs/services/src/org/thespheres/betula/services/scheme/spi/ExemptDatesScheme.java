/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme.spi;

import java.time.LocalDate;

/**
 *
 * @author boris.heithecker
 */
public interface ExemptDatesScheme extends Schedule {

    public static final String HOLIDAYS = "holidays";

    public boolean isExemptDate(LocalDate date);

}
