package com.example.getgo.utils;

import android.graphics.Color;

import com.example.getgo.dtos.report.DailyReportDTO;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartHelper {

    private static final int COLOR_PRIMARY = Color.parseColor("#133E87");
    private static final int COLOR_GREEN = Color.parseColor("#2E7D32");
    private static final int COLOR_ORANGE = Color.parseColor("#E65100");
    
    public static void setupRidesChart(BarChart chart, List<DailyReportDTO> dailyStats) {
        setupBarChart(chart, dailyStats, "rides", COLOR_PRIMARY);
    }

    public static void setupKilometersChart(BarChart chart, List<DailyReportDTO> dailyStats) {
        setupBarChart(chart, dailyStats, "kilometers", COLOR_GREEN);
    }

    public static void setupMoneyChart(BarChart chart, List<DailyReportDTO> dailyStats) {
        setupBarChart(chart, dailyStats, "money", COLOR_ORANGE);
    }

    private static void setupBarChart(BarChart chart, List<DailyReportDTO> dailyStats,
                                      String dataType, int color) {
        if (dailyStats == null || dailyStats.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No data available");
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < dailyStats.size(); i++) {
            DailyReportDTO daily = dailyStats.get(i);
            float value = 0f;

            switch (dataType) {
                case "rides":
                    value = daily.getNumberOfRides();
                    break;
                case "kilometers":
                    value = (float) daily.getTotalKilometers();
                    break;
                case "money":
                    value = (float) daily.getTotalMoney();
                    break;
            }

            entries.add(new BarEntry(i, value));
            labels.add(formatDateLabel(daily.getDate()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(color);
        dataSet.setValueTextColor(COLOR_PRIMARY);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextColor(COLOR_PRIMARY);
        xAxis.setLabelRotationAngle(45f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(COLOR_PRIMARY);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.animateY(500);
        chart.invalidate();
    }

    private static String formatDateLabel(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return "";
        }
    }
}