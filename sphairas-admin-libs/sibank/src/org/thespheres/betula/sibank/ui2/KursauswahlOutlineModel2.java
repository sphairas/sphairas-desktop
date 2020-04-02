/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2;

import org.thespheres.betula.sibank.ui2.impl.ImportUnitsAction;
import java.io.IOException;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.DatenExportXml;
import org.thespheres.betula.sibank.DatenExportXml.File;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.xmlimport.uiutil.OutlineModelNode;
import org.thespheres.betula.sibank.SiBankKlasseItem;
import org.thespheres.betula.sibank.UniqueSatzDistinguisher;
import org.thespheres.betula.sibank.ui2.impl.SiBankDefaultColumns;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractSelectNodesOutlineModel;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
@Messages({"KursauswahlOutlineModel2.columnName.fach=Fach (Sibank)",
    "KursauswahlOutlineModel2.columnName.fachart=Fachart / Kursnr. (Sibank)",
    "KursauswahlOutlineModel2.columnName.lehrer=Lehrer/Lehrerin (SiBank)",
    "KursauswahlOutlineModel2.error.addStudent=Der Schüler/die Schülerin {0} konnte dem Kurs {1} nicht hinzugefügt werden!"})
public class KursauswahlOutlineModel2<T extends ImportTargetsItem & OutlineModelNode> extends AbstractSelectNodesOutlineModel<T> {

    public static final String[] COLUMNS = {"fach", "fachart", "lehrer"};
    private DatenExportXml daten;
    protected SiBankImportData wizard;
    private SiBankImportTarget configuration;

    public KursauswahlOutlineModel2() {
        super(NbBundle.getMessage(SiBankDefaultColumns.class, "SiBankDefaultColumns.product"), COLUMNS);
    }

    @Override
    protected String getColumnDisplayName(String id) {
        try {
            return NbBundle.getMessage(KursauswahlOutlineModel2.class, "KursauswahlOutlineModel2.columnName." + id, product);
        } catch (MissingResourceException mrex) {
            Logger.getLogger(KursauswahlOutlineModel2.class.getName()).log(Level.WARNING, mrex.getMessage(), mrex);
            return id;
        }
    }

    public void initialize(SiBankImportTarget config, SiBankImportData<SiBankKursItem> wiz) {
        this.wizard = wiz;
        this.configuration = config;
        File type = (DatenExportXml.File) wizard.getProperty(ImportUnitsAction.SIBANK_IMPORT_ACTION_TYPE);
        this.daten = (DatenExportXml) wizard.getProperty(AbstractFileImportAction.DATA);
        this.selected = (Set<T>) wizard.getProperty(AbstractFileImportAction.SELECTED_NODES);
        this.clones = (Map<T, Set<T>>) wizard.getProperty(AbstractFileImportAction.CLONED_NODES);

        switch (type) {
            case KURSE: {
                List<String> jahrgaenge = daten.satzes.stream()
                        .filter(s -> !StringUtils.isBlank(s.stufe))
                        .map(s -> s.stufe)
                        .distinct()
                        .sorted(Comparator.comparingInt(Integer::parseInt))
                        .collect(Collectors.toList());
                for (String jahrgang : jahrgaenge) {
                    DefaultMutableTreeNode n = new DefaultMutableTreeNode("Jg. " + jahrgang);
                    root.add(n);

                    final Map<UniqueSatzDistinguisher, SiBankKursItem> ret = new HashMap<>();

                    daten.satzes.stream()
                            .filter(s -> !StringUtils.isBlank(s.stufe))
                            .forEach(s -> {
                                if (s.stufe.equals(jahrgang) && s.fach != null) {
                                    UniqueSatzDistinguisher uid = new UniqueSatzDistinguisher(s);
                                    try {
                                        ret.computeIfAbsent(uid, u -> {
                                            SiBankKursItem sk = new SiBankKursItem(u, DatenExportXml.File.KURSE, wiz);
                                            sk.initialize(configuration, wizard);
                                            return sk;
                                        }).addStudentFromSatz(s, true);                                        
                                    } catch (IOException ex) {
                                        ImportUtil.getIO().getErr().println(ex);
                                    }
                                }
                            });
                    ret.values().stream()
                            .sorted(kursComparator())
                            .distinct()
                            .map(sbit -> new DefaultMutableTreeNode(sbit, false))
                            .forEach(n::add);
                }
                break;
            }
            case SCHUELER: {
                List<String> jahrgaenge = daten.satzes.stream()
                        .filter(s -> !StringUtils.isBlank(s.stufe))
                        .map(s -> s.stufe)
                        .distinct()
                        .sorted(Comparator.comparingInt(Integer::parseInt))
                        .collect(Collectors.toList());
                for (String jahrgang : jahrgaenge) {
                    int stufe;
                    try {
                        stufe = Integer.parseInt(jahrgang);
                    } catch (NumberFormatException nex) {
                        ImportUtil.getIO().getErr().println(nex);
                        continue;
                    }

                    DefaultMutableTreeNode n = new DefaultMutableTreeNode("Jg. " + jahrgang);
                    root.add(n);

                    final Map<String, SiBankKlasseItem> ret = new HashMap<>();
                    for (DatenExportXml.Satz s : daten.satzes) {
                        if (s.stufe.equals(jahrgang) && s.klasse != null) {
                            try {
                                ret.computeIfAbsent(s.klasse, kla -> {
                                    SiBankKlasseItem sk = new SiBankKlasseItem(kla, stufe);
                                    sk.initialize(configuration, wizard);
                                    return sk;
                                }).addStudentFromSatz(s);
                            } catch (IOException ex) {
                                ImportUtil.getIO().getErr().println(ex);
                            }
                        }
                    }
                    ret.values().stream()
                            .sorted(Comparator.comparing(SiBankKlasseItem::getKlasse, Collator.getInstance(Locale.GERMANY)))
                            .distinct()
                            .map(sbit -> new DefaultMutableTreeNode(sbit, true))
                            .forEach(n::add);
                }
                break;
            }
            case AGS:
                TreeMap<String, SiBankKursItem> ags = new TreeMap<>(Collator.getInstance(Locale.GERMANY));
                for (DatenExportXml.Satz s : daten.satzes) {
                    if (s.isAG()) {
                        String ag = s.fach;
                        try {
                            ags.computeIfAbsent(ag, a -> {
                                UniqueSatzDistinguisher uid = new UniqueSatzDistinguisher(s);
                                SiBankKursItem sk = new SiBankKursItem(uid, File.AGS, wiz);
                                sk.initialize(configuration, wizard);
                                return sk;
                            }).addStudentFromSatz(s, true);
                        } catch (IOException ex) {
                            ImportUtil.getIO().getErr().println(ex);
                        }
                    }
                }
                ags.values().stream()
                        .sorted(Comparator.comparing(SiBankKursItem::getFach, Collator.getInstance(Locale.GERMANY)))
                        .distinct()
                        .map(sbit -> new DefaultMutableTreeNode(sbit, true))
                        .forEach(root::add);
                break;
            default:
                break;
        }
        treeModel.reload();
    }

    private static Comparator<SiBankKursItem> kursComparator() {
        final Collator collator = Collator.getInstance(Locale.GERMANY);
        return (SiBankKursItem ku1, SiBankKursItem ku2) -> {
            String k1 = StringUtils.trimToEmpty(ku1.getFachart());
            String k2 = StringUtils.trimToEmpty(ku2.getFachart());
            int r = collator.compare(k1, k2);
            if (r != 0) {
                return r;
            }
            k1 = StringUtils.trimToEmpty(ku1.getSourceSubject());
            k2 = StringUtils.trimToEmpty(ku2.getSourceSubject());
            r = collator.compare(k1, k2);
            if (r != 0) {
                return r;
            }
            k1 = StringUtils.trimToEmpty(ku1.getKursnr());
            k2 = StringUtils.trimToEmpty(ku2.getKursnr());
            r = collator.compare(k1, k2);
            if (r != 0) {
                return r;
            }
            k1 = StringUtils.trimToEmpty(ku1.getSourceSigneeName());
            k2 = StringUtils.trimToEmpty(ku2.getSourceSigneeName());
            return collator.compare(k1, k2);
        };
    }

    @Override
    protected T extractNode(Object o) {
        Object uo = ((DefaultMutableTreeNode) o).getUserObject();
        try {
            return (T) uo;
        } catch (ClassCastException e) {
        }
        return null;
    }

    @Override
    public Object getValueFor(Object o, String id) {
        OutlineModelNode l = extractNode(o);
        if (l != null) {
            return l.getColumn(id);
        }
        return "";
    }

    @Override
    public String getDisplayName(Object o) {
        T sbk = extractNode(o);
        return sbk != null ? sbk.getHtmlDisplayName() : o.toString();
    }

    @Override
    public String getTooltipText(Object o) {
        T l = extractNode(o);
        if (l != null) {
            return l.getTooltip();
        }
        return "";
    }

}
