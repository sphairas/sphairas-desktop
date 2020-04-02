/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target.slope;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.journal.Journal;
import org.thespheres.betula.journal.JournalRecord;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;

/**
 *
 * @author boris.heithecker
 */
public class StudentChartModel {

    private ObservableList<BarChart.Series> bcData;
    EditableJournal<?, ?> journal;
    EditableParticipant participant;
    private EditableJournal<? extends JournalRecord, ? extends Journal<? extends JournalRecord>> current;
    private Map<RecordId, CumulativeSum.PointAt2> cMap;
    private BarChart<String, Double> chart;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    void setCurrent(EditableParticipant ep) {
        final EditableJournal<?, ?> ej = ep != null ? ep.getEditableJournal() : null;
        cMap = ep != null ? CumulativeSum.create2(ej, ep.getIndex()) : null;
        if (!Objects.equals(ej, journal)) {
            Platform.runLater(() -> updateJournal(ej, ep));
        } else if (!Objects.equals(ep, participant)) {
            Platform.runLater(() -> updateParticipant(ep));
        }

    }

    BarChart createBarChart() {
        if (chart == null) {
            final CategoryAxis xAxis = new CategoryAxis();
//        xAxis.setCategories(FXCollections.<String>observableArrayList(getCurrentDates()));
//            xAxis.setLabel("Year");
            xAxis.setTickLabelRotation(90.0);

            final NumberAxis yAxis = new NumberAxis(0d, 250d, 10d);
//            yAxis.setTickUnit(10d);
            yAxis.setLabel("Wert");
            chart = new BarChart(xAxis, yAxis, getBarChartData());
        }
        return chart;
    }

    ObservableList<BarChart.Series> getBarChartData() {
        if (bcData == null) {
            bcData = FXCollections.<BarChart.Series>observableArrayList();
            final ObservableList<BarChart.Data<String, Double>> series = FXCollections.<BarChart.Data<String, Double>>observableArrayList();
//            updateSeries(series, null, null);
            bcData.add(new BarChart.Series(series));
        }
        return bcData;
    }

    private void updateSeries(final ObservableList<BarChart.Data<String, Double>> series, final EditableJournal<?, ?> ej, final EditableParticipant ep) {
        final CategoryAxis xAxis = (CategoryAxis) chart.getXAxis();
        final List<String> cat = ej.getEditableRecords().stream()
                .map(er -> date(er))
                .collect(Collectors.toList());
        xAxis.setCategories(FXCollections.<String>observableArrayList(cat));
        series.clear();
        if (ej != null) {
            ej.getEditableRecords().stream()
                    .map(er -> new BarChart.Data(date(er), getValueAt(er, ep)))
                    .forEach(series::add);
        }
    }

    private String date(EditableRecord<?> er) {
        return DTF.format(er.getRecordId().getLocalDateTime());
    }

    private Double getValueAt(final EditableRecord<?> er, final EditableParticipant ep) {
        if (ep == null || cMap == null) {
            return 0d;
        }
        final CumulativeSum.PointAt2 p = cMap.get(er.getRecordId());
        if (p == null) {
            return 0d;
        }
        return p.getAdjustedValue();
    }

    private void updateJournal(EditableJournal<?, ?> ej, EditableParticipant ep) {
        final BarChart.Series<String, Double> s = (BarChart.Series<String, Double>) bcData.get(0);
        final ObservableList<BarChart.Data<String, Double>> data = s.getData();
        updateSeries(data, ej, ep);
        current = ej;
    }

    private void updateParticipant(EditableParticipant ep) {
        final BarChart.Series<String, Double> s = (BarChart.Series<String, Double>) bcData.get(0);
        final ObservableList<BarChart.Data<String, Double>> data = s.getData();
        if (current != null) {
            current.getEditableRecords().stream()
                    .forEach(er -> data.get(er.getIndex()).setYValue(getValueAt(er, ep)));
        }
    }

}
