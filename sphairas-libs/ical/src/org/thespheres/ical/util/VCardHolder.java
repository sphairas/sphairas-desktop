/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Objects;
import org.thespheres.ical.VCard;

/**
 *
 * @author boris.heithecker
 */
public class VCardHolder {

    private String directoryName;
    private String gender;
    private final VCard vCard;
    private String birthplace;
    private LocalDate bDay;
    private N n;

    public VCardHolder(VCard vCard) {
        this.vCard = vCard;
    }

    public VCard getVCard() {
        return vCard;
    }

    public String getDirectoryName() {
        if (directoryName == null) {
            directoryName = vCard.getFN();
        }
        return directoryName;
    }

    public String getGivenNames() {
        return getN().getGivenNames();
    }

    public String getFirstName() {
        return getN().getFirstName();
    }

    public String getSurname() {
        return getN().getFamilyName();
    }

    public String getFullName() {
        return getN().getGivenNames() + " " + getN().getFamilyName();
    }

    public String getGender() {
        if (gender == null) {
            gender = vCard.getAnyPropertyValue(VCard.GENDER).get();
        }
        return gender;
    }

    public LocalDate getBDay() {
        if (bDay == null) {
            String v = vCard.getAnyPropertyValue(VCard.BDAY).get();
            try {
                bDay = LocalDate.parse(v, IComponentUtilities.DATE_FORMATTER);
            } catch (DateTimeParseException e) {
            }
        }
        return bDay;
    }

    public String getBirthplace() {
        if (birthplace == null) {
            birthplace = vCard.getAnyPropertyValue(VCard.BIRTHPLACE).orElse("UNBEKANNT");
        }
        return birthplace;

    }

    private N getN() {
        if (n == null) {
            n = vCard.getAnyPropertyValue(VCard.N)
                    .map(N::new)
                    .get();
        }
        return n;
    }

    public boolean equalValues(VCardHolder other) {
        if (other == null) {
            return false;
        }
        if (!Objects.equals(getFullName(), other.getFullName())) {
            return false;
        }
        if (!Objects.equals(getN(), other.getN())) {
            return false;
        }
        if (!Objects.equals(getGender(), other.getGender())) {
            return false;
        }
        if (!Objects.equals(getBirthplace(), other.getBirthplace())) {
            return false;
        }
        return Objects.equals(getBDay(), other.getBDay());
    }

    @Override
    public String toString() {
        return getFullName();
    }

    private final class N {

        private final String[] value;
        private String given;
        private String family;
        private String search;

        private N(String value) {
            this.value = value.split(";");
        }

        private String getFirstName() {
            return getGivenNames().split(" ")[0];
        }

        private String getGivenNames() {
            if (given == null) {
                given = value[1].replace(",", " ");
            }
            return given;
        }

        private String getFamilyName() {
            if (family == null) {
                family = value[0];
            }
            return family;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + Arrays.deepHashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final N other = (N) obj;
            return Arrays.deepEquals(this.value, other.value);
        }

    }
}
