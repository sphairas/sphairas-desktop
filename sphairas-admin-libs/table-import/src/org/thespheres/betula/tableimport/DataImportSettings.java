package org.thespheres.betula.tableimport;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.model.XmlImport;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.DefaultImportWizardSettings;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
@Messages({"TableImport.produce=Tabelle"})
public abstract class DataImportSettings<I extends ImportItem> extends DefaultImportWizardSettings<ConfigurableImportTarget, I> {

    public static enum Type {
        STUDENTS, TARGETS, STUDENTS_TO_TARGETS, SIGNEES;
    }
    private final Type type;

    protected DataImportSettings(final Type type) {
        this.type = type;
    }

//    public static DataImportSettings<?> create(XmlImport xml) {
//        Type type = null;
//        final List<XmlItem> i = xml.getItems();
//        if (!i.isEmpty()) {
//            if (i.stream().allMatch(XmlStudentItem.class::isInstance)) {
//                type = Type.STUDENTS;
//            } else if (i.stream().allMatch(XmlTargetItem.class::isInstance)) {
////                return new TargetsImportSettings(Type.TARGETS);
//                return null;
//            } else if (i.stream().allMatch(XmlTargetEntryItem.class::isInstance)) {
////                return new TargetsDataImportSettings(Type.STUDENTS_TO_TARGETS);
//                return null;
//            } else if (i.stream().allMatch(XmlSigneeItem.class::isInstance)) {
//                type = Type.SIGNEES;
//            }
//        }
//        return null;
//    }

    public Type getType() {
        return type;
    }

    @Override
    public void initialize(WizardDescriptor panel) throws IOException {
        super.initialize(panel);
        load();
    }

    protected abstract void load();

    public XmlImport getImportData() {
        return (XmlImport) getProperty(AbstractFileImportAction.DATA);
    }

//    private static class TargetsDataImportSettings extends DataImportSettings<XmlImportTargetsItem> {
//
//        private TargetsDataImportSettings(Type type) {
//            super(type);
//        }
//
//        @Override
//        protected void load() {
//            final ChangeSet<XmlImportTargetsItem> set = (ChangeSet<XmlImportTargetsItem>) getSelectedNodesProperty();
//            getImportData().getItems().stream()
//                    .filter(XmlTargetEntryItem.class::isInstance)
//                    .map(XmlTargetEntryItem.class::cast)
//                    .collect(Collectors.groupingBy(XmlTargetEntryItem::getTarget, Collectors.toList()))
//                    .forEach((did, l) -> {
//                        String n = did.getId() + " (" + Integer.toString(l.size()) + ")";
////                        XmlImportTargetsItem target = new XmlImportTargetsItem(n, did, false);
////                        set.add(target);
////                        l.stream()
////                                .forEach(item -> target.submit(item.getStudent(), item.getTerm(), item.getGrade(), new Timestamp(item.getTimestamp().getParsed())));
//                    });
//        }
//    }
}
