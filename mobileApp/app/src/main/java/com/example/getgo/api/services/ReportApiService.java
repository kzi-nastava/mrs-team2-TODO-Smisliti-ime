package com.example.getgo.api.services;

import com.example.getgo.dtos.report.ReportResponseDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ReportApiService {

    @GET("api/reports/driver")
    Call<ReportResponseDTO> getDriverReport(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("api/reports/passenger")
    Call<ReportResponseDTO> getPassengerReport(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("api/reports/admin/drivers")
    Call<ReportResponseDTO> getAdminDriversReport(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("api/reports/admin/passengers")
    Call<ReportResponseDTO> getAdminPassengersReport(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("api/reports/admin/driver")
    Call<ReportResponseDTO> getAdminDriverReport(
            @Query("email") String email,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("api/reports/admin/passenger")
    Call<ReportResponseDTO> getAdminPassengerReport(
            @Query("email") String email,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );
}