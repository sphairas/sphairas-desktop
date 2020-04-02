/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.gs.ui.imports;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.niedersachsen.gs.CrossmarkSettings;
import org.thespheres.betula.niedersachsen.gs.ui.CrossMarksUtil;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.tableimport.action.XmlCsvImportAction.ImportTargetsItemMapper;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"CrossMarksImportTargetsItemMapper.mapItem.findLevelException=Für die Klasse/Gruppe {0} konnte keine Stufe bestimmt werden.",
    "CrossMarksImportTargetsItemMapper.mapItem.noUnitException=Keine Klasse/Gruppe angegeben."})
@ServiceProvider(service = ImportTargetsItemMapper.class)
public class CrossMarksImportTargetsItemMapper implements ImportTargetsItemMapper {

    private Optional<CrossmarkSettings> settings;

    @Override
    public int position() {
        return 1000;
    }

    private Optional<CrossmarkSettings> getCrossmarkSettings(final String provider) {
        if (settings == null) {
            try {
                settings = Optional.ofNullable(CrossMarksUtil.load(provider));
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return settings;
    }

    @Override
    public ImportTargetsItem[] map(final ConfigurableImportTarget config, final ImportTargetsItem item, final Term term) {
        if (Objects.equals(Boolean.TRUE, item.getClientProperty(ImportCrossMarksColumn.KEY)) && getCrossmarkSettings(config.getProvider()).isPresent()) {
            return mapItem(config, getCrossmarkSettings(config.getProvider()).get(), term, item);
        }
        return null;
    }

    protected CrossMarksImportTargetsItem[] mapItem(final ConfigurableImportTarget config, final CrossmarkSettings cs, final Term term, final ImportTargetsItem s) {
        final AssessmentConvention ac = cs.getAssessmentConvention();//GradeFactory.findConvention("ndschoice1");
        final String provider = config.getProvider();
        final UnitId unit = s.getUnitId();
        if (unit == null) {
            final String msg = NbBundle.getMessage(CrossMarksImportTargetsItemMapper.class, "CrossMarksImportTargetsItemMapper.mapItem.noUnitException");
            PlatformUtil.getCodeNameBaseLogger(CrossMarksImportTargetsItemMapper.class).log(Level.INFO, "No unit specified.");
            ImportUtil.getIO().getErr().println(msg);
            return null;
        }
        final Signee signee = s.getSignee();
        final int level;
        try {
            final NamingResolver.Result r = config.getNamingResolver().resolveDisplayNameResult(unit);
            r.addResolverHint("naming.only.level");
            r.addResolverHint("klasse.ohne.schuljahresangabe");
            final String stufe = r.getResolvedName(term);
            level = Integer.parseInt(stufe);
        } catch (IllegalAuthorityException | NumberFormatException ex) {
            final String msg = NbBundle.getMessage(CrossMarksImportTargetsItemMapper.class, "CrossMarksImportTargetsItemMapper.mapItem.findLevelException", unit);
            PlatformUtil.getCodeNameBaseLogger(CrossMarksImportTargetsItemMapper.class).log(Level.INFO, "Could not determine level for unit {0}", unit);
            ImportUtil.getIO().getErr().println(msg);
            return null;
        }
        final MarkerConvention mcd = cs.findConvention(level, s.getSubjectMarker());  //mapSubject(s.getSubjectMarker(), level);
        if (mcd == null) {
            return null;
        }
        return CrossMarksImportTargetsItem.createForUnit(unit, signee, level, mcd, ac.getName(), provider);
    }
}
