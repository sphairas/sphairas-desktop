/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2;

import org.thespheres.betula.sibank.SiBankImportStudentItem;
import org.thespheres.betula.sibank.*;
import java.util.Set;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.sibank.ui2.impl.SiBankKlasseDefaultColumns;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;

/**
 *
 * @author boris.heithecker
 */
public class SiBankUpdateStudentsTableModel extends ImportTableModel<SiBankImportStudentItem, SiBankImportData<SiBankImportStudentItem>> implements Runnable {

//    public static final String[] STUDENTS_MARKER_PATH = new String[]{"students", "marker"};
    protected SiBankImportTarget configuration;

    public SiBankUpdateStudentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        final String product = NbBundle.getMessage(SiBankKlasseDefaultColumns.class, "SiBankKlasseDefaultColumns.product");
        final Set<ImportTableColumn> s = SiBankKlasseDefaultColumns.create(product);
        Lookups.forPath("SiBankUpdateStudentsVisualPanel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return s;
    }

    @Override
    public void initialize(SiBankImportData<SiBankImportStudentItem> descriptor) {
        configuration = (SiBankImportTarget) descriptor.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final ChangeSet<SiBankKlasseItem> nodes = (ChangeSet<SiBankKlasseItem>) descriptor.getProperty(AbstractFileImportAction.SELECTED_NODES);

        selected.clear();
        nodes.stream()
                .flatMap(ik -> ik.getStudents().values().stream())
                .distinct()
                .sorted()
                .forEach(selected::add);

        Mutex.EVENT.writeAccess(this::fireTableDataChanged);

        configuration.getWebServiceProvider().getDefaultRequestProcessor().post(this);
//        findRemoteLookup().getRequestProcessor().post(this);
    }

    @Override
    public void run() {
        try {
//            RemoteLookup rlp = findRemoteLookup();
//            initVCardValues(rlp);
            Mutex.EVENT.writeAccess(this::fireTableDataChanged);
//            setValid(true);
        } catch (Exception ex) {
            ImportUtil.getIO().getErr().println(ex);
        }
    }

//    private RemoteLookup findRemoteLookup() {
//        return configuration.getRemoteLookup();
//    }

//    protected void initVCardValues(RemoteLookup rlp) {
////        final StudentBean sb = rlp.lookup(StudentBean.class);
////        final DocumentId careers = configuration.getStudentCareersDocumentId();
////        final UnitDocumentBean udb = rlp.lookup(UnitDocumentBean.class);
//        selected.stream().forEach(i -> {
////            if (sb != null && careers != null) {
////                try {
////                    final Marker sgl = sb.getMarkerEntry(i.getStudentId(), careers, null);
//////                i.sgl = sgl;
////                    if (sgl != null) {
////                        i.getUniqueMarkerSet().add(sgl);
////                    }
////                } catch (RuntimeException e) {
////                    ImportUtil.getIO().getErr().println(e);
////                }
////            }
////            if (udb != null) {
////                try {
////                    UnitId[] pu = udb.getUnits(i.getStudentId(), true);
////                    i.setPrimaryUnits(pu);
////                } catch (RuntimeException e) {
////                    ImportUtil.getIO().getErr().println(e);
////                }
////            }
//        });
//    }
}
