package com.example.getgo.fragments.admins;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
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
import com.example.getgo.utils.MapManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Method;
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

    private GoogleMap mMap;
    private MapManager mapManager;
    private PanicApiService panicApiService;
    private PanicNotificationAdapter notificationAdapter;

    private CardView notificationPanel;
    private ConstraintLayout btnToggleNotifications;
    private TextView tvNotificationBadge;
    private RecyclerView rvNotifications;
    private TextView tvEmptyNotifications;
    private Button btnMarkAllRead;

    private boolean isPanelOpen = false;
    private Set<Long> panicDriverIds = new HashSet<>();
    private Map<Long, Marker> rideMarkers = new HashMap<>();

    private Map<Long, Marker> panicOverlays = new HashMap<>();
    private BitmapDescriptor panicIconDescriptor = null;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    private MediaPlayer panicSound;
    private Set<Long> lastPanicIds = new HashSet<>();

    // new: prefs keys
    private static final String PREFS_NAME = "admin_panic_prefs";
    private static final String KEY_PANIC_IDS = "saved_panic_ids";
    private SharedPreferences prefs;

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

        // init prefs and load saved panic IDs so badge persists across exits
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadSavedPanicIdsFromPrefs();

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
                if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomIn());
            });
        }
        if (zoomOut != null) {
            zoomOut.setOnClickListener(v -> {
                if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomOut());
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mapManager = new MapManager(requireContext(), mMap);

        LatLng noviSad = new LatLng(45.2519, 19.8370);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noviSad, 12f));

        loadActiveDrivers();
    }

    private void setupNotificationPanel() {
        notificationAdapter = new PanicNotificationAdapter(this::onNotificationClick);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(notificationAdapter);

        btnToggleNotifications.setOnClickListener(v -> toggleNotificationPanel());

        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());

        notificationPanel.post(() -> {
            notificationPanel.setTranslationX(notificationPanel.getWidth());
        });
    }

    private void toggleNotificationPanel() {
        isPanelOpen = !isPanelOpen;

        float targetTranslation = isPanelOpen
                ? 40f
                : notificationPanel.getWidth();

        notificationPanel.animate()
                .translationX(targetTranslation)
                .setDuration(300)
                .start();
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

    private void loadSavedPanicIdsFromPrefs() {
        try {
            Set<String> saved = prefs.getStringSet(KEY_PANIC_IDS, null);
            lastPanicIds.clear();
            if (saved != null) {
                for (String s : saved) {
                    try { lastPanicIds.add(Long.parseLong(s)); } catch (Exception ignored) {}
                }
            }
            // update UI badge based on saved set
            requireActivity().runOnUiThread(() -> {
                int count = lastPanicIds.size();
                if (tvNotificationBadge != null) {
                    if (count > 0) {
                        tvNotificationBadge.setText(String.valueOf(count));
                        tvNotificationBadge.setVisibility(View.VISIBLE);
                    } else {
                        tvNotificationBadge.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("AdminHome", "Failed to load saved panic ids", e);
        }
    }

    private void savePanicIdsToPrefs() {
        try {
            Set<String> asStrings = new HashSet<>();
            for (Long id : lastPanicIds) asStrings.add(String.valueOf(id));
            prefs.edit().putStringSet(KEY_PANIC_IDS, asStrings).apply();
        } catch (Exception e) {
            Log.e("AdminHome", "Failed to save panic ids", e);
        }
    }

    private void updateNotifications(List<PanicAlertDTO> panics) {
        notificationAdapter.setNotifications(panics);

        // collect panic IDs for badge and new detection
        Set<Long> newPanicIds = new HashSet<>();
        for (PanicAlertDTO panic : panics) {
            try {
                newPanicIds.add(panic.getPanicId());
            } catch (Exception ignored) {}
        }

        boolean hasNewPanic = false;
        for (Long id : newPanicIds) {
            if (!lastPanicIds.contains(id)) {
                hasNewPanic = true;
                break;
            }
        }

        if (panics.size() > 0) {
            tvNotificationBadge.setText(String.valueOf(panics.size()));
            tvNotificationBadge.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.VISIBLE);
            tvEmptyNotifications.setVisibility(View.GONE);
            if (hasNewPanic) playPanicSound();
        } else {
            tvNotificationBadge.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.GONE);
            tvEmptyNotifications.setVisibility(View.VISIBLE);
        }

        lastPanicIds.clear();
        lastPanicIds.addAll(newPanicIds);

        // persist the seen/unread ids so they survive exit
        savePanicIdsToPrefs();

        // Resolve driver IDs for panics on background thread:
        new Thread(() -> {
            try {
                // fetch current active drivers
                DriverRepository repo = DriverRepository.getInstance();
                List<GetActiveDriverLocationDTO> drivers = repo.getActiveDriverLocations();

                // build lookup structures: by driverId and by rideId (if driver DTO exposes rideId)
                Map<Long, GetActiveDriverLocationDTO> driversById = new HashMap<>();
                Map<Long, Long> rideToDriver = new HashMap<>();
                for (GetActiveDriverLocationDTO d : drivers) {
                    try { driversById.put(d.getDriverId(), d); } catch (Exception ignored) {}
                    // try to map rideId -> driverId if driver DTO exposes rideId
                    try {
                        Method m = d.getClass().getMethod("getRideId");
                        Object val = m.invoke(d);
                        if (val instanceof Number) {
                            Long rideId = ((Number) val).longValue();
                            try { rideToDriver.put(rideId, d.getDriverId()); } catch (Exception ignored) {}
                        }
                    } catch (Exception ignored) {}
                }

                Set<Long> resolvedDriverIds = new HashSet<>();

                for (PanicAlertDTO panic : panics) {
                    Long resolvedDriver = null;

                    // 1) try panic.getDriverId() via reflection-safe helper
                    resolvedDriver = safeGetLong(panic, "getDriverId");
                    if (resolvedDriver != null) {
                        resolvedDriverIds.add(resolvedDriver);
                        continue;
                    }

                    // 2) try panic.getRideId() -> map to driver via rideToDriver
                    Long panicRideId = safeGetLong(panic, "getRideId");
                    if (panicRideId != null) {
                        Long fromMap = rideToDriver.get(panicRideId);
                        if (fromMap != null) {
                            resolvedDriverIds.add(fromMap);
                            continue;
                        }
                    }

                    // 3) try coordinates on panic and find nearest active driver (within threshold)
                    Double panicLat = safeGetDouble(panic, "getLatitude");
                    Double panicLng = safeGetDouble(panic, "getLongitude");
                    if (panicLat != null && panicLng != null && !drivers.isEmpty()) {
                        Long nearest = findNearestDriverId(panicLat, panicLng, drivers, 200.0); // meters threshold
                        if (nearest != null) {
                            resolvedDriverIds.add(nearest);
                            continue;
                        }
                    }

                    // 4) fallback: skip if we can't resolve
                }

                // update panicDriverIds and overlays on main thread
                Set<Long> finalResolved = resolvedDriverIds;
                requireActivity().runOnUiThread(() -> {
                    panicDriverIds.clear();
                    panicDriverIds.addAll(finalResolved);
                    // refresh overlays (will use panicDriverIds)
                    loadActiveDrivers();
                });

            } catch (Exception e) {
                Log.e("AdminHome", "Failed to resolve panic -> driver mapping", e);
            }
        }).start();
    }

    // reflection-safe getters: return Long or Double or null if method missing / null
    private Long safeGetLong(Object obj, String methodName) {
        try {
            Method m = obj.getClass().getMethod(methodName);
            Object v = m.invoke(obj);
            if (v == null) return null;
            if (v instanceof Number) return ((Number) v).longValue();
            if (v instanceof String) {
                try { return Long.parseLong((String) v); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Double safeGetDouble(Object obj, String methodName) {
        try {
            Method m = obj.getClass().getMethod(methodName);
            Object v = m.invoke(obj);
            if (v == null) return null;
            if (v instanceof Number) return ((Number) v).doubleValue();
            if (v instanceof String) {
                try { return Double.parseDouble((String) v); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    // find nearest driver by lat/lng within thresholdMeters
    private Long findNearestDriverId(double lat, double lng, List<GetActiveDriverLocationDTO> drivers, double thresholdMeters) {
        double bestDist = Double.MAX_VALUE;
        Long bestDriver = null;
        for (GetActiveDriverLocationDTO d : drivers) {
            try {
                double dlat = d.getLatitude();
                double dlng = d.getLongitude();
                double dist = haversineMeters(lat, lng, dlat, dlng);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestDriver = d.getDriverId();
                }
            } catch (Exception ignored) {}
        }
        return (bestDist <= thresholdMeters) ? bestDriver : null;
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private void updatePanicMarkersOnMap(List<PanicAlertDTO> panics) {
        loadActiveDrivers();
    }

    private void loadActiveDrivers() {
        new Thread(() -> {
            try {
                DriverRepository repo = DriverRepository.getInstance();
                List<GetActiveDriverLocationDTO> drivers = repo.getActiveDriverLocations();

                requireActivity().runOnUiThread(() -> {
                    mapManager.updateDriverLocations(drivers);
                    Log.d("PassengerHome", "Loaded " + drivers.size() + " active drivers");

                    try {
                        ensurePanicIconDescriptor(requireContext());
                        Set<Long> seenDriverIds = new HashSet<>();
                        for (GetActiveDriverLocationDTO d : drivers) {
                            Long driverId = null;
                            try {
                                driverId = d.getDriverId();
                            } catch (Exception ignored) {}
                            double lat = 0, lng = 0;
                            try {
                                lat = d.getLatitude();
                                lng = d.getLongitude();
                            } catch (Exception ignored) {}

                            if (driverId != null) {
                                seenDriverIds.add(driverId);
                                LatLng pos = new LatLng(lat, lng);

                                if (panicDriverIds.contains(driverId)) {
                                    // compute small offset (meters) to place overlay at upper-right of car
                                    LatLng overlayPos = offsetLatLng(pos, /*northMeters=*/4.5, /*eastMeters=*/4.5);

                                    Marker overlay = panicOverlays.get(driverId);
                                    if (overlay == null) {
                                        MarkerOptions mo = new MarkerOptions()
                                                .position(overlayPos)
                                                .anchor(0.5f, 0.5f) // keep icon centered on overlayPos
                                                .zIndex(10f)
                                                .icon(panicIconDescriptor);
                                        Marker m = mMap.addMarker(mo);
                                        panicOverlays.put(driverId, m);
                                    } else {
                                        overlay.setPosition(overlayPos);
                                        overlay.setIcon(panicIconDescriptor);
                                    }
                                } else {
                                    Marker overlay = panicOverlays.remove(driverId);
                                    if (overlay != null) overlay.remove();
                                }
                            }
                        }

                        List<Long> toRemove = new ArrayList<>();
                        for (Long did : new ArrayList<>(panicOverlays.keySet())) {
                            if (!seenDriverIds.contains(did)) {
                                toRemove.add(did);
                            }
                        }
                        for (Long did : toRemove) {
                            Marker overlay = panicOverlays.remove(did);
                            if (overlay != null) overlay.remove();
                        }
                    } catch (Exception e) {
                        Log.e("AdminHome", "Failed to update panic overlays", e);
                    }
                });
            } catch (Exception e) {
                Log.e("PassengerHome", "Failed to load active drivers", e);
            }
        }).start();
    }

    // helper: offset a LatLng by meters north and east
    private LatLng offsetLatLng(LatLng origin, double metersNorth, double metersEast) {
        double lat = origin.latitude;
        double lng = origin.longitude;
        double dLat = metersNorth / 111111.0; // approx degrees per meter latitude
        double dLng = metersEast / (111111.0 * Math.cos(Math.toRadians(lat))); // approx degrees per meter longitude
        return new LatLng(lat + dLat, lng + dLng);
    }

    private void onNotificationClick(PanicAlertDTO notification) {
        panicApiService.markPanicAsRead(notification.getPanicId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // remove from UI list / badge immediately
                            notificationAdapter.removeNotification(notification.getPanicId());
                            // remove from saved ids and persist
                            lastPanicIds.remove(notification.getPanicId());
                            savePanicIdsToPrefs();
                            updateBadgeCount();

                            // resolve & remove overlay (existing logic)
                            // ...existing background resolution code...
                            // after removing overlay we refresh list
                            loadUnreadPanics();
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

    private void focusOnDriver(Long driverId) {
        Marker m = panicOverlays.get(driverId);
        if (m == null) {
            for (Marker marker : rideMarkers.values()) {
                if (marker != null && marker.getTag() != null && driverId.equals(marker.getTag())) {
                    m = marker;
                    break;
                }
            }
        }
        if (m != null && mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 15f));
            m.showInfoWindow();
        }
    }

    private void markAllAsRead() {
        panicApiService.markAllPanicsAsRead().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    notificationAdapter.clearAll();
                    panicDriverIds.clear();
                    lastPanicIds.clear(); // clear saved set
                    savePanicIdsToPrefs();
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
        // prefer persisted count if adapter empty (keeps badge when leaving)
        if (count <= 0) {
            count = lastPanicIds.size();
        }

        if (count > 0) {
            tvNotificationBadge.setText(String.valueOf(count));
            tvNotificationBadge.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.VISIBLE);
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

    private void ensurePanicIconDescriptor(Context ctx) {
        if (panicIconDescriptor != null) return;
        Bitmap bmp = getBitmapFromVector(ctx, R.drawable.ic_panic_overlay);
        if (bmp != null) {
            panicIconDescriptor = BitmapDescriptorFactory.fromBitmap(bmp);
        }
    }

    private Bitmap getBitmapFromVector(Context context, int drawableId) {
        try {
            Drawable drawable = ContextCompat.getDrawable(context, drawableId);
            if (drawable == null) return null;

            int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 48;
            int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 48;
            drawable.setBounds(0, 0, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            Log.e("AdminHome", "Failed to convert vector to bitmap", e);
            return null;
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
        try {
            for (Marker m : panicOverlays.values()) {
                if (m != null) m.remove();
            }
            panicOverlays.clear();
        } catch (Exception ignored) {}
    }
}
