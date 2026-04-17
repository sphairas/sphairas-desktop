package org.thespheres.betula.schulconnex;

import de.schulconnex.qs.model.Name;
import de.schulconnex.qs.model.Person;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author boris.heithecker
 */
public class SchulconnexUtil {

    public static String createSortableName(final Name name) {
        String ret = "";
        if (!StringUtils.isBlank(name.getTitel())) {
            ret += name.getTitel();
            ret += " ";
        }
        ret = ret + name.getFamilienname() + ", " + name.getVorname();
        if (name.getNamenssuffix() != null && !name.getNamenssuffix().isEmpty()) {
            final String suffix = name.getNamenssuffix().stream()
                    .collect(Collectors.joining(" "));
            ret = ret + suffix;
        }
        return ret;
    }

    static String findN(final Person person) {
        final String family = person.getName().getFamilienname();
        final String sourceGivenNames = person.getName().getVorname();
        final String[] given = StringUtils.split(sourceGivenNames);
        final String garr = Arrays.stream(given)
                .collect(Collectors.joining(","));
        return family + ";" + garr + ";;;";
    }

    static String findFNfromN(final String n) {
        final String[] el = n.split(";");
        final StringJoiner ret = new StringJoiner(" ");
        Arrays.stream(el[1].split(","))
                .forEach(ret::add);
        return el[0] + ", " + ret.toString();
    }

    static String findGender(final Person person) {
        final String gv = Optional.ofNullable(person.getGeschlecht())
                .map(String::toUpperCase)
                .orElse(null);
        switch (gv) {
            case "W":
                return "F";
            case "M":
                return "M";
        }
        return null;
    }
}
