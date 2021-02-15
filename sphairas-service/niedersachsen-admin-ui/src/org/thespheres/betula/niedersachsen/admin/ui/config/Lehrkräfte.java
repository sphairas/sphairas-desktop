/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.config;

import org.jdesktop.swingx.JXComboBox;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.services.ws.CommonDocuments;
import org.thespheres.betula.ui.ConfigurationPanelComponent;
import org.thespheres.betula.ui.ConfigurationPanelComponentProvider;
import org.thespheres.betula.ui.ConfigurationPanelContentTypeRegistration;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class Lehrkräfte {

    static final HeadTeachersType PRIMARY_UNIT = new HeadTeachersType(CommonDocuments.PRIMARY_UNIT_HEAD_TEACHERS_DOCID, "primaryUnit", null);
    static final HeadTeachersType TUTANDEN = new HeadTeachersType("tutor-teachers-documentid", "tutanden", new AbstractMarker("betula-db", "tutanden", null));

    //Klassenlehrer
    @NbBundle.Messages({"KlassenlehrerRegistration.createComboBox.name=Klassenlehrer/Klassenlehrerin"})
    @ConfigurationPanelContentTypeRegistration(contentType = "RemoteSignee", position = 5000)
    public static class KlassenlehrerRegistration implements ConfigurationPanelComponentProvider {

        @Override
        public ConfigurationPanelComponent createConfigurationPanelComponent() {
            final JXComboBox cb = new JXComboBox();
            final String n = NbBundle.getMessage(KlassenlehrerRegistration.class, "KlassenlehrerRegistration.createComboBox.name");
            cb.setName(n);
            return new HeadTeacherConfigurationPanel(cb, PRIMARY_UNIT);
        }

    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/application/betula-unit-data/Lookup")
    public static final class KlassenlehrerModelRegistration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(final Lookup base) {
            final PrimaryUnitOpenSupport puos = base.lookup(PrimaryUnitOpenSupport.class);
            if (puos != null) {
                puos.getRP().post(() -> HeadTeachers.load(puos, PRIMARY_UNIT));
            }
            return Lookup.EMPTY;
        }
    }

    //Tutoren
    @NbBundle.Messages({"TutorRegistration.createComboBox.name=Tutor/Tutorin"})
    @ConfigurationPanelContentTypeRegistration(contentType = "RemoteSignee", position = 6000)
    public static class TutorRegistration implements ConfigurationPanelComponentProvider {

        @Override
        public ConfigurationPanelComponent createConfigurationPanelComponent() {
            final JXComboBox cb = new JXComboBox();
            final String n = NbBundle.getMessage(TutorRegistration.class, "TutorRegistration.createComboBox.name");
            cb.setName(n);
            return new HeadTeacherConfigurationPanel(cb, TUTANDEN);
        }

    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/application/betula-unit-data/Lookup")
    public static final class TutorModelRegistration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(final Lookup base) {
            final PrimaryUnitOpenSupport puos = base.lookup(PrimaryUnitOpenSupport.class);
            if (puos != null) {
                puos.getRP().post(() -> HeadTeachers.load(puos, TUTANDEN));
            }
            return Lookup.EMPTY;
        }
    }
}
