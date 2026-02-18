package com.example.getgo.repositories;

import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.ReportApiService;
import com.example.getgo.dtos.report.ReportResponseDTO;

import retrofit2.Response;

public class ReportRepository {
    private static final String TAG = "ReportRepository";
    private static ReportRepository instance;

    private ReportRepository() {}

    public static synchronized ReportRepository getInstance() {
        if (instance == null) {
            instance = new ReportRepository();
        }
        return instance;
    }

    public ReportResponseDTO getDriverReport(String startDate, String endDate) throws Exception {
        ReportApiService service = ApiClient.getClient().create(ReportApiService.class);
        Response<ReportResponseDTO> response = service.getDriverReport(startDate, endDate).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Driver report fetched successfully");
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch driver report: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch driver report: " + errBody);
        }
    }

    public ReportResponseDTO getPassengerReport(String startDate, String endDate) throws Exception {
        ReportApiService service = ApiClient.getClient().create(ReportApiService.class);
        Response<ReportResponseDTO> response = service.getPassengerReport(startDate, endDate).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Passenger report fetched successfully");
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch passenger report: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch passenger report: " + errBody);
        }
    }

    public ReportResponseDTO getAdminDriversReport(String startDate, String endDate) throws Exception {
        ReportApiService service = ApiClient.getClient().create(ReportApiService.class);
        Response<ReportResponseDTO> response = service.getAdminDriversReport(startDate, endDate).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Admin drivers report fetched successfully");
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch admin drivers report: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch admin drivers report: " + errBody);
        }
    }

    public ReportResponseDTO getAdminPassengersReport(String startDate, String endDate) throws Exception {
        ReportApiService service = ApiClient.getClient().create(ReportApiService.class);
        Response<ReportResponseDTO> response = service.getAdminPassengersReport(startDate, endDate).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Admin passengers report fetched successfully");
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch admin passengers report: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch admin passengers report: " + errBody);
        }
    }

    public ReportResponseDTO getAdminDriverReport(String email, String startDate, String endDate) throws Exception {
        ReportApiService service = ApiClient.getClient().create(ReportApiService.class);
        Response<ReportResponseDTO> response = service.getAdminDriverReport(email, startDate, endDate).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Admin driver report fetched for: " + email);
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch admin driver report: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch admin driver report: " + errBody);
        }
    }

    public ReportResponseDTO getAdminPassengerReport(String email, String startDate, String endDate) throws Exception {
        ReportApiService service = ApiClient.getClient().create(ReportApiService.class);
        Response<ReportResponseDTO> response = service.getAdminPassengerReport(email, startDate, endDate).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Admin passenger report fetched for: " + email);
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch admin passenger report: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch admin passenger report: " + errBody);
        }
    }
}