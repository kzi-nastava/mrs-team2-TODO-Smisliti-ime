package com.example.getgo.fragments.admins;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.dtos.user.BlockUserResponseDTO;
import com.example.getgo.dtos.user.UserEmailDTO;
import com.example.getgo.dtos.general.Page;
import com.example.getgo.repositories.AdminRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminBlockUsersFragment extends Fragment {

    private TextInputEditText etUnblockedSearch, etBlockedSearch, etBlockReason;
    private MaterialButton btnSearchUnblocked, btnSearchBlocked;
    private MaterialButton btnUnblockedPrev, btnUnblockedNext, btnBlockedPrev, btnBlockedNext;
    private MaterialButton btnBlockUser, btnUnblockUser;
    private LinearLayout layoutUnblockedUsers, layoutBlockedUsers;
    private TextView tvNoUnblockedUsers, tvNoBlockedUsers;
    private TextView tvUnblockedPage, tvBlockedPage;
    private MaterialCardView cvSuccessMessage, cvErrorMessage;
    private TextView tvSuccessMessage, tvErrorMessage;

    private AdminRepository adminRepository;
    private ExecutorService executor;
    private Handler mainHandler;

    private int unblockedPage = 0;
    private int unblockedTotalPages = 0;
    private int blockedPage = 0;
    private int blockedTotalPages = 0;
    private static final int PAGE_SIZE = 3;

    private UserEmailDTO selectedUserToBlock = null;
    private UserEmailDTO selectedUserToUnblock = null;

    private View selectedBlockView = null;
    private View selectedUnblockView = null;

    public AdminBlockUsersFragment() {}

    public static AdminBlockUsersFragment newInstance() {
        return new AdminBlockUsersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_block_users, container, false);

        adminRepository = AdminRepository.getInstance();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initializeViews(view);
        setupListeners();
        loadUnblockedUsers();
        loadBlockedUsers();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void initializeViews(View view) {
        etUnblockedSearch = view.findViewById(R.id.etUnblockedSearch);
        etBlockedSearch = view.findViewById(R.id.etBlockedSearch);
        etBlockReason = view.findViewById(R.id.etBlockReason);

        btnSearchUnblocked = view.findViewById(R.id.btnSearchUnblocked);
        btnSearchBlocked = view.findViewById(R.id.btnSearchBlocked);
        btnBlockUser = view.findViewById(R.id.btnBlockUser);
        btnUnblockUser = view.findViewById(R.id.btnUnblockUser);

        btnUnblockedPrev = view.findViewById(R.id.btnUnblockedPrev);
        btnUnblockedNext = view.findViewById(R.id.btnUnblockedNext);
        btnBlockedPrev = view.findViewById(R.id.btnBlockedPrev);
        btnBlockedNext = view.findViewById(R.id.btnBlockedNext);

        layoutUnblockedUsers = view.findViewById(R.id.layoutUnblockedUsers);
        layoutBlockedUsers = view.findViewById(R.id.layoutBlockedUsers);

        tvNoUnblockedUsers = view.findViewById(R.id.tvNoUnblockedUsers);
        tvNoBlockedUsers = view.findViewById(R.id.tvNoBlockedUsers);
        tvUnblockedPage = view.findViewById(R.id.tvUnblockedPage);
        tvBlockedPage = view.findViewById(R.id.tvBlockedPage);

        cvSuccessMessage = view.findViewById(R.id.cvSuccessMessage);
        cvErrorMessage = view.findViewById(R.id.cvErrorMessage);
        tvSuccessMessage = view.findViewById(R.id.tvSuccessMessage);
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
    }

    private void setupListeners() {
        btnSearchUnblocked.setOnClickListener(v -> {
            unblockedPage = 0;
            selectedUserToBlock = null;
            selectedBlockView = null;
            loadUnblockedUsers();
        });

        btnSearchBlocked.setOnClickListener(v -> {
            blockedPage = 0;
            selectedUserToUnblock = null;
            selectedUnblockView = null;
            loadBlockedUsers();
        });

        btnUnblockedPrev.setOnClickListener(v -> {
            if (unblockedPage > 0) {
                unblockedPage--;
                selectedUserToBlock = null;
                selectedBlockView = null;
                loadUnblockedUsers();
            }
        });

        btnUnblockedNext.setOnClickListener(v -> {
            if (unblockedPage < unblockedTotalPages - 1) {
                unblockedPage++;
                selectedUserToBlock = null;
                selectedBlockView = null;
                loadUnblockedUsers();
            }
        });

        btnBlockedPrev.setOnClickListener(v -> {
            if (blockedPage > 0) {
                blockedPage--;
                selectedUserToUnblock = null;
                selectedUnblockView = null;
                loadBlockedUsers();
            }
        });

        btnBlockedNext.setOnClickListener(v -> {
            if (blockedPage < blockedTotalPages - 1) {
                blockedPage++;
                selectedUserToUnblock = null;
                selectedUnblockView = null;
                loadBlockedUsers();
            }
        });

        btnBlockUser.setOnClickListener(v -> blockUser());
        btnUnblockUser.setOnClickListener(v -> unblockUser());
    }

    private void loadUnblockedUsers() {
        String search = etUnblockedSearch.getText() != null
                ? etUnblockedSearch.getText().toString().trim() : "";

        executor.execute(() -> {
            try {
                Page<UserEmailDTO> page = adminRepository.getUnblockedUsers(search, unblockedPage, PAGE_SIZE);

                mainHandler.post(() -> {
                    unblockedTotalPages = page.getTotalPages();
                    populateUserTable(layoutUnblockedUsers, tvNoUnblockedUsers, page, true);
                    tvUnblockedPage.setText(String.format(Locale.ENGLISH,
                            "Page %d / %d", unblockedPage + 1, Math.max(unblockedTotalPages, 1)));
                    btnUnblockedPrev.setEnabled(unblockedPage > 0);
                    btnUnblockedNext.setEnabled(unblockedPage < unblockedTotalPages - 1);
                });
            } catch (Exception e) {
                mainHandler.post(() -> showError("Failed to load users"));
            }
        });
    }

    private void loadBlockedUsers() {
        String search = etBlockedSearch.getText() != null
                ? etBlockedSearch.getText().toString().trim() : "";

        executor.execute(() -> {
            try {
                Page<UserEmailDTO> page = adminRepository.getBlockedUsers(search, blockedPage, PAGE_SIZE);

                mainHandler.post(() -> {
                    blockedTotalPages = page.getTotalPages();
                    populateUserTable(layoutBlockedUsers, tvNoBlockedUsers, page, false);
                    tvBlockedPage.setText(String.format(Locale.ENGLISH,
                            "Page %d / %d", blockedPage + 1, Math.max(blockedTotalPages, 1)));
                    btnBlockedPrev.setEnabled(blockedPage > 0);
                    btnBlockedNext.setEnabled(blockedPage < blockedTotalPages - 1);
                });
            } catch (Exception e) {
                mainHandler.post(() -> showError("Failed to load blocked users"));
            }
        });
    }

    private void populateUserTable(LinearLayout layout, TextView emptyView,
                                   Page<UserEmailDTO> page, boolean isBlockTable) {
        layout.removeAllViews();

        if (page.getContent() == null || page.getContent().isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        emptyView.setVisibility(View.GONE);

        for (UserEmailDTO user : page.getContent()) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(32, 24, 32, 24);
            row.setClickable(true);
            row.setFocusable(true);
            row.setBackgroundColor(Color.WHITE);

            TextView tvEmail = new TextView(requireContext());
            tvEmail.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 3));
            tvEmail.setText(user.getEmail());
            tvEmail.setTextColor(Color.parseColor("#1E4A7F"));
            tvEmail.setTextSize(14);

            TextView tvRole = new TextView(requireContext());
            tvRole.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            tvRole.setText(user.getRole());
            tvRole.setTextColor(Color.parseColor("#1E4A7F"));
            tvRole.setTextSize(14);

            row.addView(tvEmail);
            row.addView(tvRole);

            row.setOnClickListener(v -> {
                if (isBlockTable) {
                    if (selectedBlockView != null) {
                        selectedBlockView.setBackgroundColor(Color.WHITE);
                    }
                    row.setBackgroundColor(Color.parseColor("#B0C8E8"));
                    selectedBlockView = row;
                    selectedUserToBlock = user;
                } else {
                    if (selectedUnblockView != null) {
                        selectedUnblockView.setBackgroundColor(Color.WHITE);
                    }
                    row.setBackgroundColor(Color.parseColor("#B0C8E8"));
                    selectedUnblockView = row;
                    selectedUserToUnblock = user;
                }
            });

            View divider = new View(requireContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.parseColor("#CBDCEB"));

            layout.addView(row);
            layout.addView(divider);
        }
    }

    private void blockUser() {
        if (selectedUserToBlock == null) return;

        String reason = etBlockReason.getText() != null
                ? etBlockReason.getText().toString().trim() : "";
        if (reason.isEmpty()) return;

        btnBlockUser.setEnabled(false);

        executor.execute(() -> {
            try {
                BlockUserResponseDTO response = adminRepository.blockUser(
                        selectedUserToBlock.getId(), reason);

                mainHandler.post(() -> {
                    btnBlockUser.setEnabled(true);
                    showSuccess("Blocked " + response.getEmail());
                    selectedUserToBlock = null;
                    selectedBlockView = null;
                    etBlockReason.setText("");
                    loadUnblockedUsers();
                    loadBlockedUsers();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    btnBlockUser.setEnabled(true);
                    showError("Failed to block user");
                });
            }
        });
    }

    private void unblockUser() {
        if (selectedUserToUnblock == null) return;

        btnUnblockUser.setEnabled(false);

        executor.execute(() -> {
            try {
                BlockUserResponseDTO response = adminRepository.unblockUser(
                        selectedUserToUnblock.getId());

                mainHandler.post(() -> {
                    btnUnblockUser.setEnabled(true);
                    showSuccess("Unblocked " + response.getEmail());
                    selectedUserToUnblock = null;
                    selectedUnblockView = null;
                    loadUnblockedUsers();
                    loadBlockedUsers();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    btnUnblockUser.setEnabled(true);
                    showError("Failed to unblock user");
                });
            }
        });
    }

    private void showSuccess(String message) {
        cvErrorMessage.setVisibility(View.GONE);
        tvSuccessMessage.setText(message);
        cvSuccessMessage.setVisibility(View.VISIBLE);

        mainHandler.postDelayed(() -> cvSuccessMessage.setVisibility(View.GONE), 3000);
    }

    private void showError(String message) {
        cvSuccessMessage.setVisibility(View.GONE);
        tvErrorMessage.setText(message);
        cvErrorMessage.setVisibility(View.VISIBLE);

        mainHandler.postDelayed(() -> cvErrorMessage.setVisibility(View.GONE), 3000);
    }
}