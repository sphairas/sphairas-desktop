/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui;

import org.thespheres.betula.gpuntis.ui.impl.UntisDefaultColumns;
import org.thespheres.betula.gpuntis.ImportedLesson;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.gpuntis.ImportUntisUtil;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.gpuntis.xml.Document;
import org.thespheres.betula.gpuntis.xml.Lesson;
import org.thespheres.betula.gpuntis.xml.Teacher;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractSelectNodesOutlineModel;

/**
 *
 * @author boris.heithecker
 */
@Messages({"KursauswahlOutlineModel.columnName.dates=Zeitraum",
    "KursauswahlOutlineModel.columnName.timetable=Studenplan",
    "KursauswahlOutlineModel.columnName.misc=Weitere Angaben"})
public class KursauswahlOutlineModel extends AbstractSelectNodesOutlineModel<ImportedLesson> {

    public static final String[] columns = {"dates", "timetable", "misc"};
    private Document daten;
    private final Collator collator = Collator.getInstance(Locale.GERMANY);
    protected UntisImportData wizard;
//    private SourceUserOverrides userOverrides;

    KursauswahlOutlineModel() {
        super(NbBundle.getMessage(UntisDefaultColumns.class, "UntisDefaultColumns.product"), columns);
    }

    @Override
    protected String getColumnDisplayName(String id) {
        try {
            return NbBundle.getMessage(KursauswahlOutlineModel.class, "KursauswahlOutlineModel.columnName." + id, product);
        } catch (MissingResourceException mrex) {
            Logger.getLogger(KursauswahlOutlineModel.class.getName()).log(Level.WARNING, mrex.getMessage(), mrex);
            return id;
        }
    }

    public void initialize(UntisImportData wiz) {
        this.wizard = wiz;
        this.daten = (Document) wizard.getProperty(AbstractFileImportAction.DATA);
        this.selected = (Set<ImportedLesson>) wizard.getProperty(AbstractFileImportAction.SELECTED_NODES);
        this.clones = (Map<ImportedLesson, Set<ImportedLesson>>) wizard.getProperty(AbstractFileImportAction.CLONED_NODES);
//                this.userOverrides = new SourceUserOverrides(wiz);
//        wiz.putProperty(ImportAction.USER_SOURCE_OVERRIDES, this.userOverrides);
//        UntisImportConfiguration config = (UntisImportConfiguration) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        Map<Teacher, List<Lesson>> teacherMap = new HashMap<>();
        daten.getLessons().stream()
                .forEach((l) -> {
                    Teacher t = l.getLessonTeacher();
                    if (t != null) {
                        teacherMap.computeIfAbsent(t, te -> new ArrayList<>())
                        .add(l);
                    }
                });

        teacherMap.entrySet().stream()
                .sorted(Comparator.comparing(e -> ImportUntisUtil.dirName(e.getKey()), collator))
                .forEach(e -> {
                    DefaultMutableTreeNode tn = new DefaultMutableTreeNode(e.getKey());
                    root.add(tn);
                    e.getValue().stream()
                    .map(l -> ImportedLesson.create(l, daten.getGeneral()))
                    .map(DefaultMutableTreeNode::new)
                    .forEach(tn::add);
                });
        treeModel.reload();
    }

    @Override
    protected ImportedLesson extractNode(Object o) {
        Object uo = ((DefaultMutableTreeNode) o).getUserObject();
        return uo instanceof ImportedLesson ? (ImportedLesson) uo : null;
    }

    @Override
    public Object getValueFor(Object o, String id) {
        ImportedLesson l = extractNode(o);
        if (l != null) {
            return l.getColumn(id);
        }
        return "";
    }

    @Override
    public String getDisplayName(Object o) {
        Teacher t = extractTeacher(o);
        if (t != null) {
            return ImportUntisUtil.dirName(t);
        } else {
            return super.getDisplayName(o);
        }
    }

    private static Teacher extractTeacher(Object o) {
        Object uo = ((DefaultMutableTreeNode) o).getUserObject();
        return uo instanceof Teacher ? (Teacher) uo : null;
    }

    @Override
    public String getTooltipText(Object o) {
        ImportedLesson l = extractNode(o);
        if (l != null) {
            return l.getTooltip();
        }
        return "";
    }

    @Override
    protected void removeSelection(ImportedLesson l) {
        super.removeSelection(l);
//        if (userOverrides != null) {
//            userOverrides.removeWatch(l);
//        }
    }

    @Override
    protected void addSelection(ImportedLesson l) {
        super.addSelection(l);
//        if (userOverrides != null) {
//            userOverrides.addWatch(l);
//        }
    }

}
