/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import java.beans.PropertyVetoException;
import java.time.Month;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.curriculum.CourseSelection;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.curriculum.DefaultCourseSelectionValue;
import org.thespheres.betula.curriculumimport.StundentafelImportTargetsItem;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris
 */
public class StundentafelImportTargetsItemImpl extends StundentafelImportTargetsItem {

    StundentafelImportTargetsItemImpl(NamingResolver.Result name, UnitId pu, CourseSelection cs, int base, int seq, Term term, Curriculum cur) {
        super(name, pu, cs, base, seq, term, cur);
    }

    synchronized void initialize(final ConfigurableImportTarget config, final StundentafelImportSettings wizard) {
        final ConfigurableImportTarget oldCfg = getConfiguration();
        boolean configChanged = oldCfg == null
                || !oldCfg.getProviderInfo().equals(config.getProviderInfo());

        if (configChanged || getDeleteDate() == null) {
            if (base != 0) {
                setDeleteDate(ImportUtil.calculateDeleteDate(base, 5, Month.JULY));
            }
        }

        if (configChanged) {
//            importStudents.setConfiguration(config);
//            if (!source.getStudents().isEmpty()) {
//                List<ImportStudentKey> l = source.getStudents().stream()
//                        .map(xmls -> new ImportStudentKey(xmls.getSourceName(), null, null))
//                        .collect(Collectors.toList());
//                try {
//                    importStudents.set(l);
//                    importStudentsException = null;
//                } catch (IOException ex) {
//                    importStudentsException = ex;
//                    Exceptions.printStackTrace(ex);
//                }
//            } else {
//                importStudents.clear();
//                students = null;
//            }
        }

//        final String ts = config.getTermSchemeProvider().getInfo().getURL();
//        if (ts != null) {
//            setPreferredTermScheduleProvider(ts);
//        }
//        configuration = config;
        try {
//            vSupport.fireVetoableChange(PROP_IMPORT_TARGET, oldCfg, configuration);
            setClientProperty(ImportItem.PROP_IMPORT_TARGET, config);
        } catch (PropertyVetoException ex) {
            //TODO: reset config?
            ex.printStackTrace(ImportUtil.getIO().getErr());
        }
    }

    boolean isTaught() {
        if (selection.getCourseSelectionValue() instanceof DefaultCourseSelectionValue) {
            final DefaultCourseSelectionValue dcsv = (DefaultCourseSelectionValue) selection.getCourseSelectionValue();
            return dcsv.getNumLessons() != null && dcsv.getNumLessons() > 0;
        }
        return false;
    }
}
