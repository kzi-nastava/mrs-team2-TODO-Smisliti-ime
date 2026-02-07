package com.example.getgo.fragments.admins;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getgo.R;
import com.example.getgo.adapters.RequestsAdapter;
import com.example.getgo.dtos.general.Page;
import com.example.getgo.dtos.request.GetDriverAvatarChangeRequestDTO;
import com.example.getgo.dtos.request.GetDriverVehicleChangeRequestDTO;
import com.example.getgo.dtos.request.GetPersonalDriverChangeRequestDTO;
import com.example.getgo.repositories.AdminRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminReviewDriverRequestsFragment extends Fragment {

    private MaterialButton btnPersonalTab, btnVehicleTab, btnAvatarTab;
    private TextView tvPaginationInfo;
    private Button btnPrevious, btnNext;
    private RecyclerView recyclerView;
    private LinearLayout layoutLoading, layoutEmpty;

    private AdminRepository adminRepository;
    private ExecutorService executor;
    private Handler mainHandler;
    private RequestsAdapter adapter;

    private String activeTab = "personal";
    private int currentPage = 0;
    private final int itemsPerPage = 2;

    private int personalTotal = 0;
    private int vehicleTotal = 0;
    private int avatarTotal = 0;

    private int personalTotalPages = 0;
    private int vehicleTotalPages = 0;
    private int avatarTotalPages = 0;

    public AdminReviewDriverRequestsFragment() {}

    public static AdminReviewDriverRequestsFragment newInstance() {
        return new AdminReviewDriverRequestsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminRepository = AdminRepository.getInstance();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_review_driver_requests, container, false);

        initializeViews(view);
        setupTabs();
        setupRecyclerView();
        loadAllTabCounts();
        loadRequests();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void initializeViews(View view) {
        btnPersonalTab = view.findViewById(R.id.btnPersonalTab);
        btnVehicleTab = view.findViewById(R.id.btnVehicleTab);
        btnAvatarTab = view.findViewById(R.id.btnAvatarTab);

        tvPaginationInfo = view.findViewById(R.id.tvPaginationInfo);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnNext = view.findViewById(R.id.btnNext);

        recyclerView = view.findViewById(R.id.recyclerView);
        layoutLoading = view.findViewById(R.id.layoutLoading);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        btnPrevious.setOnClickListener(v -> previousPage());
        btnNext.setOnClickListener(v -> nextPage());
    }

    private void setupTabs() {
        btnPersonalTab.setOnClickListener(v -> setActiveTab("personal"));
        btnVehicleTab.setOnClickListener(v -> setActiveTab("vehicle"));
        btnAvatarTab.setOnClickListener(v -> setActiveTab("avatar"));
        updateTabColors();
    }

    private void setupRecyclerView() {
        adapter = new RequestsAdapter(requireContext(), new RequestsAdapter.OnRequestActionListener() {
            @Override
            public void onApprove(Object request, Long requestId) {
                AdminReviewDriverRequestsFragment.this.showApproveAlert(requestId);
            }

            @Override
            public void onReject(Object request, Long requestId) {
                AdminReviewDriverRequestsFragment.this.showRejectAlert(requestId);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setActiveTab(String tab) {
        if (!activeTab.equals(tab)) {
            activeTab = tab;
            currentPage = 0;
            updateTabColors();
            loadRequests();
        }
    }

    private void updateTabColors() {
        btnPersonalTab.setBackgroundTintList(getResources().getColorStateList(
                activeTab.equals("personal") ? R.color.selected_blue : R.color.dark_blue, null));
        btnVehicleTab.setBackgroundTintList(getResources().getColorStateList(
                activeTab.equals("vehicle") ? R.color.selected_blue : R.color.dark_blue, null));
        btnAvatarTab.setBackgroundTintList(getResources().getColorStateList(
                activeTab.equals("avatar") ? R.color.selected_blue : R.color.dark_blue, null));
    }

    private void loadAllTabCounts() {
        executor.execute(() -> {
            try {
                Page<GetPersonalDriverChangeRequestDTO> personalPage =
                        adminRepository.getPendingPersonalRequests(0, 1);
                personalTotal = personalPage.getTotalElements();
                mainHandler.post(() -> btnPersonalTab.setText(
                        String.format(Locale.ENGLISH, "Personal (%d)", personalTotal)));
            } catch (Exception ignored) {}

            try {
                Page<GetDriverVehicleChangeRequestDTO> vehiclePage =
                        adminRepository.getPendingVehicleRequests(0, 1);
                vehicleTotal = vehiclePage.getTotalElements();
                mainHandler.post(() -> btnVehicleTab.setText(
                        String.format(Locale.ENGLISH, "Vehicle (%d)", vehicleTotal)));
            } catch (Exception ignored) {}

            try {
                Page<GetDriverAvatarChangeRequestDTO> avatarPage =
                        adminRepository.getPendingAvatarRequests(0, 1);
                avatarTotal = avatarPage.getTotalElements();
                mainHandler.post(() -> btnAvatarTab.setText(
                        String.format(Locale.ENGLISH, "Avatar (%d)", avatarTotal)));
            } catch (Exception ignored) {}
        });
    }

    private void loadRequests() {
        showLoading(true);

        executor.execute(() -> {
            try {
                List<Object> requests;
                switch (activeTab) {
                    case "personal": {
                        Page<GetPersonalDriverChangeRequestDTO> page =
                                adminRepository.getPendingPersonalRequests(currentPage, itemsPerPage);
                        personalTotal = page.getTotalElements();
                        personalTotalPages = page.getTotalPages();
                        requests = new ArrayList<>(page.getContent());
                        break;
                    }
                    case "vehicle": {
                        Page<GetDriverVehicleChangeRequestDTO> page =
                                adminRepository.getPendingVehicleRequests(currentPage, itemsPerPage);
                        vehicleTotal = page.getTotalElements();
                        vehicleTotalPages = page.getTotalPages();
                        requests = new ArrayList<>(page.getContent());
                        break;
                    }
                    case "avatar": {
                        Page<GetDriverAvatarChangeRequestDTO> page =
                                adminRepository.getPendingAvatarRequests(currentPage, itemsPerPage);
                        avatarTotal = page.getTotalElements();
                        avatarTotalPages = page.getTotalPages();
                        requests = new ArrayList<>(page.getContent());
                        break;
                    }
                    default:
                        requests = new ArrayList<>();
                }

                String tab = activeTab;
                mainHandler.post(() -> {
                    adapter.setRequests(requests, tab);
                    updatePaginationUI();
                    showLoading(false);
                    showEmpty(requests.isEmpty());
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    showToast("Failed to load requests: " + e.getMessage());
                });
            }
        });
    }

    private void updatePaginationUI() {
        int currentTotal = getCurrentTotal();
        int currentTotalPages = getCurrentTotalPages();

        if (currentTotal == 0) {
            tvPaginationInfo.setText("No requests");
            btnPrevious.setEnabled(false);
            btnNext.setEnabled(false);
            return;
        }

        int startIndex = currentPage * itemsPerPage + 1;
        int endIndex = Math.min((currentPage + 1) * itemsPerPage, currentTotal);

        tvPaginationInfo.setText(String.format(Locale.ENGLISH, "Showing %d-%d of %d request%s",
                startIndex, endIndex, currentTotal, currentTotal != 1 ? "s" : ""));

        btnPrevious.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < currentTotalPages - 1);
    }

    private int getCurrentTotal() {
        switch (activeTab) {
            case "personal": return personalTotal;
            case "vehicle": return vehicleTotal;
            case "avatar": return avatarTotal;
            default: return 0;
        }
    }

    private int getCurrentTotalPages() {
        switch (activeTab) {
            case "personal": return personalTotalPages;
            case "vehicle": return vehicleTotalPages;
            case "avatar": return avatarTotalPages;
            default: return 0;
        }
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadRequests();
        }
    }

    private void nextPage() {
        if (currentPage < getCurrentTotalPages() - 1) {
            currentPage++;
            loadRequests();
        }
    }

    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        tvPaginationInfo.setVisibility(show ? View.GONE : View.VISIBLE);
        btnPrevious.setVisibility(show ? View.GONE : View.VISIBLE);
        btnNext.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        tvPaginationInfo.setVisibility(show ? View.GONE : View.VISIBLE);
        btnPrevious.setVisibility(show ? View.GONE : View.VISIBLE);
        btnNext.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showApproveAlert(Long requestId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Approve Request")
                .setMessage("Are you sure you want to approve this request?")
                .setPositiveButton("Approve", (dialog, which) -> approve(requestId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approve(Long requestId) {
        executor.execute(() -> {
            try {
                switch (activeTab) {
                    case "personal":
                        adminRepository.approvePersonalRequest(requestId);
                        break;
                    case "vehicle":
                        adminRepository.approveVehicleRequest(requestId);
                        break;
                    case "avatar":
                        adminRepository.approveAvatarRequest(requestId);
                        break;
                }
                mainHandler.post(() -> {
                    showToast("Request approved successfully!");
                    loadAllTabCounts();
                    loadRequests();
                });
            } catch (Exception e) {
                mainHandler.post(() -> showToast("Failed to approve request"));
            }
        });
    }

    private void showRejectAlert(Long requestId) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reject_reason, null);
        TextInputEditText etReason = dialogView.findViewById(R.id.etReason);

        new AlertDialog.Builder(requireContext())
                .setTitle("Reject Request")
                .setView(dialogView)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String reason = String.valueOf(etReason.getText()).trim();
                    if (reason.isEmpty()) {
                        showToast("Rejection reason is required");
                    } else {
                        reject(requestId, reason);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reject(Long requestId, String reason) {
        executor.execute(() -> {
            try {
                switch (activeTab) {
                    case "personal":
                        adminRepository.rejectPersonalRequest(requestId, reason);
                        break;
                    case "vehicle":
                        adminRepository.rejectVehicleRequest(requestId, reason);
                        break;
                    case "avatar":
                        adminRepository.rejectAvatarRequest(requestId, reason);
                        break;
                }
                mainHandler.post(() -> {
                    showToast("Request rejected successfully!");
                    loadAllTabCounts();
                    loadRequests();
                });
            } catch (Exception e) {
                mainHandler.post(() -> showToast("Failed to reject request"));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

}