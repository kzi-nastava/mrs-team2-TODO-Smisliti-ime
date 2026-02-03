package com.example.getgo.fragments.drivers;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.dtos.inconsistencyReport.GetInconsistencyReportDTO;
import com.example.getgo.dtos.passenger.GetRidePassengerDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.api.services.RideApiService;

import android.text.Html;

import java.time.LocalDateTime;
import java.util.List;
import java.time.format.DateTimeFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DriverRideDetailFragment extends Fragment {

    private GetRideDTO ride;
    private static final String ARG_RIDE = "arg_ride";

    public DriverRideDetailFragment() {
        // Required empty public constructor
    }


    public static DriverRideDetailFragment newInstance(GetRideDTO ride) {
        DriverRideDetailFragment fragment = new DriverRideDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RIDE, ride);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            ride = (GetRideDTO) getArguments().getSerializable(ARG_RIDE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_ride_detail, container, false);

        TextView date = view.findViewById(R.id.tvDate);
        TextView start = view.findViewById(R.id.tvStartLocation);
        TextView end = view.findViewById(R.id.tvEndLocation);
        TextView startTime = view.findViewById(R.id.tvStartTime);
        TextView endTime = view.findViewById(R.id.tvEndTime);
        TextView tvPanicActivated = view.findViewById(R.id.tvPanicActivated);
        TextView price = view.findViewById(R.id.tvPrice);
        TextView tvPassengers = view.findViewById(R.id.tvPassengers);


        if (ride != null) {

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

            if (ride.getStartingTime() != null) {
                setStyledText(date, "Date:", ride.getStartingTime().format(dateFormat));
                setStyledText(startTime, "Start time:", ride.getStartingTime().format(timeFormat));
            }

            if (ride.getFinishedTime() != null) {
                setStyledText(endTime, "End time:", ride.getFinishedTime().format(timeFormat));
            }

            setStyledText(start, "Start location:", ride.getStartPoint());
            setStyledText(end, "End location:", ride.getEndPoint());

            setStyledText(price, "Price:", "$" + ride.getPrice());
            setStyledText(tvPanicActivated, "Panic Activated:",
                    ride.getPanicActivated() != null && ride.getPanicActivated() ? "Yes" : "No");

            if (ride.getPassengers() != null && !ride.getPassengers().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (GetRidePassengerDTO p : ride.getPassengers()) {
                    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;• ").append(p.getUsername()).append("<br>");
                }
                setStyledText(tvPassengers, "Passengers: <br>", sb.toString());
            } else {
                setStyledText(tvPassengers, "Passengers:", "None");
            }


            TextView tvReportsLoading = view.findViewById(R.id.tvReportsLoading);
            TextView tvNoReports = view.findViewById(R.id.tvNoReports);
            LinearLayout reportsContainer = view.findViewById(R.id.reportsContainer);

            tvReportsLoading.setVisibility(View.VISIBLE);

            RideApiService rideApiService = ApiClient.getClient().create(RideApiService.class);

            rideApiService.getInconsistencyReports(ride.getId()).enqueue(new Callback<List<GetInconsistencyReportDTO>>() {
                @Override
                public void onResponse(Call<List<GetInconsistencyReportDTO>> call,
                                       Response<List<GetInconsistencyReportDTO>> response) {

                    tvReportsLoading.setVisibility(View.GONE);
                    Log.d("RIDE_DETAIL", "Retrofit response code = " + response.code());
                    Log.d("RIDE_DETAIL", "Retrofit response body = " + response.body());

                    if (response.isSuccessful() && response.body() != null) {

                        List<GetInconsistencyReportDTO> reports = response.body();

                        System.out.println("Retrofit: reports size = " + reports.size());
                        for (GetInconsistencyReportDTO r : reports) {
                            System.out.println("Report: text=" + r.getText() + ", email=" + r.getPassengerEmail() + ", createdAt=" + r.getCreatedAt());
                        }

                        if (reports.isEmpty()) {
                            tvNoReports.setVisibility(View.VISIBLE);
                            return;
                        }

                        for (GetInconsistencyReportDTO report : reports) {
                            View reportView = inflater.inflate(R.layout.item_report, reportsContainer, false);

                            TextView tvText = reportView.findViewById(R.id.tvReportText);
                            TextView tvMeta = reportView.findViewById(R.id.tvReportMeta);

                            tvText.setText("“" + report.getText() + "”");

                            String createdAtString = report.getCreatedAt();
                            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");

                            LocalDateTime createdAtDateTime = LocalDateTime.parse(createdAtString, inputFormatter);
                            String formattedDate = createdAtDateTime.format(outputFormatter);

                            String meta = "Reported by <b>" + report.getPassengerEmail() + "</b> • " + formattedDate;
                            tvMeta.setText(Html.fromHtml(meta, Html.FROM_HTML_MODE_LEGACY));



                            reportsContainer.addView(reportView);
                        }
                    }else {
                        Log.e("RIDE_DETAIL", "Server error: code=" + response.code() + ", message=" + response.message());
                        tvNoReports.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<List<GetInconsistencyReportDTO>> call, Throwable t) {
                    tvReportsLoading.setVisibility(View.GONE);
                    tvNoReports.setVisibility(View.VISIBLE);
                }
            });


        }
            return view;
    }
    private void setStyledText(TextView tv, String label, String value) {
        String html =
                "<b><font color='#133E87'>" + label + "</font></b> " +
                        "<font color='#757474'>" + value + "</font>";

        tv.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
    }


}