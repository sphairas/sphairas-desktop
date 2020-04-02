/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import org.thespheres.betula.niedersachsen.admin.ui.ZeugnisAngabenColumn;
import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.swing.JComboBox;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.niedersachsen.admin.ui.Constants;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.ZeugnisAngabenModel.ColFactory;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel.PluggableColumnFactory;
import org.thespheres.betula.ui.util.ExportToCSVOption;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    @ActionReference(id = @ActionID(category = "Betula", id = "org.thespheres.betula.ui.actions.ExportCSVAction"),
            path = "Loaders/application/betula-unit-context/Actions", position = 280000, separatorBefore = 200000)})
//@RemoteLookup.Registration(name = "java:global/Betula_Persistence/ExternalizableUnitZeugnisDataBean!org.thespheres.betula.niedersachsen.zeugnis.ExternalizableUnitZeugnisData$Bean", beanInterface = ExternalizableUnitZeugnisData.Bean.class, module = "zeugnisse")
public class ZeugnisAngabenModel extends AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, PluggableTableColumn<RemoteReportsModel2, ReportData2>, ColFactory> implements ActionListener, ExportToCSVOption, NavigatorLookupPanelsPolicy, NavigatorLookupHint {

    final PrimaryUnitOpenSupport support;
    private final Term[] currentTerm = new Term[]{null};
    private AbstractNode node;
    private final ChangeSupport cSupport = new ChangeSupport(this);
    public final RequestProcessor RP = new RequestProcessor(ZeugnisAngabenModel.class.getName(), 8);

    @SuppressWarnings("LeakingThisInConstructor")
    private ZeugnisAngabenModel(Set<PluggableTableColumn<RemoteReportsModel2, ReportData2>> s, PrimaryUnitOpenSupport uos) {
        super("ZeugnisAngabenModel", s);
        this.support = uos;
        Lookup.getDefault().lookup(WorkingDate.class).addChangeListener(ev -> initialize());
    }

    @Override
    public void initialize(RemoteReportsModel2 model, Lookup context) {
        super.initialize(model, context);
        getItemsModel().getEventBus().register(this);
    }

    static ZeugnisAngabenModel create(PrimaryUnitOpenSupport uos) {
        Set<PluggableTableColumn<RemoteReportsModel2, ReportData2>> s = ZeugnisAngabenColumn.createDefault();
        MimeLookup.getLookup(Constants.UNIT_NDS_ZEUGNIS_SETTINGS_MIME)
                .lookupAll(ZeugnisAngabenColumn.Factory.class).stream()
                .map(ZeugnisAngabenColumn.Factory::createInstance)
                .forEach(s::add);
        final ZeugnisAngabenModel ret = new ZeugnisAngabenModel(s, uos);
        final RemoteReportsModel2 history = uos.getLookup().lookup(RemoteReportsModel2.class);
        ret.initialize(history, uos.getLookup());
        return ret;
    }

    private void initialize() {
        final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
        final LocalDate asOf = wd.isNow() ? null : LocalDate.from(wd.getCurrentWorkingDate().toInstant().atZone(ZoneId.systemDefault()));
        final Term term = getCurrentTerm();
        if (term != null) {
            final TermId tid = term.getScheduledItemId();
            ((RemoteReportsModel2Impl) getItemsModel()).loadTerm(tid, asOf);
        }
        cSupport.fireChange();
    }

    public PrimaryUnitOpenSupport getUnitOpenSupport() {
        return support;
    }

    @Subscribe
    public void onCollectionChange(CollectionChangeEvent event) {
        if (event.getCollectionName().equals(RemoteReportsModel2.COLLECTION_TERMS)) {
            final TermId ct = getCurrentTermId();
            boolean fire = ct == null || event.getItemAs(TermId.class)
                    .map(t -> Objects.equals(ct, t))
                    .orElse(false);
            if (fire) {
                EventQueue.invokeLater(this::fireTableDataChanged);
            }
        }
    }

    @Override
    protected int getItemSize() {
        return getReportsForSelectedTerm().size();
    }

    @Override
    protected ReportData2 getItemAt(int row) {
        return getReportsForSelectedTerm().get(row);
    }

    public Term getCurrentTerm() {
        synchronized (currentTerm) {
            return currentTerm[0];
        }
    }

    public TermId getCurrentTermId() {
        synchronized (currentTerm) {
            return currentTerm[0] == null ? null : currentTerm[0].getScheduledItemId();
        }
    }

    void setCurrentTerm(final Term selected) {
        synchronized (currentTerm) {
            currentTerm[0] = selected;
        }
        Mutex.EVENT.writeAccess(this::fireTableDataChanged);
        RP.post(this::initialize);
    }

    private List<ReportData2> getReportsForSelectedTerm() {
        final Term current = getCurrentTerm();
        if (current != null) {
            return getItemsModel().getStudentsForTerm(current.getScheduledItemId());
        }
        return Collections.EMPTY_LIST;
    }

    public ReportData2 findReport(final StudentId stud) {
        return getReportsForSelectedTerm().stream()
                .filter(sd -> sd.getRemoteStudent().getStudentId().equals(stud))
                .findAny().orElse(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox && ((JComboBox) e.getSource()).getSelectedItem() instanceof Term) {
            final Term selected = (Term) ((JComboBox) e.getSource()).getSelectedItem();
            final Term current = getCurrentTerm();
            if (!Objects.equals(selected, current)) {
                setCurrentTerm(selected);
                Mutex.EVENT.writeAccess(this::fireTableDataChanged);
                RP.post(this::initialize);
            }
        }
    }

    @NbBundle.Messages({"ZeugnisAngabenModel.export.csv.filehint={0} Zeugnisangaben {1}-{2} ({3,date,d.M.yy HH'h'mm}).csv"})
    @Override
    public String createFileNameHint() throws IOException {
        Term term = getCurrentTerm();
        if (term == null) {
            throw new IOException("No current Term selected.");
        }
        NamingResolver nr = support.findNamingResolver();
        NamingResolver.Result rdn;
        try {
            rdn = nr.resolveDisplayNameResult(support.getUnitId());
        } catch (IllegalAuthorityException ex) {
            throw new IOException(ex);
        }
        rdn.addResolverHint("klasse.ohne.schuljahresangabe");
        String name = rdn.getResolvedName(term);
        String jahr = Integer.toString((Integer) term.getParameter(NdsTerms.JAHR));
        int hj = (Integer) term.getParameter(NdsTerms.HALBJAHR);
        return NbBundle.getMessage(ZeugnisAngabenModel.class, "ZeugnisAngabenModel.export.csv.filehint", name.replace("/", "_"), jahr, hj, new Date());
    }

    @Override
    public byte[] getCSV() throws IOException {
        StringBuilder sb = new StringBuilder();
        StringJoiner header = new StringJoiner(";", "", "\n");
        for (int i = 0; i < getColumnCount(); i++) {
            ColumnIndex ci = getColumnsAt(i);
            String h = ci.getColumn().getDisplayName();//TODO IndexColumn
            header.add(h);
        }
        sb.append(header.toString());

//        JXTable t;
//        t.getStringAt(0, 0);
        for (int i = 0; i < getRowCount(); i++) {
            final StringJoiner sj = new StringJoiner(";", "", "\n");
            RemoteStudent rs = getItemAt(i).getRemoteStudent();
            sj.add(rs.getDirectoryName());
            ReportData2 rd = findReport(rs.getStudentId());
            Marker sgl = rd.getRemoteStudent().getClientProperty("sgl", Marker.class);
            sj.add(rd.getFehltage() != null ? Integer.toString(rd.getFehltage()) : "")
                    .add(rd.getUnentschuldigt() != null ? Integer.toString(rd.getUnentschuldigt()) : "")
                    .add(rd.getArbeitsverhalten() != null ? rd.getArbeitsverhalten().getShortLabel() : "")
                    .add(rd.getSozialverhalten() != null ? rd.getSozialverhalten().getShortLabel() : "")
                    .add(rd.getZeugnisTyp() != null ? rd.getZeugnisTyp().getLongLabel() : "")
                    .add(sgl != null ? sgl.getShortLabel() : "")
                    //                    .add(rd.getBDay().format(ZeugnisAngabenColumn.BDayColumn.DTF))
                    .add(rd.getRemoteStudent().getBirthplace());
            sb.append(sj.toString());
        }
        return sb.toString().getBytes("utf-8");
    }

    @Override
    protected ColFactory createColumnFactory() {
        return new ColFactory();
    }

    //Node for BemerkungenComponent, NavigatorPanel
    Node getNodeDelegate() {
        if (node == null) {
            class ND extends AbstractNode {

                @SuppressWarnings({"OverridableMethodCallInConstructor"})
                private ND() {
                    super(Children.LEAF, Lookups.singleton(ZeugnisAngabenModel.this));
                    setName(support.getNodeDelegate().getName());
                    setDisplayName(support.getNodeDelegate().getDisplayName());
                    setIconBaseWithExtension("org/thespheres/betula/admin/units/resources/table.png");
                }

            }
            node = new ND();
        }
        return node;
    }

    @Override
    public String getContentType() {
        return "application/betula-report-data";
    }

    @Override
    public int getPanelsPolicy() {
        return NavigatorLookupPanelsPolicy.LOOKUP_HINTS_ONLY;
    }

    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    public class ColFactory extends PluggableColumnFactory {

    }
}
