/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import org.jdesktop.swingx.JXTable;
import org.openide.util.NbBundle;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.impl.AbstractXmlCsvImportItem;
import org.thespheres.betula.xmlimport.uiutil.CreateDocumentsComponent;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"XmlDataDocumentsVisualPanel.step.name=Dokumente anlegen"})
public abstract class XmlDataDocumentsVisualPanel<I extends AbstractXmlCsvImportItem> extends JPanel implements CreateDocumentsComponent<I, XmlCsvImportSettings<I>> {

    private final JScrollPane scrollPanel;
    protected final JXTable table;
    private final JToolBar toolbar;
    protected XmlCsvImportSettings<I> wizard;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    protected XmlDataDocumentsVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        toolbar = new JToolBar();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolbar.setFloatable(false);
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        add(toolbar, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
    }

    protected abstract void initialize(XmlCsvImportSettings<I> wiz);

    @Override
    public String getName() {
        return NbBundle.getMessage(XmlDataDocumentsVisualPanel.class, "XmlDataDocumentsVisualPanel.step.name");
    }

    @Override
    public JTable getTable() {
        return table;
    }

    @Override
    public XmlCsvImportSettings<I> getSettings() {
        return wizard;
    }

}
