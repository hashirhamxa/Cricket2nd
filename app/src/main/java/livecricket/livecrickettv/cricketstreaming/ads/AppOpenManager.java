package livecricket.livecrickettv.cricketstreaming.ads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.io.File;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import livecricket.livecrickettv.cricketstreaming.BuildConfig;
import livecricket.livecrickettv.cricketstreaming.newplayer.NewPlayerActivity;
import livecricket.livecrickettv.cricketstreaming.activities.SplashActivity;
import livecricket.livecrickettv.cricketstreaming.network.AppRepository;
import livecricket.livecrickettv.cricketstreaming.utilities.Utils;


public class AppOpenManager implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private static final String LOG_TAG = "AppOpenManager";
    private static final long AD_EXPIRATION_MS = 4 * 3600 * 1000L; // 4 hours

    private final Application myApplication;
    private final AppRepository repository;
    private AppOpenAd appOpenAd = null;
    private Activity currentActivity = null;
    private boolean isShowingAd = false;
    private boolean isLoadingAd = false;
    private long loadTime = 0;
    private String appOpenAdId = null;

    public interface OnAppOpenAdListener {
        void onAdResult();
        void onAdShowed();
    }

    public AppOpenManager(Application myApplication, AppRepository repository) {
        this.myApplication = myApplication;
        this.repository = repository;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public void setAppOpenAdId(String adId) {
        this.appOpenAdId = adId;
    }

    public boolean isAdAvailable() {
        return appOpenAd != null && (System.currentTimeMillis() - loadTime < AD_EXPIRATION_MS);
    }

    public boolean isShowingAd() {
        return isShowingAd;
    }

    /**
     * Dedicated Splash flow method: loads and displays an App Open ad on splash launch.
     * Prevents duplicate load calls and protects against activity finish race conditions.
     */
    public void loadAndShowSplashAd(Activity activity, String adKey, OnAppOpenAdListener listener) {
        if (BuildConfig.DEBUG || adKey == null || adKey.trim().isEmpty()) {
            if (listener != null) listener.onAdResult();
            return;
        }

        this.appOpenAdId = adKey;

        // If a valid ad is already cached (<4 hours), show it immediately
        if (isAdAvailable()) {
            showSplashAd(activity, listener);
            return;
        }

        if (isLoadingAd) {
            Log.d(LOG_TAG, "App Open ad is already loading for splash.");
            return;
        }

        isLoadingAd = true;
        AdRequest request = new AdRequest.Builder().build();

        AppOpenAd.load(myApplication, adKey, request, new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                isLoadingAd = false;
                appOpenAd = ad;
                loadTime = System.currentTimeMillis();
                Log.d(LOG_TAG, "Splash AppOpen ad loaded successfully.");

                if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                    Log.w(LOG_TAG, "Splash activity finished before ad load completed. Preserving ad for foreground.");
                    if (listener != null) listener.onAdResult();
                    return;
                }

                showSplashAd(activity, listener);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                isLoadingAd = false;
                appOpenAd = null;
                Log.e(LOG_TAG, "Splash AppOpen ad failed to load: " + loadAdError.getMessage());
                if (listener != null) listener.onAdResult();
            }
        });
    }

    private void showSplashAd(Activity activity, OnAppOpenAdListener listener) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed() || !isAdAvailable() || isShowingAd) {
            if (listener != null) listener.onAdResult();
            return;
        }

        AppOpenAd adToShow = appOpenAd;
        appOpenAd = null;

        adToShow.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                isShowingAd = false;
                if (listener != null) listener.onAdResult();
                if (appOpenAdId != null) {
                    fetchAd(appOpenAdId);
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                isShowingAd = false;
                if (listener != null) listener.onAdResult();
                if (appOpenAdId != null) {
                    fetchAd(appOpenAdId);
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                isShowingAd = true;
                if (listener != null) listener.onAdShowed();
            }
        });

        adToShow.show(activity);
    }

    /**
     * Legacy signature kept for backwards compatibility.
     */
    public void fetchAndShowAd(String adKey, OnAppOpenAdListener listener) {
        loadAndShowSplashAd(currentActivity, adKey, listener);
    }

    /**
     * Shows ad when app is brought to foreground via ProcessLifecycleOwner.
     */
    public void showAdIfAvailable() {
        if (currentActivity == null || currentActivity.isFinishing() || currentActivity.isDestroyed()) {
            return;
        }
        if (currentActivity instanceof SplashActivity || currentActivity instanceof NewPlayerActivity) {
            return;
        }

        if (!isShowingAd && isAdAvailable()) {
            AppOpenAd adToShow = appOpenAd;
            appOpenAd = null;

            adToShow.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    isShowingAd = false;
                    if (appOpenAdId != null) {
                        fetchAd(appOpenAdId);
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    isShowingAd = false;
                    if (appOpenAdId != null) {
                        fetchAd(appOpenAdId);
                    }
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    isShowingAd = true;
                }
            });

            adToShow.show(currentActivity);
        }
    }

    public void fetchAd(String adKey) {
        if (BuildConfig.DEBUG || adKey == null || adKey.trim().isEmpty()) {
            return;
        }
        if (isAdAvailable() || isLoadingAd) {
            return;
        }

        isLoadingAd = true;
        AdRequest request = new AdRequest.Builder().build();

        AppOpenAd.load(myApplication, adKey, request, new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                isLoadingAd = false;
                appOpenAd = ad;
                loadTime = System.currentTimeMillis();
                Log.d(LOG_TAG, "AppOpen ad cached for next foreground.");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                isLoadingAd = false;
                appOpenAd = null;
                Log.e(LOG_TAG, "AppOpen background fetch failed: " + loadAdError.getMessage());
            }
        });
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (!checkSniffer(currentActivity)) {
            showAdIfAvailable();
        }
    }

    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {}
    @Override public void onActivityStarted(@NonNull Activity activity) { currentActivity = activity; }
    @Override public void onActivityResumed(@NonNull Activity activity) { currentActivity = activity; }
    @Override public void onActivityPaused(@NonNull Activity activity) {}
    @Override public void onActivityStopped(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {}
    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }

    // =========================================================================
    // SECURITY & SNIFFER DETECTION
    // =========================================================================

    public static boolean checkSniffer(Activity currentActivity) {
        if (BuildConfig.DEBUG || currentActivity == null) return false;
        boolean isThreatDetected = false;
        String threatMessage = "";
        String detailMessage = "";

        if (checkVPN(currentActivity)) {
            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "VPN Detected", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "VPN Detected";
            detailMessage = "Our app has detected that you're using a VPN or sniffer app. Please uninstall or disable it to continue.";
            isThreatDetected = true;
        } else if (isUsingProxy(currentActivity)) {
            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Proxy Detected", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Proxy Detected";
            detailMessage = "Please disable your proxy to continue.";
            isThreatDetected = true;
        } else if (isPacketCaptureAppInstalled(currentActivity)) {
            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Unauthorized Activities Detected", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Packet Capture App Detected";
            detailMessage = "Our app has detected a packet capture app. Please uninstall it to continue.";
            isThreatDetected = true;
        } else if (isRooted()) {
            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Rooted Device Detected", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Rooted Device Detected";
            detailMessage = "We do not allow rooted devices to use this app.";
            isThreatDetected = true;
        } else if (hasTunnelingActive(currentActivity)) {
            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Unauthorized Activities Detected", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Network Tunneling Detected";
            detailMessage = "Network tunneling detected. Please disable it to continue.";
            isThreatDetected = true;
        } else if (checkDeveloperOP(currentActivity) == 1) {
            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Developer Options Enabled", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Developer Options Enabled";
            detailMessage = "Please disable USB or Wireless debugging from your phone to use the app.";
            isThreatDetected = true;
        } else if (checkWirelessDebugOP(currentActivity) == 1) {
            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Wireless Debugging Enabled", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Wireless Debugging Enabled";
            detailMessage = "Please disable Wireless debugging from your phone to use the app.";
            isThreatDetected = true;
        }

        if (isThreatDetected) {
            String finalThreatMessage = threatMessage;
            String positiveBtn = "Exit";
            if (finalThreatMessage.contains("Developer Options Enabled") || finalThreatMessage.contains("Wireless Debugging Enabled")) {
                positiveBtn = "Disable";
            }
            Utils.showCustomDialog(currentActivity, threatMessage, detailMessage, positiveBtn, "Cancel", false, false, null,
                    view -> {
                        if (finalThreatMessage.contains("Developer Options Enabled") || finalThreatMessage.contains("Wireless Debugging Enabled")) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                            currentActivity.startActivity(intent);
                        } else {
                            currentActivity.finishAffinity();
                        }
                    },
                    view -> currentActivity.finishAffinity()
            );
            return true;
        }

        return false;
    }

    private static boolean isUsingProxy(Activity activity) {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        return proxyHost != null || proxyPort != null;
    }

    private static boolean isPacketCaptureAppInstalled(Context context) {
        String[] sniffingApps = {
                "com.guoshi.httpcanary", "app.greyshirts.sslcapture", "com.minhui.networkcapture",
                "com.egorovandreyrm.pcapremote", "com.evbadroid.wicapdemo", "com.evbadroid.wicap",
                "com.packagesniffer", "jp.co.taosoftware.android.packetcapture", "com.emanuelef.remote_capture",
                "com.pcap.packetcapture", "com.kpnh.pcap", "com.reqable.android", "com.northghost.touchvpn"
        };

        PackageManager pm = context.getPackageManager();
        for (String packageName : sniffingApps) {
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {}
        }
        return false;
    }

    private static boolean isRooted() {
        String[] paths = {
                "/sbin/su", "/system/bin/su", "/system/xbin/su",
                "/data/local/xbin/su", "/data/local/bin/su",
                "/system/sd/xbin/su", "/system/bin/failsafe/su",
                "/data/local/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }

        String[] superuserPaths = {
                "/system/app/Superuser.apk",
                "/system/app/SuperSU.apk",
                "/system/app/superuser.apk"
        };
        for (String path : superuserPaths) {
            if (new File(path).exists()) return true;
        }

        return false;
    }

    private static boolean hasTunnelingActive(Context context) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (intf.isUp() && !intf.getInterfaceAddresses().isEmpty()) {
                    if (intf.getName().startsWith("tun") ||
                            intf.getName().startsWith("ppp") ||
                            intf.getName().startsWith("tap")) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean checkVPN(Activity currentActivity) {
        try {
            ConnectivityManager cm = (ConnectivityManager) currentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null && cm.getNetworkInfo(ConnectivityManager.TYPE_VPN) != null) {
                return cm.getNetworkInfo(ConnectivityManager.TYPE_VPN).isConnectedOrConnecting();
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static int checkDeveloperOP(Activity currentActivity) {
        try {
            return Settings.Global.getInt(currentActivity.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static int checkWirelessDebugOP(Activity currentActivity) {
        try {
            return Settings.Global.getInt(currentActivity.getContentResolver(), "adb_wifi_enabled", 0);
        } catch (Exception ignored) {
            return 0;
        }
    }
}