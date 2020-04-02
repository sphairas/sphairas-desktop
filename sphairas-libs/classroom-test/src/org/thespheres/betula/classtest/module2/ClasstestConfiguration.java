/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.module2;

import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
@Messages({"ClasstestConfiguration.findProblemName.hint={0}{1}{2}.)"})
public class ClasstestConfiguration {

    private ClasstestConfiguration() {
    }

    public static String findProblemDisplayName(final int numProblem, final String prefix, final String suffix) {
        final String prepend = StringUtils.trimToEmpty(prefix);
        final String add = StringUtils.trimToEmpty(suffix);
        return NbBundle.getMessage(ClasstestConfiguration.class, "ClasstestConfiguration.findProblemName.hint", prepend, Integer.toString(numProblem), add);
    }

    public static boolean useLongLabel() {
        return true;
    }
}
