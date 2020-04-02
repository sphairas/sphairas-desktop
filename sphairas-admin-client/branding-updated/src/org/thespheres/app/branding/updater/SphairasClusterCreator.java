package org.thespheres.app.branding.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;
import org.openide.util.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author boris.heithecker
 * 
 * This file has been created as an adapted version
 * of org.netbeans.modules.updatecenters.resources.NetBeansClusterCreator
 * licensed under the Common
 * Development and Distribution License("CDDL")
 */
@ServiceProvider(service = AutoupdateClusterCreator.class)
public class SphairasClusterCreator extends AutoupdateClusterCreator {

    protected @Override
    File findCluster(String clusterName) {
        AtomicReference<File> parent = new AtomicReference<>();
        File conf = findConf(parent, new ArrayList<>());
        return conf != null 
                && conf.isFile() 
                && Files.isWritable(conf.toPath()) ? new File(parent.get(), clusterName) : null;
    }

    private static File findConf(AtomicReference<File> parent, List<? super File> clusters) {
        String nbdirs = System.getProperty("netbeans.dirs");
        if (nbdirs != null) {
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator); // NOI18N
            while (tok.hasMoreElements()) {
                File cluster = new File(tok.nextToken());
                clusters.add(cluster);
                if (!cluster.exists()) {
                    continue;
                }

                if (parent.get() == null) {
                    parent.set(cluster.getParentFile());
                }

                if (!parent.get().equals(cluster.getParentFile())) {
                    // we can handle only case when all clusters are in
                    // the same directory these days
                    return null;
                }
            }
        }
        
        final String name = NbBundle.getMessage(SphairasClusterCreator.class, "clusters.config.filename");
        return new File(new File(parent.get(), "etc"), name);
    }

    @Override
    protected File[] registerCluster(String clusterName, File cluster) throws IOException {
        AtomicReference<File> parent = new AtomicReference<>();
        List<File> clusters = new ArrayList<>();
        File conf = findConf(parent, clusters);
        clusters.add(cluster);
        Properties p = new Properties();
        try (InputStream is = new FileInputStream(conf)) {
            p.load(is);
        }
        if (!p.keySet().contains(clusterName)) {
            try (OutputStream os = new FileOutputStream(conf, true)) {
                os.write('\n');
                os.write(clusterName.getBytes());
                os.write('\n');
            }
        }
        return clusters.toArray(new File[clusters.size()]);
    }

}
