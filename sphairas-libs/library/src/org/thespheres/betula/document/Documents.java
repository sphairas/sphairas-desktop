/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import org.thespheres.betula.document.DocumentId.Version;
import org.thespheres.betula.util.DeweyDecimal;

/**
 *
 * @author boris.heithecker
 */
public class Documents {

    private Documents() {
    }

    public static DocumentId.Version inc(final Version version, int index) {
        DeweyDecimal dd = null;
        try {
            dd = new DeweyDecimal(version.getVersion());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
        int[] ret = new int[Math.max(index + 1, dd.getSize())];
        for (int i = 0; i < ret.length; i++) {
            int old = dd.getOrZero(i);
            ret[i] = i != index ? old : ++old;
        }
        return new Version(new DeweyDecimal(ret));
    }

    public static int size(final Version version) {
        DeweyDecimal dd = null;
        try {
            dd = new DeweyDecimal(version.getVersion());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
        return dd.getSize();
    }

    public static int compare(final Version v1, final Version v2) {
        final int max = Math.max(size(v1), size(v2));
        final DeweyDecimal dd1 = new DeweyDecimal(v1.getVersion());
        final DeweyDecimal dd2 = new DeweyDecimal(v2.getVersion());
        int c = 0;
        int i = 0;
        while (c == 0 && i < max) {
            c = dd1.getOrZero(i) - dd2.getOrZero(i);
            ++i;
        }
        return c;
    }

}
