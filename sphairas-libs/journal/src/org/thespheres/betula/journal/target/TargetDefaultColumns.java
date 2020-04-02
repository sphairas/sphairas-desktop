/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.ui.GradeComboBoxModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@Messages({
    "TargetDefaultColumns.columnName.participant=Name",
    "TargetDefaultColumns.columnName.scoreSum=Gesamt",
    "TargetDefaultColumns.columnName.grade=Note"})
class TargetDefaultColumns {

    private TargetDefaultColumns() {
    }

    static Set<PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant>> create() {
        HashSet<PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant>> ret = new HashSet<>();
        ret.add(new ScoreColumns());
        ret.add(new ParticipantColumn());
        ret.add(new ScoreSumColumn());
        ret.add(new GradeColumn());
        return ret;
    }

    private static String displayName(String colid) {
        final String key = "TargetDefaultColumns.columnName." + colid;
        return NbBundle.getMessage(TargetDefaultColumns.class, key);
    }

    static class ScoreColumns extends PluggableTableColumn.IndexedColumn<EditableJournal<?, ?>, EditableParticipant> {

        private final Highlighter centerHl = new AlignmentHighlighter(SwingConstants.CENTER);
        private final NumberFormat nf = NumberFormat.getIntegerInstance(Locale.getDefault());
        private final StringValue numberStringValue = v -> v == null ? "---" : nf.format(v);

        ScoreColumns() {
            super("scores", 1000, false, 40);
        }

        private Optional<AssessmentConvention> getConvention() {
            return Optional.ofNullable(JournalConfiguration.getInstance().getJournalEntryPreferredConvention())
                    .map(GradeFactory::findConvention);
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<EditableJournal<?, ?>, EditableParticipant, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            final int index = (int) col.getClientProperty(AbstractPluggableTableModel.PROP_COLUMNS_INDEX);
            getConvention()
                    .map(c -> c.getAllGrades()[index])
                    .ifPresent(g -> {
                        col.setToolTipText(g.getLongLabel());
                        col.putClientProperty(TargetAssessmentTableModel.CLIENT_PROP_GRADE, g);
                    });
            col.addHighlighter(centerHl);
            col.setCellRenderer(new DefaultTableRenderer(numberStringValue));
        }

        @Override
        public String getDisplayName(int index) {
            return getConvention()
                    .map(c -> c.getAllGrades()[index])
                    .map(g -> g.getShortLabel())
                    .orElse("");
        }

        @Override
        public int getColumnsSize() {
            return getConvention()
                    .map(c -> c.getAllGrades().length)
                    .orElse(0);
        }

        @Override
        public Object getColumnValue(final EditableParticipant il, final int index) {
            return getConvention()
                    .map(c -> c.getAllGrades()[index])
                    .map(g -> il.getGradeCount(g))
                    .orElse(0l);
        }

    }

    static class ParticipantColumn extends PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant> {

        ParticipantColumn() {
            super("participant", 100, false, 140);
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<EditableJournal<?, ?>, EditableParticipant, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.setCellRenderer(new DefaultTableRenderer(v -> v instanceof EditableParticipant ? ((EditableParticipant) v).getDirectoryName() : null));
        }

        @Override
        public String getDisplayName() {
            return displayName(columnId());
        }

        @Override
        public Object getColumnValue(EditableParticipant ri) {
            return ri;
        }

    }

    static class ScoreSumColumn extends PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant> {

        private final NumberFormat nf;

        ScoreSumColumn() {
            super("scoreSum", 5000, false, 55);
            nf = NumberFormat.getNumberInstance(Locale.getDefault());
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(0);
        }

        @Override
        public String getDisplayName() {
            return displayName(columnId());
        }

        @Override
        public Object getColumnValue(EditableParticipant il) {
            double mean = il.getWeightedGradesMean().getWeightedMean();
            return nf.format(mean);
        }

    }

    static class GradeColumn extends PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant> implements LookupListener {

        private TargetAssessment<Grade, TargetAssessment.Listener<Grade>> target;
        private JournalEditor editor;
        private Lookup.Result<TargetAssessment> taResult;

        GradeColumn() {
            super("grade", 10000, true, 40);
        }

        @Override
        public void initialize(final EditableJournal<?, ?> ej, final Lookup context) {
            editor = context.lookup(JournalEditor.class);
            if (taResult != null) {
                taResult.removeLookupListener(this);
            }
            taResult = editor.getLookup().lookupResult(TargetAssessment.class);
            updateTarget();
            taResult.addLookupListener(this);
            super.initialize(ej, context);
        }

        @Override
        public String getDisplayName() {
            return displayName(columnId());
        }

        @Override
        public Object getColumnValue(EditableParticipant il) {
            if (target != null) {
                return target.select(il.getStudentId());
            }
            return null;
        }

        @Override
        public boolean setColumnValue(EditableParticipant il, Object val) {
            final Grade g = (Grade) val;
            if (target != null) {
                final StudentId s = il.getStudentId();
                final Grade old = target.select(s);
                final TargetAssessmentTableModel.GradeEdit2 edit = new TargetAssessmentTableModel.GradeEdit2(s, old, g, target);
                target.submit(s, g, Timestamp.now());
                editor.getUndoSupport().postEdit(edit);
            }
            return false;
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<EditableJournal<?, ?>, EditableParticipant, ?, ?> model, TableColumnExt col) {
            final GradeComboBoxModel targetBoxModel = new GradeComboBoxModel(new String[0], false);
            final JXComboBox targetComboBox = new JXComboBox(targetBoxModel);
            updateTargetBoxModel(targetBoxModel);
            col.setCellEditor(new DefaultCellEditor(targetComboBox));
            col.setCellRenderer(new DefaultTableRenderer(targetBoxModel));
        }

        private void updateTargetBoxModel(final GradeComboBoxModel targetBoxModel) {
            String pc = null;
            if (target != null) {
                pc = target.getPreferredConvention();
            }
            targetBoxModel.setConventions(new String[]{pc});
        }

        private void updateTarget() {
            if (this.target != null) {
//                this.target.removeListener(this);
            }
            this.target = taResult.allInstances().stream()
                    .map(TargetAssessment.class::cast)
                    .collect(CollectionUtil.singleOrNull());
            if (this.target != null) {
//                this.target.addListener(this);
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            updateTarget();
        }
    }

}
