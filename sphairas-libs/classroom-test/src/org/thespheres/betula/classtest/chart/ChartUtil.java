/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLabelLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GroupingGrades;
import org.thespheres.betula.assess.GroupingGrades.GradeGroup;
import org.thespheres.betula.classtest.analytics.ClasstestAnalytics;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.GradesMean;

/**
 *
 * @author boris.heithecker
 */
@Messages({"ChartUtil.categoryAxisLabel=Note",
    "ChartUtil.categoryAxisLabelOverride=Gesamt: {0}",
    "ChartUtil.category.grade=Anzahl",
    "ChartUtil.category.distribution=Normalverteilung",
    "ChartUtil.valueAxisLabel=Anzahl", //    "ChartUtil.emptyTitle=---"
})
public class ChartUtil {

    public static JFreeChart createChart(DefaultCategoryDataset data, DefaultCategoryDataset nd) {
        JFreeChart ret = ChartFactory.createBarChart(null, //NbBundle.getMessage(ChartUtil.class, "ChartUtil.emptyTitle"),
                null,
                //                NbBundle.getMessage(ChartUtil.class, "ChartUtil.categoryAxisLabel"),
                NbBundle.getMessage(ChartUtil.class, "ChartUtil.valueAxisLabel"), data);
//        ret.setBorderVisible(false);
        ret.getLegend().setBorder(0.0, 0.0, 0.0, 0.0);
        ret.setPadding(new RectangleInsets(35.0, 5.0, 5.0, 5.0));

        CategoryPlot plot = (CategoryPlot) ret.getPlot();

        plot.setOutlineVisible(false);
        plot.setBackgroundPaint(null);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.GRAY);
//        plot.setRangeCrosshairVisible(true); //Null-Linie
//        plot.setRangeCrosshairPaint(Color.blue);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits(Locale.getDefault()));
        rangeAxis.setRange(-0.5, 10.0);

//        Font font = UIManager.getFont("Label.font");
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.decode("#9884c8"),
                1000.0f, 1000.0f, Color.decode("#e2dcef"));
//        GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.green,
//                0.0f, 0.0f, new Color(0, 64, 0));
        renderer.setSeriesPaint(0, gp0);
//        renderer.setSeriesFillPaint(0, Color.BLACK);
//        renderer.setAutoPopulateSeriesFillPaint(false);

        renderer.setLegendItemToolTipGenerator(
                new StandardCategorySeriesLabelGenerator("Tooltip: {0}"));

        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
//        NumberAxis axis2 = new NumberAxis("Dist");
//        axis2.setNumberFormatOverride(NumberFormat.getPercentInstance());
//        plot.setRangeAxis(1, axis2);
        plot.setDataset(1, nd);
        plot.setRenderer(1, lineRenderer);
        lineRenderer.setSeriesPaint(1, Color.ORANGE);
//        plot.mapDatasetToRangeAxis(1, 1);

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

//        ChartUtilities.applyCurrentTheme(ret);
//        TextTitle source = new TextTitle(;
//        source.setPosition(RectangleEdge.BOTTOM);
//        source.setHorizontalAlignment(HorizontalAlignment.RIGHT);
//        chart.addSubtitle(source);
        CategoryAxis domainAxis = plot.getDomainAxis();
//        domainAxis.setTickLabelFont(domainAxis.getTickLabelFont().deriveFont(Font.BOLD).deriveFont(16f));
        domainAxis.setTickLabelFont(domainAxis.getLabelFont().deriveFont(Font.BOLD));
//        domainAxis.setCategoryLabelPositions(
//                CategoryLabelPositions.createUpRotationLabelPositions(
//                        Math.PI / 6.0));
//        domainAxis.setLabelFont(font.deriveFont(14f).deriveFont(Font.BOLD));
        domainAxis.setLabelLocation(AxisLabelLocation.LOW_END);
        domainAxis.setLabelInsets(RectangleInsets.ZERO_INSETS);

        renderer.setBarPainter(new StandardBarPainter());
//        renderer.setItemMargin(0.0);
        renderer.setMaximumBarWidth(0.1);
//        renderer.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER, TextAnchor.BOTTOM_CENTER, -Math.PI / 6.0));
        renderer.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER, TextAnchor.BOTTOM_CENTER, 0.0));
        renderer.setSeriesItemLabelsVisible(0, true);
        renderer.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator("({2})", NumberFormat.getIntegerInstance(Locale.getDefault())));
//        renderer.setSeriesItemLabelFont(0, new Font("Monospaced", Font.PLAIN, 12));
        renderer.setSeriesItemLabelFont(0, domainAxis.getLabelFont().deriveFont(Font.BOLD));
        return ret;
    }

    public static void populateData(final ClassroomTestEditor2 editor, DefaultCategoryDataset data, DefaultCategoryDataset nd, boolean group) {
        EditableClassroomTest etest = editor.getEditableClassroomTest();
        AssessmentConvention ac = editor.getAssessmentConvention();
        final String catGrade = NbBundle.getMessage(ChartUtil.class, "ChartUtil.category.grade");
        final String catND = NbBundle.getMessage(ChartUtil.class, "ChartUtil.category.distribution");
        if (ac != null && etest != null) {
            final GroupingGrades gg;
            if (group && editor.getAssessmentConvention() instanceof GroupingGrades) {
                gg = (GroupingGrades) editor.getAssessmentConvention();
            } else {
                gg = null;
            }
            final GradesMean gm = new GradesMean(etest);
//            Lists.reverse(Arrays.asList(ac.getAllLinkedGrades())).stream()
            final Grade[] arr = ac instanceof AssessmentConvention.OfBiasable
                    ? ((AssessmentConvention.OfBiasable) ac).getAllGradesUnbiased()
                    : ac.getAllGrades();
            Arrays.stream(arr)
                    .forEach(g -> {
                        int num = etest.getGrades().get(g);
                        final String row;
                        final GradeGroup gr;
                        if (gg != null && (gr = gg.findGroup(g)) != null) {
                            row = gr.getDisplayLabel();
                        } else {
                            row = g.getShortLabel();
                        }
                        try {
                            data.incrementValue(num, catGrade, row);
                        } catch (UnknownKeyException e) {
                            data.addValue(num, catGrade, row);
                        }
                        Double nv = ClasstestAnalytics.getInstance().valueOf(g);
                        if (nv != null && nd != null) {
                            double ndist = gm.normalDistribution(nv);
                            double v = ndist * (double) etest.getEditableStudents().size();
                            try {
                                nd.incrementValue(v, catND, row);
                            } catch (UnknownKeyException e) {
                                nd.addValue(v, catND, row);
                            }
                        }
                    });
        }
    }

}
