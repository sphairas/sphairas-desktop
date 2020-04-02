/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.ks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.ui.KeyStores;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = OptionProcessor.class)
public class PasswordOptionsParser extends OptionProcessor {

    private final Option pwOption = Option.requiredArgument(Option.NO_SHORT_NAME, "password");
    private final Option fileOption = Option.requiredArgument(Option.NO_SHORT_NAME, "password-file");
    private final Option savePassword = Option.withoutArgument(Option.NO_SHORT_NAME, "save-password");

    @Override
    protected Set<Option> getOptions() {
        return Set.of(pwOption, fileOption, savePassword);
    }

    //Not called if the command line contains one or more parameters unknown to the application
    //https://netbeans.org/bugzilla/show_bug.cgi?id=193649
    @Override
    protected void process(final Env env, final Map<Option, String[]> m) throws CommandException {
        final String[] pw = m.get(pwOption);
        final String[] file = m.get(fileOption);
        if (pw != null && file != null) {
            throw new CommandException(-1, "be");
        }
        final String password;
        if (pw != null) {
            password = pw[0];
        } else if (file != null) {
            final Path p = env.getCurrentDirectory().toPath().resolve(file[0]);
            final List<String> ll;
            try {
                ll = Files.readAllLines(p);
            } catch (IOException ex) {
                throw new CommandException(-1, "be");
            }
            password = ll.stream()
                    .filter(l -> StringUtils.isNotBlank(l))
                    .findFirst()
                    .orElseThrow(() -> new CommandException(-1, "be"));
        } else {
            return;
        }
        System.out.println("Setting password: " + password );
        final boolean save = m.get(savePassword) != null;
        if (save) {
//            Keyring.save(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY, password.toCharArray(), null);
        }
    }

}
