package org.thespheres.betula.tableimport.action;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Component;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.IntStream;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.tableimport.DataImportSettings;
import org.thespheres.betula.tableimport.action.MultiFileFilterDelegate.MimeFileFilterExt;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdaterDescriptions;

@Messages({"XmlCsvDataImportAction.dialog.title=Tabellen-Import"})
public abstract class XmlCsvImportAction<I extends ImportItem> extends AbstractFileImportAction<XmlCsvImportSettings<I>, XmlCsvFile[], ConfigurableImportTarget, I> {

    protected XmlCsvImportAction() {
        super(NbBundle.getMessage(XmlCsvImportAction.class, "XmlCsvDataImportAction.dialog.title"));
    }

    @Override
    protected WizardDescriptor.Iterator<XmlCsvImportSettings<I>> createIterator(XmlCsvFile[] xml, XmlCsvImportSettings<I> settings) {
        return settings.createIterator(null);
    }

    @Override
    protected void onConfigurationSelectionChange(ConfigurableImportTarget newConfig, XmlCsvImportSettings data, WizardDescriptor wiz) {
        if (newConfig != null) {
            final String title = dialogTitle + " - " + newConfig.getProviderInfo().getDisplayName();
            wiz.setTitle(title);
        }
        try {
            data.initialize(newConfig);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected DataImportSettings.Type[] getTypes() {
        return null;
    }

    @Messages({"XmlCsvDataImportAction.FileChooser.Title=Importiere Tabelle",
        "XmlCsvDataImportAction.FileChooser.utf8Box.title=UTF-8"})
    @Override
    protected XmlCsvFile[] openFile() throws IOException {
        final File home = new File(System.getProperty("user.home"));
        final String title = NbBundle.getMessage(XmlCsvImportAction.class, "XmlCsvDataImportAction.FileChooser.Title");
        final MultiFileFilterDelegate filter = MultiFileFilterDelegate.create(getTypes(), true);
        final JCheckBox encBox = new JCheckBox();
        encBox.setHorizontalTextPosition(SwingConstants.LEADING);
        encBox.setEnabled(false);
        final String encBoxTitle = NbBundle.getMessage(XmlCsvImportAction.class, "XmlCsvDataImportAction.FileChooser.utf8Box.title");
        encBox.setText(encBoxTitle);
        final String system = Charset.defaultCharset().name();
        final boolean isSystemCharsetUTF8 = StandardCharsets.UTF_8.name().equals(system);
        encBox.setSelected(isSystemCharsetUTF8);
        final JPanel encBoxLabel = new JPanel();
        final BoxLayout bl = new BoxLayout(encBoxLabel, BoxLayout.LINE_AXIS);
        encBoxLabel.setLayout(bl);
        encBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
        encBoxLabel.add(encBox);
        final FileChooserBuilder fcb = new FileChooserBuilder(XmlCsvImportAction.class) {
            @Override
            public JFileChooser createFileChooser() {
                final JFileChooser ret = super.createFileChooser();
                filter.allFilters().forEach(ret::addChoosableFileFilter);

//                Logger log = PlatformUtil.getCodeNameBaseLogger(XmlCsvImportAction.class);
//                log.info("LayoutManager " + ret.getLayout().getClass().getCanonicalName());
//
////                BorderLayout l = (java.awt.BorderLayout) ret.getLayout();
//                log.info("Components ");
//                for (int i = 0; i < ret.getComponentCount(); i++) {
//                    Component c = ret.getComponent(i);
//                    if (c instanceof JComponent) {
//                        log.info("Component i " + i + " name " + c.getName() + " class  " + c.getClass().getCanonicalName());
//                        for (int j = 0; j < ((JComponent) c).getComponentCount(); j++) {
//
//                            Component cc = ((JComponent) c).getComponent(j);
//                            log.info("Component j " + j + " name " + cc.getName() + " class  " + cc.getClass().getCanonicalName());
//                        }
//                    }
//                }
                boolean boxAdded = false;
                final List<JComboBox> cbx = new ArrayList<>();
                findComboBoxes(ret, cbx);
                JComboBox fileFilterBox = null;
                for (final JComboBox jcb : cbx) {
                    final ComboBoxModel cbm = jcb.getModel();
                    if (IntStream.range(0, cbm.getSize() - 1).mapToObj(cbm::getElementAt).anyMatch(FileFilter.class::isInstance)) {
                        fileFilterBox = jcb;
                    }
                }

                if (fileFilterBox != null) {
                    final JComponent p = (JComponent) fileFilterBox.getParent();
                    for (int c = 0; c < p.getComponentCount(); c++) {
                        if (p.getComponent(c) == fileFilterBox) {
                            p.add(Box.createHorizontalStrut(10), ++c);
                            p.add(encBoxLabel, ++c);
                            boxAdded = true;
                            break;
                        }
                    }
                }

                ret.addPropertyChangeListener(e -> {
                    if (JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(e.getPropertyName())) {
                        final FileFilter filter = ret.getFileFilter();
                        final boolean ena;
                        if (filter instanceof MimeFileFilterExt) {
                            MimeFileFilterExt mffx = (MimeFileFilterExt) filter;
                            ena = mffx.converter.isRequireCharsetParameter();
                        } else {
                            ena = false;
                        }
                        encBox.setEnabled(ena);
                    }
                });

                if (!boxAdded) {
                    throw new IllegalStateException("Could not add JCheckBox.");
                }

//                if(!cbx.isEmpty())
//                try {
//                    JPanel panel1 = (JPanel) ret.getComponent(3);
//                    JPanel panel2 = (JPanel) panel1.getComponent(3);
//
//                    Component c1 = panel2.getComponent(0);//optional used to add the buttons after combobox
//                    Component c2 = panel2.getComponent(1);//optional used to add the buttons after combobox
//                    panel2.removeAll();
//
//                    panel2.add(encBox);
//                    panel2.add(c1);//optional used to add the buttons after combobox
//                    panel2.add(c2);//optional used to add the buttons after combobox
//                } catch (Exception e) {
//                    Exceptions.printStackTrace(e);
//                }
//                
//                try {
//                    final JComponent c1 = (JComponent) ret.getComponent(ret.getComponentCount() - 1);
//                    final JComponent c2 = (JComponent) c1.getComponent(c1.getComponentCount() - 1);
//                    final JComponent c3 = (JComponent) c2.getComponent(c2.getComponentCount() - 1);
//                    final JComponent panel;
//                    if (c3 instanceof JPanel) {
//                        panel = c3;
//                    } else {
//                        panel = c2;
//                    }
//                    final Component[] components = panel.getComponents();
//                    panel.removeAll();
////                    panel.add(encBox);
//                    Arrays.stream(components)
//                            .forEach(panel::add);
//                } catch (Exception e) {
//                    Exceptions.printStackTrace(e);
//                }
                return ret;
            }

            private void findComboBoxes(final JComponent ret, final List<JComboBox> cbx) {
                for (Component cp : ret.getComponents()) {
                    if (cp instanceof JComponent) {
                        final JComponent jcp = (JComponent) cp;
                        if (jcp instanceof JComboBox) {
                            cbx.add((JComboBox) jcp);
                        }
                        findComboBoxes(jcp, cbx);
                    }
                }
            }
        };
        fcb.setTitle(title)
                .setDefaultWorkingDirectory(home)
                .setFileHiding(true);
        final File file = fcb.showOpenDialog();
        if (file == null || !file.exists()) {
            return null;
        }
        final String mimeType = filter.getMimeType(file);
        final String encoding = (encBox.isEnabled() && encBox.isSelected()) ? StandardCharsets.UTF_8.name() : Charset.defaultCharset().name();
        return filter.load(file, encoding);
    }

    @Override
    protected abstract AbstractUpdater<I> createUpdater(Set<?> selected, ConfigurableImportTarget config, Term term, XmlCsvImportSettings<I> wiz);

    protected TargetItemsUpdaterDescriptions createTargetItemsUpdaterDescriptions(final ConfigurableImportTarget config, final XmlCsvImportSettings<?> wiz) {
        final VCardStudents students;
        try {
            students = VCardStudents.get(LocalProperties.find(config.getProvider()));
        } catch (IOException ioex) {
            return null;
        }
        final Signees signees;
        try {
            signees = Signees.get(config.getProvider()).get();
        } catch (NoSuchElementException e) {
            return null;
        }
        final Term term = (Term) wiz.getProperty(AbstractFileImportAction.TERM);
        final NamingResolver nr = config.getNamingResolver();
        final SchemeProvider tsp = config.getTermSchemeProvider();
        return new TargetItemsUpdaterDescriptions(students, term, nr, tsp, signees);
    }

    public static interface ImportTargetsItemMapper {

        default public int position() {
            return Integer.MAX_VALUE;
        }

        public ImportTargetsItem[] map(final ConfigurableImportTarget config, final ImportTargetsItem item, final Term term);
    }

    @ServiceProvider(service = ImportTargetsItemMapper.class)
    public static class DefaultImportTargetsItemMapper implements ImportTargetsItemMapper {

        @Override
        public ImportTargetsItem[] map(final ConfigurableImportTarget config, final ImportTargetsItem item, final Term term) {
            return new ImportTargetsItem[]{item};
        }
    }
}
