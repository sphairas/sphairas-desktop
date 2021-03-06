/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport;

import org.thespheres.betula.xmlimport.uiutil.ImportUnitStudentsCache;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.curriculum.CourseSelection;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.curriculum.StringClientProperty;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.MultiSubject;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 *
 * @author boris.heithecker
 */
public class StundentafelImportTargetsItem extends ImportTargetsItem implements ImportItem.CloneableImport {

//    public static final String PROP_CONFIGURATION = "configuration";
    public static final String PROP_TARGET_ID = "targetId";
    static final Pattern TARGET_ID_PATTERN = Pattern.compile("[\\w]+[-\\w]*", 0); //Centralize
    private boolean selected;
//    private ConfigurableImportTarget configuration;
//    private final ConfigurableImportTargetHelper helper;
//    private final Term term;
    protected final CourseSelection selection;
    private final NamingResolver.Result name;
    protected final int base;
    private final int sequence;
    private String customTargetId;
    private final IDHelper helper;
    private final ID identifier;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    protected StundentafelImportTargetsItem(NamingResolver.Result name, UnitId pu, CourseSelection cs, int base, int seq, Term term, Curriculum cur) {
        super(name.getResolvedName());
        this.name = name;
        this.selection = cs;
        this.base = base;
        this.sequence = seq;
        try {
            setClientProperty(ImportTargetsItem.PROP_SELECTED_TERM, term);
        } catch (PropertyVetoException ex) {
            throw new IllegalStateException(ex);
        }
        setUnitId(pu);
        final MultiSubject su = selection.getCourse().getSubject();
        if (su.isSingleSubject()) {
            setSubjectMarker(su.getSingleSubject());
        } else {
            throw new UnsupportedOperationException();
        }
        this.identifier = new ID(this);
        this.preferredConvention = cs.getClientProperty("preferred-convention", StringClientProperty.class)
                .map(StringClientProperty::getValue)
                .orElse(cur.getGeneral().getPreferredAssessmenConvention());
        this.helper = new IDHelper(this);
    }

    @Override
    public int id() {
        return 0;
    }

    public ID getIdentifier() {
        return identifier;
    }

    public CourseSelection getSelection() {
        return selection;
    }

    public Term getTerm() {
        return (Term) getClientProperty(ImportTargetsItem.PROP_SELECTED_TERM);
    }

    public ConfigurableImportTarget getConfiguration() {
        return (ConfigurableImportTarget) getClientProperty(ImportItem.PROP_IMPORT_TARGET);
    }

    @Override
    public synchronized StudentId[] getUnitStudents() {
        final ConfigurableImportTarget cfg = getConfiguration();
        final UnitId uid = getUnitId();
        if (cfg != null && uid != null) {
            try {
                return ImportUnitStudentsCache.get(cfg.getProvider(), uid);
            } catch (final IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(StundentafelImportTargetsItem.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }
        return super.getUnitStudents();
    }

    @Override
    public boolean isUnitIdGenerated() {
        return false;
    }

    @Override
    public boolean existsUnitInSystem() {
        return getUnitStudents() != null;
    }

    public String getCustomTargetId() {
        return customTargetId;
    }

    public void setCustomTargetId(String customTargetId) {
        String old = getCustomTargetId();
        final String trim = StringUtils.trimToNull(customTargetId);
        if (!Objects.equals(old, trim) && TARGET_ID_PATTERN.matcher(trim).matches()) {
            this.customTargetId = trim;
            try {
                this.vSupport.fireVetoableChange(PROP_TARGET_ID, old, this.customTargetId);
            } catch (PropertyVetoException ex) {
                this.customTargetId = old;
            }
        }
    }

    @Override
    public DocumentId getTargetDocumentIdBase() {
        return helper.getTargetDocumentIdBase();
    }

    @Override
    public void setTargetDocumentIdBase(DocumentId did) {
        helper.setTargetDocumentBase(did);
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        if (getConfiguration() != null && isValid()) {
            return getConfiguration().createTargetDocuments(this);
        }
        return new TargetDocumentProperties[0];
    }

    @Override
    public boolean fileUnitParticipants() {
        return false;
    }

    @Override
    public String getUnitDisplayName() {
        return name.getResolvedName();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean isValid() {
        return getDeleteDate() != null
                && getUnitId() != null
                && getTargetDocumentIdBase() != null
                && getPreferredConvention() != null
                && !Marker.isNull(getSubjectMarker());
//                && importStudents.isValid();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.getSubjectMarker());
        return 83 * hash + Objects.hashCode(this.getUnitId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StundentafelImportTargetsItem other = (StundentafelImportTargetsItem) obj;
        if (!Objects.equals(this.getSubjectMarker(), other.getSourceSubject())) {
            return false;
        }
        return Objects.equals(this.getUnitId(), other.getUnitId());
    }

    public static class Filter implements UpdaterFilter<StundentafelImportTargetsItem, TargetDocumentProperties> {

        @Override
        public boolean accept(final StundentafelImportTargetsItem iti) {
            return iti.isSelected() && iti.isValid();
        }

        @Override
        public boolean accept(final StundentafelImportTargetsItem iti, TargetDocumentProperties td, StudentId stud) {
            final List<UpdaterFilter> f = iti.getFilters();
            return td.getPreferredConvention() != null
                    && (f.isEmpty() ? true : f.stream().allMatch(sbf -> sbf.accept(iti, td, stud)));
        }
    }

    public static class ID {

        private final String course;
        private final UnitId unit;

        public ID(final String course, final UnitId unit) {
            super();
            this.course = course;
            this.unit = unit;
        }

        ID(final StundentafelImportTargetsItem item) {
            super();
            this.course = item.getSelection().getCourse().getId();
            this.unit = item.getUnitId();
        }

        public String getCourse() {
            return course;
        }

        public UnitId getUnit() {
            return unit;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + Objects.hashCode(this.course);
            return 53 * hash + Objects.hashCode(this.unit);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ID other = (ID) obj;
            if (!Objects.equals(this.course, other.course)) {
                return false;
            }
            return Objects.equals(this.unit, other.unit);
        }
    }
}
