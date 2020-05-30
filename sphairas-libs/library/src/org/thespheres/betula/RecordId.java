/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

import java.io.Serializable;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.document.util.Identities;

/**
 *
 * @author boris.heithecker
 */
@XmlType(name = "recordIdType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public final class RecordId extends Identity<String> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final Pattern PATTERN = Pattern.compile("\\d\\d\\d\\d-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1](T([0-1][1-9]|2[0-3])|P\\d\\d\\d?\\?))");
    private static final int PERIOD_MAXIMUM_VALUE = 9999;
    private static final NumberFormat PF = NumberFormat.getIntegerInstance(Locale.getDefault());

    static {
        PF.setMinimumIntegerDigits(2);
        PF.setMaximumIntegerDigits(4);
    }

    @XmlAttribute(name = "id", required = true)
    private String id;
    @XmlAttribute(name = "authority", required = true)
    private String authority;
    private transient Object parsed;

    //JAXB only!
    public RecordId() {
    }

    public RecordId(String authority, String id) {
        if (!PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException();
        }
        final char disc = id.charAt(10);
        if ('T' == disc) {
            TemporalAccessor par;
            try {
                par = DTF.parse(id);
                this.id = DTF.format(par);
                this.authority = authority;
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(e);
            }
        } else if ('P' == disc) {
            TemporalAccessor ps;
            try {
                String pval = id.substring(11);
                int period = Integer.parseInt(pval);
                ps = DF.parse(id);
                String p = Integer.toString(period, 10);
                this.id = DF.format(ps) + "P" + p;
                this.authority = authority;
            } catch (DateTimeParseException | NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public RecordId(String authority, LocalDateTime ldt) {
        this.id = DTF.format(ldt);
        this.authority = authority;
    }

    public RecordId(String authority, LocalDate ld, int period) {
        if (period < 0 || period > PERIOD_MAXIMUM_VALUE) {
            throw new IllegalArgumentException("Period must not be 0 < period < " + PERIOD_MAXIMUM_VALUE);
        }
        String p = Integer.toString(period, 10);
        this.id = DF.format(ld) + "P" + p;
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     *
     * @return The PeriodId respresentation, e. g. "5" or
     * "5@demo/class-schedule-2019"
     */
    public String getPeriodRepresentation() {
        return getParsed().period;
    }

    public int getPeriod() {
        return getParsed().getPeriod();
    }

    public LocalDate getLocalDate() {
        return getParsed().ld != null ? getParsed().ld : getParsed().ldt.toLocalDate();
    }

    public LocalDateTime getLocalDateTime() {
        return getParsed().ldt;
    }

    private synchronized Parsed getParsed() {
        if (parsed == null) {
            final char disc = id.charAt(10);
            switch (disc) {
                case 'T':
                    try {
                    LocalDateTime val = LocalDateTime.parse(id, DTF);
                    parsed = new Parsed(val);
                } catch (DateTimeParseException e) {
                    parsed = e;
                }
                break;
                case 'P':
                    try {
                    String pval = id.substring(10);
                    LocalDate val = LocalDate.parse(pval.substring(0, 10), DF);
                    parsed = new Parsed(val, pval);
                } catch (DateTimeParseException | NumberFormatException e) {
                    throw new IllegalArgumentException(e);
                }
                break;
                default:
                    parsed = new IllegalStateException();
                    break;
            }
        }
        if (parsed instanceof RuntimeException) {
            throw (RuntimeException) parsed;
        }
        return (Parsed) parsed;
    }

    //Legacy cases
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        try {
            long d = Long.parseLong(id);
            LocalDateTime date = new Date(d).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            this.id = DTF.format(date);
        } catch (NumberFormatException nex) {
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.authority);
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
        final RecordId other = (RecordId) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.authority, other.authority);
    }

    private static class Parsed {

        private final String period;
        private final LocalDate ld;
        private final LocalDateTime ldt;

        private Parsed(LocalDateTime ldt) {
            this.period = null;
            this.ld = null;
            this.ldt = ldt;
        }

        private Parsed(LocalDate ld, String period) {
            this.period = period;
            this.ld = ld;
            this.ldt = null;
        }

        private int getPeriod() {
            if (StringUtils.isBlank(period)) {
                return 0;
            }
            int[] ret = new int[1];
            Identities.<Identity<Integer>>parse(period, (a, i, v) -> {
                try {
                    ret[0] = Integer.parseInt(i);
                } catch (NumberFormatException nfex) {
                }
                return null;
            });
            return ret[0];
        }

    }
}
