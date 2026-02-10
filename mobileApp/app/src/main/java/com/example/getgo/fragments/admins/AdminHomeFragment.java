package com.example.getgo.fragments.admins;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getgo.R;
import com.example.getgo.adapters.PanicNotificationAdapter;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.PanicApiService;
import com.example.getgo.dtos.driver.GetActiveDriverLocationDTO;
import com.example.getgo.dtos.panic.PanicAlertDTO;
import com.example.getgo.repositories.DriverRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private PanicApiService panicApiService;
    private PanicNotificationAdapter notificationAdapter;

    private CardView notificationPanel;
    private ConstraintLayout btnToggleNotifications;
    private TextView tvNotificationBadge;
    private RecyclerView rvNotifications;
    private TextView tvEmptyNotifications;
    private Button btnMarkAllRead;

    private boolean isPanelOpen = false;
    private Set<Long> panicRideIds = new HashSet<>();
    private Map<Long, Marker> rideMarkers = new HashMap<>();

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    private MediaPlayer panicSound;
    private Set<Long> lastPanicIds = new HashSet<>();

    public AdminHomeFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        panicApiService = ApiClient.getPanicApiService();

        initializeViews(view);
        setupMap();
        setupNotificationPanel();
        loadUnreadPanics();
        startPeriodicRefresh();
    }

    private void initializeViews(View view) {
        notificationPanel = view.findViewById(R.id.notificationPanel);
        btnToggleNotifications = view.findViewById(R.id.btnToggleNotifications);
        tvNotificationBadge = view.findViewById(R.id.tvNotificationBadge);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvEmptyNotifications = view.findViewById(R.id.tvEmptyNotifications);
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);

        Button zoomIn = view.findViewById(R.id.btn_zoom_in);
        Button zoomOut = view.findViewById(R.id.btn_zoom_out);

        if (zoomIn != null) {
            zoomIn.setOnClickListener(v -> {
                if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            });
        }
        if (zoomOut != null) {
            zoomOut.setOnClickListener(v -> {
                if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomOut());
            });
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        UiSettings s = googleMap.getUiSettings();
        s.setZoomControlsEnabled(false);
        LatLng defaultPos = new LatLng(45.248, 19.842);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 12f));

        loadActiveDrivers();
    }

    private void setupNotificationPanel() {
        notificationAdapter = new PanicNotificationAdapter(this::onNotificationClick);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(notificationAdapter);

        btnToggleNotifications.setOnClickListener(v -> toggleNotificationPanel());

        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void toggleNotificationPanel() {
        isPanelOpen = !isPanelOpen;

        float targetTranslation = isPanelOpen ? -300f : 0f;

        ObjectAnimator animator = ObjectAnimator.ofFloat(
                notificationPanel,
                "translationX",
                targetTranslation
        );
        animator.setDuration(300);
        animator.start();
    }

    private void loadUnreadPanics() {
        panicApiService.getUnreadPanics().enqueue(new Callback<List<PanicAlertDTO>>() {
            @Override
            public void onResponse(Call<List<PanicAlertDTO>> call, Response<List<PanicAlertDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PanicAlertDTO> panics = response.body();
                    updateNotifications(panics);
                    updatePanicMarkersOnMap(panics);
                }
            }

            @Override
            public void onFailure(Call<List<PanicAlertDTO>> call, Throwable t) {
                Log.e("AdminHome", "Failed to load panics", t);
            }
        });
    }

    private void updateNotifications(List<PanicAlertDTO> panics) {
        notificationAdapter.setNotifications(panics);

        int count = panics.size();

        Set<Long> newPanicIds = new HashSet<>();
        for (PanicAlertDTO panic : panics) {
            newPanicIds.add(panic.getPanicId());
        }

        boolean hasNewPanic = false;
        for (Long id : newPanicIds) {
            if (!lastPanicIds.contains(id)) {
                hasNewPanic = true;
                break;
            }
        }

        if (count > 0) {
            tvNotificationBadge.setText(String.valueOf(count));
            tvNotificationBadge.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.VISIBLE);
            tvEmptyNotifications.setVisibility(View.GONE);

            if (hasNewPanic) {
                playPanicSound();
            }
        } else {
            tvNotificationBadge.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.GONE);
            tvEmptyNotifications.setVisibility(View.VISIBLE);
        }

        lastPanicIds.clear();
        lastPanicIds.addAll(newPanicIds);

        panicRideIds.clear();
        for (PanicAlertDTO panic : panics) {
            panicRideIds.add(panic.getRideId());
        }
    }

    private void updatePanicMarkersOnMap(List<PanicAlertDTO> panics) {
        // Remove panic markers for rides that are no longer in panic
        List<Long> currentPanicRides = new ArrayList<>();
        for (PanicAlertDTO panic : panics) {
            currentPanicRides.add(panic.getRideId());
        }

        // Reload drivers to update markers
        loadActiveDrivers();
    }

    private void loadActiveDrivers() {
        new Thread(() -> {
            try {
                DriverRepository repo = DriverRepository.getInstance();
                List<GetActiveDriverLocationDTO> drivers = repo.getActiveDriverLocations();

                requireActivity().runOnUiThread(() -> {
                    showDriversOnMap(drivers);
                    Log.d("AdminHome", "Loaded " + drivers.size() + " active drivers");
                });
            } catch (Exception e) {
                Log.e("AdminHome", "Failed to load active drivers", e);
            }
        }).start();
    }

    private void showDriversOnMap(List<GetActiveDriverLocationDTO> drivers) {
        if (googleMap == null) return;

        googleMap.clear();

        for (GetActiveDriverLocationDTO d : drivers) {
            if (d.getLatitude() == null || d.getLongitude() == null) continue;

            LatLng position = new LatLng(d.getLatitude(), d.getLongitude());

            // Driver is in panic if NOT available AND there exists any unread panic
            boolean hasPanic = !d.getIsAvailable() && !panicRideIds.isEmpty();

            String title = d.getVehicleType();
            String snippet = "Status: " + (d.getIsAvailable() ? "Free" : "Occupied");

            if (hasPanic) {
                snippet += " - 🚨 PANIC!";
            }

            BitmapDescriptor icon;
            if (hasPanic) {
                icon = createPanicMarkerIcon();
            } else {
                icon = bitmapDescriptorFromVector(
                        requireContext(),
                        d.getIsAvailable()
                                ? R.drawable.ic_car_green
                                : R.drawable.ic_car_red,
                        120,
                        120
                );
            }

            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .icon(icon));
        }
    }

    private BitmapDescriptor createPanicMarkerIcon() {
        // Create a red circle with white "P"
        Context context = requireContext();
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_car_red);

        int width = 120;
        int height = 120;

        if (drawable != null) {
            drawable.setBounds(0, 0, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // Draw car icon
            drawable.draw(canvas);

            // Draw red circle with "P" on top
            Drawable panicIcon = ContextCompat.getDrawable(context, R.drawable.ic_panic_marker);
            if (panicIcon != null) {
                int panicSize = 40;
                panicIcon.setBounds(width - panicSize, 0, width, panicSize);
                panicIcon.draw(canvas);
            }

            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(@NonNull Context context,
                                                        int vectorResId,
                                                        int width,
                                                        int height) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
        return BitmapDescriptorFactory.defaultMarker();
    }

    private void onNotificationClick(PanicAlertDTO notification) {
        // Mark as read
        panicApiService.markPanicAsRead(notification.getPanicId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            notificationAdapter.removeNotification(notification.getPanicId());
                            panicRideIds.remove(notification.getRideId());
                            updateBadgeCount();
                            loadActiveDrivers(); // Refresh map

                            // Focus on the ride location
                            focusOnRide(notification.getRideId());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(requireContext(),
                                "Failed to mark as read",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void focusOnRide(Long rideId) {
        Marker marker = rideMarkers.get(rideId);
        if (marker != null && googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    marker.getPosition(), 15f));
            marker.showInfoWindow();
        }
    }

    private void markAllAsRead() {
        panicApiService.markAllPanicsAsRead().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    notificationAdapter.clearAll();
                    panicRideIds.clear();
                    updateBadgeCount();
                    loadActiveDrivers();
                    Toast.makeText(requireContext(),
                            "All panic alerts cleared",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Failed to clear alerts",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBadgeCount() {
        int count = notificationAdapter.getItemCount();
        if (count > 0) {
            tvNotificationBadge.setText(String.valueOf(count));
            tvNotificationBadge.setVisibility(View.VISIBLE);
        } else {
            tvNotificationBadge.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.GONE);
            tvEmptyNotifications.setVisibility(View.VISIBLE);
        }
    }

    private void startPeriodicRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadUnreadPanics();
                loadActiveDrivers();
                handler.postDelayed(this, 10000); // Refresh every 10 seconds
            }
        };
        handler.postDelayed(refreshRunnable, 10000);
    }

    private void playPanicSound() {
        try {
            panicSound = MediaPlayer.create(requireContext(), R.raw.panic_alert);

            if (panicSound != null && !panicSound.isPlaying()) {
                panicSound.start();
            } else {
                // Use system notification sound as alternative
                android.media.RingtoneManager.getRingtone(
                    requireContext(),
                    android.media.RingtoneManager.getDefaultUri(
                        android.media.RingtoneManager.TYPE_NOTIFICATION
                    )
                ).play();
            }
        } catch (Exception e) {
            Log.e("AdminHome", "Failed to play panic sound", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
        if (panicSound != null) {
            panicSound.release();
            panicSound = null;
        }
    }
}
