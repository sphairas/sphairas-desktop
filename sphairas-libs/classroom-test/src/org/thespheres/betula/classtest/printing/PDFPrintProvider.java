/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.printing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.XMLDataObject;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.ExternalGraphic;
import org.plutext.jaxb.xslfo.Root;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.classtest.chart.ChartUtil;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.table2.ClasstestTableSupport;
import org.thespheres.betula.listprint.PDFFactory;
import org.thespheres.betula.listprint.XSLFOException;
import org.thespheres.betula.listprint.builder.RootBuilder;
import org.thespheres.betula.listprint.builder.TableBuilder;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
@Messages({"PDFPrintProvider.print.displayName=Ergebnisse {0}"})
public class PDFPrintProvider implements PDFFactory {

    private final ClasstestTableSupport env;

    public PDFPrintProvider(ClasstestTableSupport env) {
        this.env = env;
    }

    @Override
    public Root createRoot() throws XSLFOException {
        TableBuilder tb = new ClassroomTestTableBuilder(env.getModel());
        String displayName = UIUtilities.findDisplayName(env.getDataObject());
        RootBuilder rb = new RootBuilder(displayName, tb.build());
        EditableClassroomTest etest = env.getDataObject().getLookup().lookup(EditableClassroomTest.class);
        AssessmentContext ac = env.getDataObject().getLookup().lookup(AssessmentContext.class);
        AssessmentConvention acc = env.getDataObject().getLookup().lookup(AssessmentConvention.class);
        if (etest != null && ac != null && acc != null) {
            rb.addFlow(new ClassroomDistributionTableBuilder(etest, acc, ac).build());
        }

        final DefaultCategoryDataset data = new DefaultCategoryDataset();
        final DefaultCategoryDataset nd = new DefaultCategoryDataset();
        JFreeChart chart = ChartUtil.createChart(data, nd);
        ChartUtil.populateData(env.getLookup().lookup(ClassroomTestEditor2.class), data, nd, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ChartUtilities.writeChartAsPNG(baos, chart, 150, 150);
        } catch (IOException ex) {
        }
        String img = Base64.getEncoder().encodeToString(baos.toByteArray());
        Block add = new Block();
        ExternalGraphic eg = new ExternalGraphic();
        String src = "url('data:image/png;base64," + img + "')";
        eg.setSrc(src);
        add.getContent().add(eg);
        rb.addFlow(add);
        return rb.build();
    }

    @Override
    public OutputStream getOutputStream(String mimeType) throws IOException {
        final XMLDataObject data = env.getDataObject();
        String name = NbBundle.getMessage(PDFPrintProvider.class, "PDFPrintProvider.print.displayName", data.getNodeDelegate().getDisplayName());
        FileObject folder = data.getPrimaryFile().getParent();
        String n = FileUtil.findFreeFileName(folder, name, "pdf");
        return folder.createData(n, "pdf").getOutputStream();
    }

}
