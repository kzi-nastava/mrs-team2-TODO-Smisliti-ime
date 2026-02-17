package com.example.getgo.fragments.passengers;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.dtos.report.ReportResponseDTO;
import com.example.getgo.repositories.ReportRepository;
import com.example.getgo.utils.ChartHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class PassengerReportFragment extends Fragment {

    private static final String TAG = "PassengerReportFragment";

    private MaterialButton btnStartDate, btnEndDate, btnGenerateReport;
    private androidx.cardview.widget.CardView tvError, layoutLoading, layoutNoData;
    private TextView tvErrorText;
    private TextView tvTotalRides, tvAvgRides;
    private TextView tvTotalKm, tvAvgKm;
    private TextView tvTotalMoney, tvAvgMoney;
    private LinearLayout layoutReport;
    private BarChart chartRides, chartKm, chartMoney;

    private Calendar startDate, endDate;
    private SimpleDateFormat dateFormatter;
    private ReportRepository reportRepository;

    public PassengerReportFragment() {
    }

    public static PassengerReportFragment newInstance() {
        return new PassengerReportFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        reportRepository = ReportRepository.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_report, container, false);

        initViews(view);
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        btnStartDate = view.findViewById(R.id.btnStartDate);
        btnEndDate = view.findViewById(R.id.btnEndDate);
        btnGenerateReport = view.findViewById(R.id.btnGenerateReport);

        tvError = view.findViewById(R.id.tvError);
        tvErrorText = view.findViewById(R.id.tvErrorText);

        layoutLoading = view.findViewById(R.id.layoutLoading);
        layoutReport = view.findViewById(R.id.layoutReport);
        layoutNoData = view.findViewById(R.id.layoutNoData);

        tvTotalRides = view.findViewById(R.id.tvTotalRides);
        tvAvgRides = view.findViewById(R.id.tvAvgRides);
        tvTotalKm = view.findViewById(R.id.tvTotalKm);
        tvAvgKm = view.findViewById(R.id.tvAvgKm);
        tvTotalMoney = view.findViewById(R.id.tvTotalMoney);
        tvAvgMoney = view.findViewById(R.id.tvAvgMoney);

        chartRides = view.findViewById(R.id.chartRides);
        chartKm = view.findViewById(R.id.chartKm);
        chartMoney = view.findViewById(R.id.chartMoney);
    }

    private void setupListeners() {
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
        btnGenerateReport.setOnClickListener(v -> generateReport());
    }

    private void showDatePicker(boolean isStartDate) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStartDate ? "Select Start Date" : "Select End Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            selectedDate.setTimeInMillis(selection);

            if (isStartDate) {
                startDate = selectedDate;
                btnStartDate.setText(dateFormatter.format(selectedDate.getTime()));
            } else {
                endDate = selectedDate;
                btnEndDate.setText(dateFormatter.format(selectedDate.getTime()));
            }
        });

        picker.show(getParentFragmentManager(), picker.toString());
    }

    private void generateReport() {
        if (startDate == null || endDate == null) {
            showError("Please select both start and end date.");
            return;
        }

        if (startDate.after(endDate)) {
            showError("Start date must be before end date.");
            return;
        }

        hideError();
        showLoading();

        String startDateStr = dateFormatter.format(startDate.getTime());
        String endDateStr = dateFormatter.format(endDate.getTime());

        new Thread(() -> {
            try {
                ReportResponseDTO report = reportRepository.getPassengerReport(startDateStr, endDateStr);

                requireActivity().runOnUiThread(() -> {
                    hideLoading();
                    displayReport(report);
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch report", e);
                requireActivity().runOnUiThread(() -> {
                    hideLoading();
                    showError("Failed to load report: " + e.getMessage());
                });
            }
        }).start();
    }

    private void displayReport(ReportResponseDTO report) {
        if (report == null) {
            showError("No data available for the selected date range.");
            return;
        }

        layoutReport.setVisibility(View.VISIBLE);
        layoutNoData.setVisibility(View.GONE);

        tvTotalRides.setText(String.valueOf(report.getTotalRides()));
        tvAvgRides.setText(String.format(Locale.getDefault(), "avg %.2f/day", report.getAvgRidesPerDay()));

        tvTotalKm.setText(String.format(Locale.getDefault(), "%.2f km", report.getTotalKilometers()));
        tvAvgKm.setText(String.format(Locale.getDefault(), "avg %.2f km/day", report.getAvgKilometersPerDay()));

        tvTotalMoney.setText(String.format(Locale.getDefault(), "%.2f RSD", report.getTotalMoney()));
        tvAvgMoney.setText(String.format(Locale.getDefault(), "avg %.2f RSD/day", report.getAvgMoneyPerDay()));

        ChartHelper.setupRidesChart(chartRides, report.getDailyStats());
        ChartHelper.setupKilometersChart(chartKm, report.getDailyStats());
        ChartHelper.setupMoneyChart(chartMoney, report.getDailyStats());
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutReport.setVisibility(View.GONE);
        layoutNoData.setVisibility(View.GONE);
        btnGenerateReport.setEnabled(false);
    }

    private void hideLoading() {
        layoutLoading.setVisibility(View.GONE);
        btnGenerateReport.setEnabled(true);
    }

    private void showError(String message) {
        tvErrorText.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
}