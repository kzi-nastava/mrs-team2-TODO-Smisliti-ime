package com.example.getgo.repositories;

import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.AdminApiService;
import com.example.getgo.dtos.driver.CreateDriverDTO;
import com.example.getgo.dtos.driver.CreatedDriverDTO;
import com.example.getgo.dtos.general.Page;
import com.example.getgo.dtos.request.AcceptDriverChangeRequestDTO;
import com.example.getgo.dtos.request.GetDriverAvatarChangeRequestDTO;
import com.example.getgo.dtos.request.GetDriverVehicleChangeRequestDTO;
import com.example.getgo.dtos.request.GetPersonalDriverChangeRequestDTO;
import com.example.getgo.dtos.request.RejectDriverChangeRequestDTO;

import retrofit2.Response;

public class AdminRepository {
    private static final String TAG = "AdminRepository";
    private static AdminRepository instance;

    private AdminRepository() {}

    public static synchronized AdminRepository getInstance() {
        if (instance == null) {
            instance = new AdminRepository();
        }
        return instance;
    }

    public CreatedDriverDTO registerDriver(CreateDriverDTO dto) throws Exception {
        AdminApiService service = ApiClient.getClient().create(AdminApiService.class);
        Response<CreatedDriverDTO> response = service.registerDriver(dto).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Driver registered: " + response.body().getEmail());
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to register driver: " + response.code() + " - " + errBody);
            throw new Exception("Failed to register driver: " + errBody);
        }
    }

    public Page<GetPersonalDriverChangeRequestDTO> getPendingPersonalRequests(int page, int size) throws Exception {
        AdminApiService service = ApiClient.getClient().create(AdminApiService.class);
        Response<Page<GetPersonalDriverChangeRequestDTO>> response =
                service.getPendingPersonalChangeRequests(page, size).execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch personal requests: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch personal change requests");
        }
    }

    public Page<GetDriverVehicleChangeRequestDTO> getPendingVehicleRequests(int page, int size) throws Exception {
        AdminApiService service = ApiClient.getClient().create(AdminApiService.class);
        Response<Page<GetDriverVehicleChangeRequestDTO>> response =
                service.getPendingVehicleChangeRequests(page, size).execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch vehicle requests: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch vehicle change requests");
        }
    }

    public Page<GetDriverAvatarChangeRequestDTO> getPendingAvatarRequests(int page, int size) throws Exception {
        AdminApiService service = ApiClient.getClient().create(AdminApiService.class);
        Response<Page<GetDriverAvatarChangeRequestDTO>> response =
                service.getPendingPictureChangeRequests(page, size).execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch avatar requests: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch avatar change requests");
        }
    }

    public AcceptDriverChangeRequestDTO approvePersonalRequest(Long requestId) throws Exception {
        AdminApiService service = ApiClient.getClient().create(AdminApiService.class);
        Response<AcceptDriverChangeRequestDTO> response =
                service.approvePersonalChangeRequest(requestId).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Personal request approved: " + requestId);
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to approve personal request: " + response.code() + " - " + errBody);
            throw new Exception("Failed to approve personal request");
        }
    }

    public AcceptDriverChangeRequestDTO approveVehicleRequest(Long requestId) throws Exception {
        AdminApiService service = ApiClient.getClient().create(AdminApiService.class);
        Response<AcceptDriverChangeRequestDTO> response =
                service.approveVehicleChangeRequest(requestId).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Vehicle request approved: " + requestId);
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to approve vehicle request: " + response.code() + " - " + errBody);
            throw new Exception("Failed to approve vehicle request");
        }
    }

    public AcceptDriverChangeRequestDTO approveAvatarRequest(Long requestId) throws Exception {
        AdminApiService service = ApiClient.getClient().create(AdminApiService.class);
        Response<AcceptDriverChangeRequestDTO> response =
                service.approvePictureChangeRequest(requestId).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Avatar request approved: " + requestId);
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to approve avatar request: " + response.code() + " - " + errBody);
            throw new Exception("Failed to approve avatar request");
        }
    }

    public AcceptDriverChangeRequestDTO rejectPersonalRequest(Long requestId, String reason) throws Exception {
        AdminApiService service = ApiClient.getClient().create(AdminApiService.class);
        RejectDriverChangeRequestDTO dto = new RejectDriverChangeRequestDTO(reason);
        Response<AcceptDriverChangeRequestDTO> response =
                service.rejectPersonalChangeRequest(requestId, dto).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Personal request rejected: " + requestId);
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to reject personal request: " + response.code() + " - " + errBody);
            throw new Exception("Failed to reject personal request");
        }
    }

    public AcceptDriverChangeRequestDTO rejectVehicleRequest(Long requestId, String reason) throws Exception {
        AdminApiService service = ApiClient.getClient().create(AdminApiService.class);
        RejectDriverChangeRequestDTO dto = new RejectDriverChangeRequestDTO(reason);
        Response<AcceptDriverChangeRequestDTO> response =
                service.rejectVehicleChangeRequest(requestId, dto).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Vehicle request rejected: " + requestId);
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to reject vehicle request: " + response.code() + " - " + errBody);
            throw new Exception("Failed to reject vehicle request");
        }
    }

    public AcceptDriverChangeRequestDTO rejectAvatarRequest(Long requestId, String reason) throws Exception {
        AdminApiService service = ApiClient.getClient().create(AdminApiService.class);
        RejectDriverChangeRequestDTO dto = new RejectDriverChangeRequestDTO(reason);
        Response<AcceptDriverChangeRequestDTO> response =
                service.rejectPictureChangeRequest(requestId, dto).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Avatar request rejected: " + requestId);
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to reject avatar request: " + response.code() + " - " + errBody);
            throw new Exception("Failed to reject avatar request");
        }
    }
}