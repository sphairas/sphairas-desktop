/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.couchdb.users.impl;

import java.io.IOException;
import org.apache.commons.lang3.RandomStringUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.DbAccessException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.couchdb.CouchDBAdminInstance;
import org.thespheres.betula.couchdb.users.impl.DesignDoc.View;

/**
 *
 * @author boris.heithecker
 */
@Messages({"GenDBTask.createUser.success=Benutzer {0} angelegt.",
    "GenDBTask.createDB.success=Benutzer-Datenbank {1} für {0} angelegt.",
    "GenDBTask.addSecurity.success=Rechte für {0} geschrieben."})
public class GenDBTask implements Runnable {

    private final CouchDBUser user;
    private final CouchDBAdminInstance db;
    private String userDB;

    public GenDBTask(CouchDBUser user, CouchDBAdminInstance db) {
        this.user = user;
        this.db = db;
    }

    public CouchDBAdminInstance getCouchDBAdminInstance() {
        return db;
    }

    public CouchDBUser getCoucDBUser() {
        return user;
    }

    public String getUserDB() {
        return userDB;
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch (IOException ex) {
            ex.printStackTrace(Util.getIO().getErr());
        }
    }

    private void runImpl() throws IOException {
        final CouchDbConnector connector = db.getInstance().createConnector("_users", false);
        try {
            if (!connector.contains(user.getCouchDBUserID())) {
                connector.create(user);
                String msg = NbBundle.getMessage(GenDBTask.class, "GenDBTask.createUser.success", user.getUser());
                Util.getIO().getOut().println(msg);
            }

            String n;
            while (db.getInstance().checkIfDbExists(n = findName())) {
            }
            CouchDbConnector c = db.getInstance().createConnector(n, true);
            userDB = n;
            String msg = NbBundle.getMessage(GenDBTask.class, "GenDBTask.createDB.success", user.getUser(), userDB);
            Util.getIO().getOut().println(msg);

            CouchDBSecurity security = new CouchDBSecurity(user);
            c.create(security);

            DesignDoc timeDocDesign = new DesignDoc("TimeDoc2");
            timeDocDesign.getViews().put("findByTargetId", new View("function(doc) {if(doc.target) emit(doc.target, doc); }"));
            c.create(timeDocDesign);

            DesignDoc appDesign = new DesignDoc("app");
            appDesign.getViews().put("clientView", new View("function(doc) {if(!doc._id.startsWidth('_design')) emit(doc._id); }"));
            c.create(appDesign);

            msg = NbBundle.getMessage(GenDBTask.class, "GenDBTask.addSecurity.success", user.getUser());
            Util.getIO().getOut().println(msg);

        } catch (DbAccessException ex) {
            throw new IOException(ex);
        }
    }

    private static String findName() {
        String ret = RandomStringUtils.randomAlphabetic(1) + RandomStringUtils.randomAlphanumeric(29);
        return ret.toLowerCase();
    }

}
