/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOFolding;
import org.openide.windows.InputOutput;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ui.util.Targets;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;

/**
 *
 * @author boris.heithecker
 * @param <Data>
 * @param <T>
 * @param <C>
 * @param <I>
 */
public abstract class AbstractFileImportAction<Data extends AbstractImportWizardSettings, T, C extends ImportTarget, I extends ImportItem> implements ActionListener {

    public static final String PROP_DRY_RUN = "dryRun";
    public static final String SELECTED_NODES = "selected";
    public static final String CLONED_NODES = "cloned";
    public static final String DATA = "data";
    public static final String IMPORT_TARGET = "import-target";
    public static final String SAVED_IMPORT_TARGET_PROVIDER = "saved.import-target.provider";
    public static final String SOURCE_TARGET_LINKS = "source-target-links";
    public static final String TERM = "current-term";
    protected WizardDescriptor.Iterator<Data> iterator;
    protected final String dialogTitle;

    protected AbstractFileImportAction(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    protected abstract Product getProduct();

    protected Class<C> getImportTargetImpl() {
        Class clz = getClass();
        while (!(clz = clz.getSuperclass()).equals(AbstractFileImportAction.class)) {
        }
        return (Class<C>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];
    }

    protected Class<I> getImportTargetsItemImpl() {
        Class clz = getClass();
        while (!(clz = clz.getSuperclass()).equals(AbstractFileImportAction.class)) {
        }
//          return (Class<I>) ((ParameterizedType) (Type) clz).getActualTypeArguments()[0];
        return (Class<I>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[3];
    }

    protected void initialize() {
        ImportUnitStudentsCache.resetAll();
    }

    protected void uninitialize() {
        iterator = null;
    }

    protected abstract T openFile() throws IOException;

    protected void onConfigurationSelectionChange(C newConfig, Data wiz, WizardDescriptor wd) {
    }

    public static FoldHandle messageActionStart(String title) throws IOException {
        InputOutput io = ImportUtil.getIO();
        io.select();
        io.getOut().reset();
        io.getOut().println();
        IOColorLines.println(io, title, Color.BLUE);
        FoldHandle ret = IOFolding.startFold(io, true);
        io.getOut().println();
        return ret;
    }

    protected abstract WizardDescriptor.Iterator<Data> createIterator(T xml, Data settings);

    protected abstract Data createSettings(T xml);

    @Override
    public void actionPerformed(ActionEvent e) {
        initialize();
        FoldHandle fold;
        try {
            fold = messageActionStart(dialogTitle);
        } catch (IOException ex) {
            Logger.getLogger(AbstractFileImportAction.class.getName()).severe(ex.getLocalizedMessage());
            return;
        }
        T xml;
        try {
            xml = openFile();
        } catch (IOException ex) {
            Logger.getLogger(AbstractFileImportAction.class.getCanonicalName()).warning(ex.getLocalizedMessage());
            return;
        }
        if (xml == null) {
            return;
        }
        final Data d = createSettings(xml);
        if (d == null) {
            return;
        }
        if ((iterator = createIterator(xml, d)) == null) {
            return;
        }
        final WizardDescriptor wd = new WizardDescriptor(iterator, d);
        d.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        d.putProperty(AbstractFileImportAction.DATA, xml);
        d.putProperty(AbstractFileImportAction.SELECTED_NODES, new ChangeSet<>(new HashSet<>()));
        d.putProperty(AbstractFileImportAction.CLONED_NODES, new HashMap<>());
        try {
            d.initialize(wd);
        } catch (IOException ex) {
            ImportUtil.getIO().getErr().println(ex);
            return;
        }
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wd.setTitleFormat(new MessageFormat("{0} ({1})"));
        wd.setTitle(dialogTitle);
        class TitleUpdater implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(AbstractFileImportAction.IMPORT_TARGET)) {
                    try {
                        final C cfg = (C) d.getProperty(AbstractFileImportAction.IMPORT_TARGET);
                        onConfigurationSelectionChange(cfg, d, wd);
                    } catch (ClassCastException cce) {
                        Logger.getLogger(AbstractFileImportAction.class.getCanonicalName()).warning(cce.getLocalizedMessage());
                    }
                }
            }
        }
        d.addPropertyChangeListener(new TitleUpdater());
        setLastImportTargetSelection(d);
        beforeWizardShow(d);
        C config = null;
        if (DialogDisplayer.getDefault().notify(wd) == WizardDescriptor.FINISH_OPTION) {
            config = (C) d.getProperty(AbstractFileImportAction.IMPORT_TARGET);
            Set<?> selected = (Set<?>) d.getProperty(AbstractFileImportAction.SELECTED_NODES);
            onWizardFinishOK(config, selected, xml, d);
        }
        final String url = config != null ? config.getProviderInfo().getURL() : null;
        final Runnable after = () -> {
            uninitialize();
            fold.silentFinish();
            if (url != null) {
                Units.get(url).ifPresent(Units::forceReload);
                Signees.get(url).ifPresent(Signees::forceReload);
                try {
                    Targets.get(url).forceReload();
                } catch (IOException ex) {
                }
            }
        };
        if (config != null) {
            config.getWebServiceProvider().getDefaultRequestProcessor().post(after);
        } else {
            after.run();
        }
    }

    protected void setLastImportTargetSelection(Data wiz) {
        final String configLast = findLastImportTargetUrl();
        if (configLast != null) {
            wiz.putProperty(SAVED_IMPORT_TARGET_PROVIDER, configLast);
//          final  Class<C> clz = getImportTargetImpl();
//            Lookup.getDefault().lookupAll(clz).stream()
//                    .filter(sbi -> sbi.getProduct().equals(getProduct()) && sbi.getProviderInfo().getURL().equals(configLast))
//                    .map(clz::cast)
//                    .collect(CollectionUtil.singleton())
//                    .ifPresent(last -> {
//                        wiz.putProperty(AbstractFileImportAction.IMPORT_TARGET, last);
        }
    }

    protected String findLastImportTargetUrl() {
        return null;
    }

    protected Term findImportTerm(C config, T xml, Data wiz) throws IOException {
        Object p = wiz.getProperty(TERM);
        if (p instanceof Term) {
            return (Term) p;
        }
        return null;
    }

    protected void beforeWizardShow(Data wiz) {
    }

    protected void onWizardFinishOK(C config, Set<?> selected, T xml, Data wiz) {
        Term term;
        try {
            term = findImportTerm(config, xml, wiz);
        } catch (IOException ex) {
            ImportUtil.getIO().getErr().println(ex);
            return;
        }
        final AbstractUpdater<?> updater = createUpdater(selected, config, term, wiz);
        if (updater != null) {
            final Boolean dr = (Boolean) wiz.getProperty(AbstractFileImportAction.PROP_DRY_RUN);
            if (dr != null) {
                updater.setDryRun(dr);
            }
            final RequestProcessor.Task task = config.getWebServiceProvider().getDefaultRequestProcessor().post(updater);
            task.addTaskListener(t -> onUpdateFinished(config, selected, xml, wiz, updater));
        }
    }

    protected AbstractUpdater<?> createUpdater(Set<?> selected, C config, Term term, Data wiz) {
        final Class<I> implClass = getImportTargetsItemImpl();
        I[] iti = ((Set<I>) selected).stream()
                .map(implClass::cast)
                .toArray(i -> (I[]) Array.newInstance(implClass, i));
        return (AbstractUpdater<I>) new TargetItemsUpdater<>((ImportTargetsItem[]) iti, config.getWebServiceProvider(), term, null);
    }

    protected void onUpdateFinished(C config, Set<?> selected, T xml, Data wiz, AbstractUpdater<?> updater) {
    }

}
