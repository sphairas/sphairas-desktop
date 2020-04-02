package org.thespheres.betula.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class DeweyDecimal {

    private final int[] cmp;
    private static final String PATTERN = "yyyyMMddHHmmssSSS";
    static final DateTimeFormatter REVISION = DateTimeFormatter.ofPattern(PATTERN);
    private LocalDateTime revision;

    public DeweyDecimal(final int[] components) {
        this.cmp = new int[components.length];
        System.arraycopy(components, 0, this.cmp, 0, components.length);
    }

    public DeweyDecimal(final String string) throws NumberFormatException, DateTimeParseException {
        final String[] token = string.split("\\.");
        cmp = new int[token.length];

        for (int i = 0; i < cmp.length; i++) {
            final String component = token[i];
            if (component.equals("")) {
                throw new NumberFormatException("Empty Dewey decimal component.");
            }
            //TODO last component can be revision date-time
            if (i == cmp.length - 1 && component.length() == PATTERN.length()) {
                revision = LocalDateTime.parse(component, REVISION);
                break;
            }
            cmp[i] = Integer.parseInt(component);
        }
    }

    public int getSize() {
        return cmp.length;
    }

    public int getOrZero(final int index) {
        return index < cmp.length ? cmp[index] : 0;
    }

    public LocalDateTime getRevision() {
        return revision;
    }

    public boolean isEqual(final DeweyDecimal other) {
        final int max = Math.max(other.cmp.length, cmp.length);
        for (int i = 0; i < max; i++) {
            final int component1 = (i < cmp.length) ? cmp[i] : 0;
            final int component2 = (i < other.cmp.length) ? other.cmp[i] : 0;

            if (component2 != component1) {
                return false;
            }
        }
        return Objects.equals(revision, other.revision);
    }

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(".");
        Arrays.stream(cmp)
                .mapToObj(Integer::toString)
                .forEach(sj::add);
        if(revision != null) {
            sj.add(REVISION.format(revision));
        }
        return sj.toString();
    }
}
