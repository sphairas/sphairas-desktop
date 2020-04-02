package org.thespheres.betula.niedersachsen.admin.ui.validate;

import java.io.IOException;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.validation.Validation;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.support.AbstractEngine;
import org.thespheres.betula.validation.support.ValidationEngine;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = ValidationEngine.class)
public class NdsVersetzungsEngine extends AbstractEngine<RequestProcessor> {

//    protected final XMLConfiguration config;

    public NdsVersetzungsEngine() throws IOException {
        super("niedersachsen.mover.engine", new RequestProcessor(NdsVersetzungsEngine.class.getName(), 8));
//        Parameters params = new Parameters();
//        FileObject file = FileUtil.getConfigFile("/ValidationEngine/Configuration/org-thespheres-betula-niedersachsen-admin-ui-validate-NdsVersetzungsEngine-config.xml");
//        try {
//            if (file != null) {
//                URL url = file.toURL();
//                FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
//                        .configure(params.xml()
//                                .setURL(url));
//                config = builder.getConfiguration();
//            } else {
//                throw new IOException("No configuration file.");
//            }
//        } catch (ConfigurationException ex) {
//            throw new IOException(ex);
//        }
    }

    @Override
    protected ValidationResultSet<?, ?> createValidationSet(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Validation<?, ?> unwrap(String id, ValidationResultSet<?, ?> set) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Validation<?, ?> createValidation(String validationId, Lookup context) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
