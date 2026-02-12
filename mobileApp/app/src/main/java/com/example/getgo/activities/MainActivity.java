package com.example.getgo.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.core.view.WindowCompat;

import com.example.getgo.R;
import com.example.getgo.fragments.passengers.PassengerRateDriverVehicleFragment;
import com.example.getgo.fragments.admins.AdminRideHistoryFragment;
import com.example.getgo.fragments.passengers.PassengerRideHistoryFragment;
import com.example.getgo.fragments.passengers.PassengerRideTrackingFragment;
import com.example.getgo.utils.NavigationHelper;
import com.example.getgo.model.UserRole;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.DriverApiService;
import com.example.getgo.api.services.AuthApiService;
import com.example.getgo.api.services.UserApiService;
import com.example.getgo.model.UserProfile;
import com.example.getgo.utils.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.getgo.utils.WebSocketManager;
import com.example.getgo.fragments.layouts.NotificationsFragment;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.getgo.dtos.notification.NotificationDTO;
import com.example.getgo.api.services.NotificationApiService;

public class MainActivity extends AppCompatActivity {
    private UserRole currentUserRole;
    private DrawerLayout drawer;
    private NavigationHelper navigationHelper;
    private SwitchMaterial driverActiveSwitch;
    private TextView tvDriverStatus;
    private View driverStatusLayout;
    private DriverApiService driverApiService;
    private boolean isDriverActive = false; // Cache driver active status
    private UserApiService userApiService;
    private TextView tvUserName;
    private CircleImageView ivUserProfile;
    private WebSocketManager webSocketManager;
    private Long currentUserId = null;
    private static final int REQ_CODE_POST_NOTIFICATIONS = 1001;
    private MediaPlayer notificationSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.createNotificationChannel(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "getgo_notifications",
                    "GetGo Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for ride updates");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // Ensure notification channel exists for system notifications (Android O+)
        createNotificationChannel();
        // Request runtime notification permission on Android 13+
        requestNotificationPermissionIfNeeded();

        initUserRole();
        setupGuestRestrictions();
        setupToolbarAndNavigation();

        // Load user profile for authenticated users
        if (currentUserRole != UserRole.GUEST) {
            loadUserProfile();
            setupNotificationSocket();
        }

        Intent intent = getIntent();
        boolean openRate = intent.getBooleanExtra("OPEN_RATE_FRAGMENT", false);

        if (!openRate) {
            openFragment(navigationHelper.getStartFragment());
        }


        handleIntent(getIntent());
//        if (getIntent() != null && getIntent().getBooleanExtra("OPEN_RIDE_TRACKING_FRAGMENT", false)) {
//            Long rideId = getIntent().getLongExtra("RIDE_ID", -1);
//            if (rideId != -1) {
//                openRideTrackingFragment(rideId);
//            }
//        }

        handleNotificationIntent(intent);

    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        if (intent.getBooleanExtra("OPEN_RIDE_TRACKING_FRAGMENT", false)) {
            long rideId = intent.getLongExtra("RIDE_ID", -1);
            if (rideId != -1 && !isFragmentAlreadyOpen(PassengerRideTrackingFragment.class)) {
                openRideTrackingFragment(rideId);
            }
        }
    }

    private void openRideTrackingFragment(Long rideId) {
        PassengerRideTrackingFragment fragment = PassengerRideTrackingFragment.newInstance(rideId);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void initUserRole() {
        String roleString = getIntent().getStringExtra("USER_ROLE");
        currentUserRole = roleString != null
                ? UserRole.valueOf(roleString)
                : UserRole.GUEST;

        navigationHelper = new NavigationHelper(currentUserRole);
    }

    private UserRole getCurrentUserRole() {
        return currentUserRole;
    }

    private void setupGuestRestrictions() {
        if (currentUserRole != UserRole.GUEST) return;

        View bottom = findViewById(R.id.bottom_nav);
        if (bottom != null) bottom.setVisibility(View.GONE);

        drawer = findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void setupToolbarAndNavigation() {
        FrameLayout toolbarContainer = findViewById(R.id.toolbar_container);
        toolbarContainer.removeAllViews();

        if (currentUserRole == UserRole.GUEST) {

            View toolbarView = getLayoutInflater()
                    .inflate(R.layout.guest_toolbar, toolbarContainer, true);

            Button btnLogin = toolbarView.findViewById(R.id.btnLoginToolbar);
            Button btnRegister = toolbarView.findViewById(R.id.btnRegisterToolbar);

            btnLogin.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));

            btnRegister.setOnClickListener(v ->
                    startActivity(new Intent(this, RegisterActivity.class)));

        } else {
            // Standard toolbar for all authenticated users
            View toolbarView = getLayoutInflater()
                    .inflate(R.layout.standard_toolbar, toolbarContainer, true);

            Toolbar toolbar = toolbarView.findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // Get user profile views
            tvUserName = toolbarView.findViewById(R.id.tvUserName);
            ivUserProfile = toolbarView.findViewById(R.id.ivUserProfile);

            // Get driver status views
            driverStatusLayout = toolbarView.findViewById(R.id.driverStatusLayout);
            driverActiveSwitch = toolbarView.findViewById(R.id.switchDriverActive);
            tvDriverStatus = toolbarView.findViewById(R.id.tvDriverStatus);

            // Show toggle only for drivers
            if (currentUserRole == UserRole.DRIVER) {
                driverStatusLayout.setVisibility(View.VISIBLE);
                setupDriverStatusToggle();
            } else {
                driverStatusLayout.setVisibility(View.GONE);
            }

            drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar,
                    R.string.drawer_open, R.string.drawer_close
            );
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            toggle.getDrawerArrowDrawable()
                    .setColor(getColor(android.R.color.white));

            setupBottomNavigation();
            setupDrawerNavigation();
        }
    }

    private void setupDriverStatusToggle() {
        driverApiService = ApiClient.getDriverApiService();

        // Load current status
        loadDriverStatus();

        // Handle toggle changes
        driverActiveSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return; // Ignore programmatic changes
            updateDriverStatus(isChecked);
        });
    }

    private void loadDriverStatus() {
        driverApiService.getDriverStatus().enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isDriverActive = response.body();
                    driverActiveSwitch.setChecked(isDriverActive);
                    updateStatusText(isDriverActive);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("MainActivity", "Failed to load driver status", t);
            }
        });
    }

    private void updateDriverStatus(boolean isActive) {
        driverActiveSwitch.setEnabled(false);

        driverApiService.updateDriverStatus(isActive).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                driverActiveSwitch.setEnabled(true);

                if (response.isSuccessful()) {
                    isDriverActive = isActive; // Update cached status
                    updateStatusText(isActive);
                    Toast.makeText(MainActivity.this,
                            isActive ? "You are now active" : "You are now inactive",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Revert switch on failure
                    driverActiveSwitch.setChecked(!isActive);
                    Toast.makeText(MainActivity.this,
                            "Failed to update status",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                driverActiveSwitch.setEnabled(true);
                driverActiveSwitch.setChecked(!isActive);
                Toast.makeText(MainActivity.this,
                        "Network error",
                        Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Failed to update driver status", t);
            }
        });
    }

    private void updateStatusText(boolean isActive) {
        tvDriverStatus.setText(isActive ? R.string.driver_active : R.string.driver_inactive);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.getMenu().clear();
        int menuRes = navigationHelper.getBottomNavMenu();
        if (menuRes != 0) {
            bottomNav.inflateMenu(menuRes);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Handle "My Rides" for both passenger and admin
            if (id == R.id.nav_bottom_my_rides) {
                if (currentUserRole == UserRole.PASSENGER) {
                    PassengerRideHistoryFragment.navigateTo(this);
                } else if (currentUserRole == UserRole.ADMIN) {
                    AdminRideHistoryFragment.navigateTo(this);
                } else {
                    // Short, descriptive message
                    Toast.makeText(this, "My Rides not available", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            // Try NavigationHelper for other menu items
            Fragment fragment = navigationHelper.getFragmentForMenuItem(id);
            if (fragment != null) {
                openFragment(fragment);
                return true;
            }

            // Fragment not implemented yet - show toast
            Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    private void setupDrawerNavigation() {
        NavigationView navView = findViewById(R.id.nav_view);
        navView.getMenu().clear();
        navView.inflateMenu(navigationHelper.getDrawerNavMenu());

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Handle logout separately
            if (id == R.id.nav_drawer_logout) {
                handleLogout();
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            // Handle "My Rides" from drawer too
            if (id == R.id.nav_bottom_my_rides) {
                if (currentUserRole == UserRole.PASSENGER) {
                    PassengerRideHistoryFragment.navigateTo(this);
                } else if (currentUserRole == UserRole.ADMIN) {
                    AdminRideHistoryFragment.navigateTo(this);
                } else {
                    Toast.makeText(this, "My Rides not available", Toast.LENGTH_SHORT).show();
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            // Try NavigationHelper for other items
            Fragment fragment = navigationHelper.getFragmentForMenuItem(id);
            if (fragment != null) {
                openFragment(fragment);
            } else {
                // Fragment not implemented yet - show toast
                Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void openFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    private void handleLogout() {
        // If user is a driver, check active status before allowing logout
        if (currentUserRole == UserRole.DRIVER) {
            // First try to use cached status
            if (isDriverActive) {
                Toast.makeText(this,
                        "Cannot logout while active",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Double-check with backend to ensure status is current
            AuthApiService authService = ApiClient.getAuthApiService();
            authService.canLogout().enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        boolean canLogout = response.body();

                        if (canLogout) {
                            performLogout();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Cannot logout while active",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Logout check failed",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Log.e("MainActivity", "Logout check failed", t);
                    Toast.makeText(MainActivity.this,
                            "Network error",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Non-driver users can logout immediately
            performLogout();
        }
    }

    private void performLogout() {
        // Clear JWT token
        getSharedPreferences("getgo_prefs", MODE_PRIVATE)
                .edit()
                .remove("jwt_token")
                .apply();

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent == null) return;

        long rideId = intent.getLongExtra("RIDE_ID", -1);
        long driverId = intent.getLongExtra("driverId", -1);

        // Rating fragment
        if (intent.getBooleanExtra("OPEN_RATE_FRAGMENT", false)
                && rideId != -1
                && !isFragmentAlreadyOpen(PassengerRateDriverVehicleFragment.class)) {

            Bundle bundle = new Bundle();
            bundle.putLong("rideId", rideId);
            bundle.putLong("driverId", driverId);

            PassengerRateDriverVehicleFragment fragment = new PassengerRateDriverVehicleFragment();
            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return;
        }

        // Ride tracking fragment
        if (intent.getBooleanExtra("OPEN_RIDE_TRACKING", false)
                && rideId != -1
                && !isFragmentAlreadyOpen(PassengerRideTrackingFragment.class)) {

            Bundle bundle = new Bundle();
            bundle.putLong("rideId", rideId);

            PassengerRideTrackingFragment fragment = new PassengerRideTrackingFragment();
            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    private boolean isFragmentAlreadyOpen(Class<? extends Fragment> fragmentClass) {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        return current != null && fragmentClass.isInstance(current);
    }

    private void loadUserProfile() {
        userApiService = ApiClient.getUserApiService();

        userApiService.getUserProfile().enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();
                    updateUserProfileUI(profile);

                    // WebSocket subscription handled in setupNotificationSocket()
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Log.e("MainActivity", "Failed to load user profile", t);
            }
        });
    }


    private void updateUserProfileUI(UserProfile profile) {
        if (tvUserName != null) {
            tvUserName.setText(profile.getFullName());
        }

        if (ivUserProfile != null && profile.getProfilePictureUrl() != null && !profile.getProfilePictureUrl().isEmpty()) {
            // Build final image URL the same way as PassengerProfileInfoFragment:
            // if returned URL is absolute, use it; otherwise prefix with ApiClient.SERVER_URL
            String rawUrl = profile.getProfilePictureUrl();
            String imageUrl = rawUrl.startsWith("http") ? rawUrl : ApiClient.SERVER_URL + rawUrl;

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.unregistered_profile) // same placeholder as passenger fragment
                    .error(R.drawable.unregistered_profile)
                    .circleCrop()
                    .into(ivUserProfile);
        }
    }

    private void setupNotificationSocket() {
        // Avoid creating multiple socket connections
        if (webSocketManager != null) return;

        // Try to get user id from prefs first; do NOT fallback to an extra HTTP call.
        SharedPreferences prefs = getSharedPreferences("getgo_prefs", MODE_PRIVATE);
        long uid = prefs.getLong("user_id", -1L);
        Log.d("NOTIF_DEBUG", "setupNotificationSocket: user_id = " + uid); // <- dodaj ovo
        if (uid <= 0) {
            Log.d("NOTIF_DEBUG", "User ID invalid, websocket neće startovati");
            return;
        }
        String jwt = prefs.getString("jwt_token", null);

        // If we already have user id, start socket immediately
        if (uid > 0) {
            startNotificationSocketForUser(uid, jwt);
        } else {
            // No stored user id -> nothing to subscribe right now. The socket will be
            // started on next app launch or after login when prefs are set.
            Log.d("MainActivity", "No user_id in prefs, skipping socket subscription");
        }
    }

    // Start and subscribe socket for a specific user id. Keeps method small and focused.
    private void startNotificationSocketForUser(Long uid, String jwtToken) {
        if (uid == null || uid <= 0) return;
        if (webSocketManager != null) return; // already started

        currentUserId = uid;
        webSocketManager = new WebSocketManager();
        webSocketManager.setNotificationListener(this::onWebSocketNotificationReceived);
        if (jwtToken != null && !jwtToken.isEmpty()) webSocketManager.setAuthToken(jwtToken);

        webSocketManager.setConnectionListener(() -> runOnUiThread(() -> {
            // Bulk unread notifications
            webSocketManager.subscribeToUserNotifications(currentUserId, notifications -> runOnUiThread(() -> {
                if (notifications == null || notifications.isEmpty()) return;

                int unreadCount = 0;
                NotificationApiService notifService = ApiClient.getNotificationApiService(); // local ref

                for (com.example.getgo.dtos.notification.NotificationDTO n : notifications) {
                    if (n == null) continue;
                    if (!n.isRead()) {
                        unreadCount++;
                        String title = n.getTitle() != null ? n.getTitle() : "Notification";
                        String msg = n.getMessage() != null ? n.getMessage() : "";
                        int nid = n.getId() != null ? n.getId().intValue() : (int) (System.currentTimeMillis() & 0x7fffffff);
                        Log.d("NOTIF_FLOW", "Bulk unread -> showing notification id=" + nid + " title='" + title + "' msg='" + msg + "'");
                        showUserNotification(title, msg, nid);

                        // mark notification as viewed on backend so it won't be forwarded again
                        if (n.getId() != null) {
                            notifService.deleteNotification(n.getId()).enqueue(new Callback<com.example.getgo.dtos.notification.NotificationDTO>() {
                                @Override
                                public void onResponse(Call<com.example.getgo.dtos.notification.NotificationDTO> call, Response<com.example.getgo.dtos.notification.NotificationDTO> response) {
                                    if (response.isSuccessful()) {
                                        Log.d("NOTIF_SYNC", "Marked notification read id=" + n.getId());
                                    } else {
                                        Log.w("NOTIF_SYNC", "Failed to mark read id=" + n.getId() + " code=" + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<com.example.getgo.dtos.notification.NotificationDTO> call, Throwable t) {
                                    Log.e("NOTIF_SYNC", "Error marking notification read id=" + n.getId(), t);
                                }
                            });
                        }
                    }
                }

                if (unreadCount > 0) {
                    // Show a summary system notification instead of a Toast
                    String summaryTitle = "New notifications";
                    String summaryMessage = "You have " + unreadCount + " new notifications";
                    int summaryId = (int) (System.currentTimeMillis() & 0x7fffffff);
                    showUserNotification(summaryTitle, summaryMessage, summaryId);

                    Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                    if (current instanceof NotificationsFragment) {
                        ((NotificationsFragment) current).refreshNotifications();
                    }
                }
            }));

            webSocketManager.subscribeToUserNotification(currentUserId, notification -> runOnUiThread(() -> {
                if (notification == null) return;
                String title = notification.getTitle() != null ? notification.getTitle() : "Notification";
                int nid = (notification.getId() != null) ? notification.getId().intValue() : (int) (System.currentTimeMillis() & 0x7fffffff);
                String msg = notification.getMessage() != null ? notification.getMessage() : "";
                // Post a real system notification for the incoming single notification
                Log.d("NOTIF_FLOW", "Single notification -> showing notification id=" + nid + " title='" + title + "' msg='" + msg + "'");
                showUserNotification(title, msg, nid);

                // mark single notification as viewed on backend to avoid re-forwarding
                if (notification.getId() != null) {
                    ApiClient.getNotificationApiService().deleteNotification(notification.getId())
                            .enqueue(new Callback<NotificationDTO>() {
                                @Override
                                public void onResponse(Call<com.example.getgo.dtos.notification.NotificationDTO> call, Response<com.example.getgo.dtos.notification.NotificationDTO> response) {
                                    if (response.isSuccessful()) {
                                        Log.d("NOTIF_SYNC", "Marked single notification read id=" + notification.getId());
                                    } else {
                                        Log.w("NOTIF_SYNC", "Failed to mark single notification read id=" + notification.getId() + " code=" + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<com.example.getgo.dtos.notification.NotificationDTO> call, Throwable t) {
                                    Log.e("NOTIF_SYNC", "Error marking single notif read id=" + notification.getId(), t);
                                }
                            });
                }

                Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (current instanceof NotificationsFragment) {
                    ((NotificationsFragment) current).refreshNotifications();
                }
            }));

            // Request unread notifications immediately (no userId param required)
            webSocketManager.requestUnreadNotifications();

            // Also start periodic polling to request unread notifications repeatedly (every 10s)
            webSocketManager.startUnreadNotificationsPolling(10);
        }));

        webSocketManager.connect();
        Log.d("NOTIF_DEBUG", "WebSocket connect() pozvan");
    }

    // Helper: show simple system notification (channel must exist elsewhere in app)
    private void showUserNotification(String title, String message, int id) {
        try {
            Log.d("NOTIF_FLOW", "showUserNotification called id=" + id + " title='" + title + "'");
             // If runtime permission is required and not granted, request it and skip showing now
             if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                 if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                     // Ask user for permission; the next incoming notification will be shown after grant
                     int notifFlow = Log.w("NOTIF_FLOW", "POST_NOTIFICATIONS not granted, requesting permission");
                     ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_CODE_POST_NOTIFICATIONS);
                     return;
                 }
             }

            // Build PendingIntent to open NotificationsFragment when user taps the notification
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("OPEN_NOTIFICATIONS", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "getgo_channel")
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    // Use defaults for vibration/sound as channel provides sound
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationManagerCompat.from(this).notify(id, builder.build());
            Log.d("NOTIF_FLOW", "Notification posted id=" + id);

            // Also play local sound immediately (fallback in case notifications are suppressed)
            playNotificationSound();
        } catch (Exception ex) {
            Log.e("MainActivity", "Failed to post system notification", ex);
        }
    }

    // Create notification channel used by showUserNotification()
    private void createNotificationChannel() {
        try {
            String channelId = "getgo_channel";
            String channelName = "GetGo Notifications";
            String channelDesc = "Notifications from GetGo app";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationManager nm = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                // Delete old channel to ensure new settings (importance/sound) are applied.
                // Note: deleting a channel resets any user-customizations; keep for debugging/testing.
                try {
                    android.app.NotificationChannel existing = nm.getNotificationChannel(channelId);
                    if (existing != null) {
                        nm.deleteNotificationChannel(channelId);
                    }
                } catch (Exception ignored) {}

                android.app.NotificationChannel channel = new android.app.NotificationChannel(channelId, channelName, importance);
                channel.setDescription(channelDesc);

                // Use default notification sound
                Uri soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                channel.setSound(soundUri, audioAttributes);
                channel.enableVibration(true);
                channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

                nm.createNotificationChannel(channel);
            }
        } catch (Exception ex) {
            Log.e("MainActivity", "Failed to create notification channel", ex);
        }
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        if (intent == null) return;
//        // If notification click asked to open notifications, do it
//        if (intent.getBooleanExtra("OPEN_NOTIFICATIONS", false)) {
//            runOnUiThread(() -> {
//                Fragment fragment = new NotificationsFragment();
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.fragmentContainer, fragment)
//                        .commit();
//            });
//            return;
//        }
//
//        // also handle other notification intents (rate fragment / ride id) via existing handler
//        handleNotificationIntent(intent);
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQ_CODE_POST_NOTIFICATIONS) {
//            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d("MainActivity", "POST_NOTIFICATIONS granted");
//            } else {
//                Log.w("MainActivity", "POST_NOTIFICATIONS denied");
//                // Optionally guide user to settings
//                // Show a short toast / log
//                Toast.makeText(this, "Notifications disabled. Enable in system settings.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void requestNotificationPermissionIfNeeded() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // Request the permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_CODE_POST_NOTIFICATIONS);
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to request notification permission", e);
        }
    }

    private void playNotificationSound() {
        try {
            if (notificationSound != null) {
                if (notificationSound.isPlaying()) return;
                notificationSound.start();
                return;
            }

            // Try to load app bundled sound (same as panic)
            int resId = getResources().getIdentifier("notification_sound", "raw", getPackageName());
            if (resId != 0) {
                notificationSound = MediaPlayer.create(this, resId);
                if (notificationSound != null) {
                    notificationSound.start();
                    return;
                }
            }

            // Fallback to default notification ringtone
            try {
                android.media.Ringtone r = android.media.RingtoneManager.getRingtone(this, android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION));
                if (r != null) r.play();
            } catch (Exception ignored) {}
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to play notification sound", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
            webSocketManager = null;
        }
        try {
            if (notificationSound != null) {
                if (notificationSound.isPlaying()) notificationSound.stop();
                notificationSound.release();
                notificationSound = null;
            }
        } catch (Exception ignored) {}
    }

    private void onWebSocketNotificationReceived(NotificationDTO notif, long rideId) {
        Log.d("NOTIF_DEBUG", "Primljena notifikacija: title=" + notif.getTitle() + ", rideId=" + rideId); // <- dodaj

        showLocalNotification(notif, rideId);

        NotificationsFragment fragment = (NotificationsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        if (fragment != null) {
            fragment.addNotification(notif);
        }
    }

    private void showLocalNotification(NotificationDTO notif, long rideId) {
        Log.d("NOTIF_DEBUG", "showLocalNotification: pripremam notifikaciju za rideId=" + rideId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("NOTIF_DEBUG", "POST_NOTIFICATIONS permission nije odobrena");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101);
                return;
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("OPEN_RIDE_TRACKING", true);
        intent.putExtra("RIDE_ID", rideId);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, notif.getId().intValue(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "getgo_notifications")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notif.getTitle())
                .setContentText(notif.getMessage())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(notif.getId().intValue(), builder.build());

        Log.d("NOTIF_DEBUG", "Notifikacija poslata OS-u: id=" + notif.getId());
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 101) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
//                Log.d("NOTIF_DEBUG", "Notification permission granted");
//            } else {
//                Log.d("NOTIF_DEBUG", "Notification permission denied");
//                Toast.makeText(this, "Cannot show notifications without permission", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        handleIntent(intent);
//        handleNotificationIntent(intent);
//    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) return;

        // Open notifications fragment
        if (intent.getBooleanExtra("OPEN_NOTIFICATIONS", false)) {
            Fragment fragment = new NotificationsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return;
        }

        // Ride tracking / rate fragment handling
        handleIntent(intent);
        handleNotificationIntent(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CODE_POST_NOTIFICATIONS || requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Notification permission granted");
            } else {
                Log.w("MainActivity", "Notification permission denied");
                Toast.makeText(this,
                        "Notifications disabled. Enable in system settings.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
