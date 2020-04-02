/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.ZeugnisAngabenModel;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class ReportContextListener implements LookupListener, PropertyChangeListener {

    private static ReportContextListener INSTANCE;
    private final Lookup.Result<RemoteStudent> lkpRes;
    private ZeugnisAngabenModel currentZeungisSettingsModel;
    private ReportData2 currentReportData;
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private Object[] defArgs;

    @SuppressWarnings("LeakingThisInConstructor")
    private ReportContextListener() {
        lkpRes = Utilities.actionsGlobalContext().lookupResult(RemoteStudent.class);
        onChange();
        lkpRes.addLookupListener(this);
        TopComponent.getRegistry().addPropertyChangeListener(this);
    }

    public static synchronized ReportContextListener getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new ReportContextListener();
        }
        return INSTANCE;
    }

    public ZeugnisAngabenModel getCurrentZeungisSettingsModel() {
        return currentZeungisSettingsModel;
    }

    public ReportData2 getCurrentReportData() {
        return currentReportData;
    }

    public Object[] getCurrentFormatArgs() {
        return currentReportData != null ? currentReportData.getFormatArgs() : getDefaultFormatArgs();
    }

    public Object[] getDefaultFormatArgs() {
        if (defArgs == null) {
//            String nSJ = "?";
//            String nStufe = "?";
//            try {
//                t = Terms.fromId(this.term);
//                int jahr = (int) t.getParameter(Terms.JAHR);
//                nSJ = Integer.toString(jahr) + "/" + Integer.toString(++jahr).substring(2);
//                NamingResolver.Result r = history.support.findNamingResolverResult();
//                r.addResolverHint("naming.only.level");
//                nStufe = r.getResolvedName(Terms.getTerm(jahr, 1));
//            } catch (IllegalAuthorityException | NumberFormatException | IOException ex) {
//            }
//            Date zkDate = null;
//            try {
//                zkDate = calendar.findZeugnisDate(history.support.getUnitId(), term);
//            } catch (IllegalStateException e) {
//            }
//            if (zkDate == null) {
//                Logger.getLogger(getClass().getCanonicalName()).log(Level.INFO, "No zeugniskonferenz date set for unit {0} in term {1}", new Object[]{history.support.getUnitId(), this.term});
//                zkDate = new Date();
//            }
            defArgs = new Object[]{"Max", "Max'", "seine", new Date(), "8", "2017/18", 0l};
        }
        return defArgs;
    }

    private synchronized void onChange() {
        boolean changed = Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
                .flatMap(n -> n.getLookup().lookupAll(ZeugnisAngabenModel.class).stream())
                .findAny()
                .map(this::setCurrentZeugnisSettingModel)
                .orElse(Boolean.FALSE);

        changed = changed | setStudent();
        if (changed) {
            cSupport.fireChange();
        }
    }

    private boolean setCurrentZeugnisSettingModel(ZeugnisAngabenModel m) {
        if (m != null) {
            UnitId uid = m.getUnitOpenSupport().getUnitId();
            if (!Objects.equals(uid, getCurrentUnitId())) {
                currentZeungisSettingsModel = m;
                return true;
            }
        }
        return false;
    }

    private UnitId getCurrentUnitId() {
        return currentZeungisSettingsModel != null ? currentZeungisSettingsModel.getUnitOpenSupport().getUnitId() : null;
    }

    private boolean setStudent() {
        final RemoteStudent rs = lkpRes.allInstances().stream().collect(CollectionUtil.singleOrNull());
        if (currentZeungisSettingsModel != null && rs != null) { //&& model.getCurrentStudent().map(s -> !s.getRemoteStudent().equals(rs)).orElse(true)
            final ReportData2 rd = currentZeungisSettingsModel.findReport(rs.getStudentId());
            if (currentReportData == null || !currentReportData.getRemoteStudent().getStudentId().equals(rs.getStudentId())) {
                currentReportData = rd;
                return true;
            }
        }
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case TopComponent.Registry.PROP_ACTIVATED_NODES:
                onChange();
                break;
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        setStudent();
    }

    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

}
