package livecricket.livecrickettv.cricketstreaming.ads;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import livecricket.livecrickettv.cricketstreaming.BuildConfig;
import livecricket.livecrickettv.cricketstreaming.R;


public class AdsHelper {
    private static final String TAG = "AdsHelper";
    private static final long AD_EXPIRATION_MS = 4 * 3600 * 1000L; // 4 hours

    private static AdsHelper instance;
    private final Context appContext;
    private final AdTimeManager adTimeManager;
    private final AdsSPGetSet appSPGetSet;

    // Interstitial State
    private InterstitialAd interstitialAd = null;
    private boolean isInterstitialLoading = false;
    private long interstitialLoadTime = 0;
    private String lastInterstitialAdUnitId = null;

    // Rewarded State
    private RewardedAd rewardedAd = null;
    private boolean isRewardedLoading = false;
    private long rewardedLoadTime = 0;
    private String lastRewardedAdUnitId = null;
    private boolean isRewardedAdShownInSession = false;

    public static boolean interAdShowing = false;

    // Private constructor
    private AdsHelper(Context context) {
        this.appContext = context.getApplicationContext();
        this.adTimeManager = new AdTimeManager(context);
        this.appSPGetSet = new AdsSPGetSet();
    }

    // Singleton getInstance method
    public static synchronized AdsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AdsHelper(context.getApplicationContext());
        }
        return instance;
    }

    public void setAdInterval(Integer seconds) {
        if (adTimeManager != null) {
            adTimeManager.setAdIntervalInSeconds(seconds);
        }
    }

    public interface OnConsentGatheredListener {
        void onConsentGathered();
    }

    public void gatherConsent(Activity activity, OnConsentGatheredListener listener) {
        if (BuildConfig.DEBUG) {
            if (listener != null) {
                listener.onConsentGathered();
            }
            return;
        }

        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                () -> {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            activity,
                            formError -> {
                                if (consentInformation.canRequestAds()) {
                                    initializeAdMob(activity, "");
                                }
                                if (listener != null) {
                                    listener.onConsentGathered();
                                }
                            }
                    );
                },
                requestConsentError -> {
                    if (consentInformation.canRequestAds()) {
                        initializeAdMob(activity, "");
                    }
                    if (listener != null) {
                        listener.onConsentGathered();
                    }
                }
        );
    }

    public void showPrivacyOptionsForm(Activity activity, ConsentForm.OnConsentFormDismissedListener listener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, listener);
    }

    public boolean isPrivacyOptionsRequired(Context context) {
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(context);
        return consentInformation.getPrivacyOptionsRequirementStatus() == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    public void initializeAdMob(Activity activity, String appId) {
        if (BuildConfig.DEBUG) {
            return;
        }
        MobileAds.initialize(activity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                Log.d(TAG, "AdMob initialized with App ID: " + appId);
            }
        });
    }

    // =========================================================================
    // INTERSTITIAL ADS
    // =========================================================================

    public boolean isInterstitialAvailable() {
        if (interstitialAd == null) return false;
        long timeSinceLoad = System.currentTimeMillis() - interstitialLoadTime;
        if (timeSinceLoad >= AD_EXPIRATION_MS) {
            Log.d(TAG, "Cached Interstitial expired (>4h). Discarding.");
            interstitialAd = null;
            return false;
        }
        return true;
    }

    public void preloadAdADMOB_X_Inter(Context context, String adUnitId) {
        if (BuildConfig.DEBUG) {
            return;
        }
        if (adUnitId == null || adUnitId.trim().isEmpty()) {
            return;
        }

        this.lastInterstitialAdUnitId = adUnitId;

        if (isInterstitialLoading) {
            Log.d(TAG, "Interstitial is already loading. Skipping duplicate request.");
            return;
        }
        if (isInterstitialAvailable()) {
            Log.d(TAG, "Valid Interstitial already cached. Skipping request.");
            return;
        }

        isInterstitialLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        // Use application context to avoid activity leaks during async load
        InterstitialAd.load(appContext, adUnitId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                isInterstitialLoading = false;
                interstitialAd = ad;
                interstitialLoadTime = System.currentTimeMillis();
                Log.d(TAG, "Interstitial Ad Loaded successfully at " + interstitialLoadTime);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                isInterstitialLoading = false;
                interstitialAd = null;
                Log.e(TAG, "Interstitial Ad failed to load: " + loadAdError.getMessage());
            }
        });
    }

    public void showAd_Mob_X_Inter_With_Time(Activity activity) {
        showRewardedOrInterstitialAd(activity, null);
    }

    public void showAd_Mob_X_Inter_With_Time(Activity activity, Runnable onAdClosed) {
        showRewardedOrInterstitialAd(activity, onAdClosed);
    }

    public void showRewardedOrInterstitialAd(Activity activity) {
        showRewardedOrInterstitialAd(activity, null);
    }

    /**
     * Priority Ad Method:
     * Shows 1 Rewarded Ad per session if available.
     * If Rewarded Ad is unavailable, already shown this session, or fails to show,
     * falls back to Interstitial Ad.
     */
    public void showRewardedOrInterstitialAd(Activity activity, Runnable onAdClosed) {
        Runnable safeCallback = new Runnable() {
            private boolean called = false;
            @Override
            public void run() {
                if (!called) {
                    called = true;
                    if (onAdClosed != null) {
                        new Handler(Looper.getMainLooper()).post(onAdClosed);
                    }
                }
            }
        };

        if (BuildConfig.DEBUG) {
            safeCallback.run();
            return;
        }

        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            Log.w(TAG, "Host activity is finishing/destroyed. Cancelling ad display.");
            safeCallback.run();
            return;
        }

        boolean isFirstAd = appSPGetSet.getAddFirstTimeSP(activity);
        boolean canShowByTimer = adTimeManager.canShowAd();

        if (!isFirstAd && !canShowByTimer) {
            safeCallback.run();
            return;
        }

        // Check if Rewarded Ad should be shown first (max 1 per session)
        if (!isRewardedAdShownInSession && isRewardedAvailable()) {
            RewardedAd adToShow = rewardedAd;
            rewardedAd = null; // Consume ad immediately

            adToShow.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed.");
                    interAdShowing = false;
                    isRewardedAdShownInSession = true;
                    appSPGetSet.setRewardAdShownSP(activity, true);
                    adTimeManager.setLastAdShownTime(System.currentTimeMillis());
                    appSPGetSet.setAddFirstTimeSP(activity, false);
                    safeCallback.run();

                    // Preload interstitial for subsequent ad triggers in this session
                    if (lastInterstitialAdUnitId != null) {
                        preloadAdADMOB_X_Inter(activity, lastInterstitialAdUnitId);
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    Log.e(TAG, "Rewarded ad failed to show: " + adError.getMessage() + ". Falling back to Interstitial.");
                    interAdShowing = false;
                    isRewardedAdShownInSession = true;
                    showInterstitialFallback(activity, safeCallback);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad shown.");
                    interAdShowing = true;
                }
            });

            adToShow.show(activity, rewardItem -> {
                Log.d(TAG, "User completed rewarded ad.");
            });
            return;
        }

        // Fallback to Interstitial Ad
        showInterstitialFallback(activity, safeCallback);
    }

    private void showInterstitialFallback(Activity activity, Runnable safeCallback) {
        if (!isInterstitialAvailable()) {
            Log.d(TAG, "Interstitial ad not ready or expired.");
            safeCallback.run();
            if (lastInterstitialAdUnitId != null) {
                preloadAdADMOB_X_Inter(activity, lastInterstitialAdUnitId);
            }
            return;
        }

        InterstitialAd adToShow = interstitialAd;
        interstitialAd = null; // Consume ad immediately

        adToShow.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial dismissed.");
                interAdShowing = false;
                adTimeManager.setLastAdShownTime(System.currentTimeMillis());
                appSPGetSet.setAddFirstTimeSP(activity, false);
                safeCallback.run();

                if (lastInterstitialAdUnitId != null) {
                    preloadAdADMOB_X_Inter(activity, lastInterstitialAdUnitId);
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                Log.e(TAG, "Interstitial failed to show: " + adError.getMessage());
                interAdShowing = false;
                safeCallback.run();

                if (lastInterstitialAdUnitId != null) {
                    preloadAdADMOB_X_Inter(activity, lastInterstitialAdUnitId);
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial shown.");
                interAdShowing = true;
            }
        });

        adToShow.show(activity);
    }

    // =========================================================================
    // REWARDED ADS
    // =========================================================================

    public boolean isRewardedAvailable() {
        if (rewardedAd == null) return false;
        long timeSinceLoad = System.currentTimeMillis() - rewardedLoadTime;
        if (timeSinceLoad >= AD_EXPIRATION_MS) {
            Log.d(TAG, "Cached Rewarded expired (>4h). Discarding.");
            rewardedAd = null;
            return false;
        }
        return true;
    }

    public void preloadRewardedAd(Context context, String adUnitId) {
        if (BuildConfig.DEBUG) {
            return;
        }
        if (isRewardedAdShownInSession) {
            Log.d(TAG, "Rewarded ad already shown in this session. Skipping preload.");
            return;
        }
        if (adUnitId == null || adUnitId.trim().isEmpty()) {
            return;
        }

        this.lastRewardedAdUnitId = adUnitId;

        if (isRewardedLoading) {
            Log.d(TAG, "Rewarded ad is already loading. Skipping duplicate request.");
            return;
        }
        if (isRewardedAvailable()) {
            Log.d(TAG, "Valid Rewarded ad already present in memory. Skipping request.");
            return;
        }

        isRewardedLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(appContext, adUnitId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                isRewardedLoading = false;
                rewardedAd = ad;
                rewardedLoadTime = System.currentTimeMillis();
                Log.d(TAG, "Rewarded Ad Loaded successfully at " + rewardedLoadTime);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                isRewardedLoading = false;
                rewardedAd = null;
                Log.e(TAG, "Rewarded Ad failed to load: " + loadAdError.getMessage() + ". Preloading Interstitial fallback.");
                if (lastInterstitialAdUnitId != null) {
                    preloadAdADMOB_X_Inter(appContext, lastInterstitialAdUnitId);
                }
            }
        });
    }

    public void showRewardedAd(Activity activity) {
        showRewardedAd(activity, null, null);
    }

    public void showRewardedAd(Activity activity, Runnable onRewarded, Runnable onDismissed) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            if (onDismissed != null) onDismissed.run();
            return;
        }

        if (!isRewardedAvailable()) {
            Log.d(TAG, "Rewarded ad not available.");
            if (onDismissed != null) onDismissed.run();
            return;
        }

        RewardedAd adToShow = rewardedAd;
        rewardedAd = null;

        adToShow.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed.");
                isRewardedAdShownInSession = true;
                adTimeManager.setLastAdShownTime(System.currentTimeMillis());
                if (onDismissed != null) onDismissed.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                Log.e(TAG, "Rewarded ad failed to show: " + adError.getMessage());
                isRewardedAdShownInSession = true;
                if (onDismissed != null) onDismissed.run();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad shown.");
                isRewardedAdShownInSession = true;
            }
        });

        adToShow.show(activity, rewardItem -> {
            Log.d(TAG, "User completed rewarded ad.");
            if (onRewarded != null) onRewarded.run();
        });
    }

    // =========================================================================
    // BANNER & NATIVE ADS
    // =========================================================================

    public void loadAdaptiveADMOB_X_Banner(Activity activity, RelativeLayout adContainerView, String bannerId) {
        if (BuildConfig.DEBUG || activity == null || adContainerView == null || bannerId == null || bannerId.isEmpty()) {
            return;
        }

        AdView adView = new AdView(activity);
        adView.setAdUnitId(bannerId);
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getBannerAdSize(activity);
        adView.setAdSize(adSize);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private AdSize getBannerAdSize(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int adWidth = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    public void loadNativeBannerAd(Context context, NativeAdView adView, String nativeId) {
        if (BuildConfig.DEBUG || context == null || adView == null || nativeId == null || nativeId.isEmpty()) {
            return;
        }

        AdLoader adLoader = new AdLoader.Builder(context, nativeId)
                .forNativeAd(nativeAd -> populateNativeAdView(nativeAd, adView))
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        adView.setVisibility(View.GONE);
                    }
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        if (adView == null || nativeAd == null) return;

        TextView headlineView = adView.findViewById(R.id.ad_headline);
        if (headlineView != null) {
            if (nativeAd.getHeadline() != null) {
                headlineView.setText(nativeAd.getHeadline());
                adView.setHeadlineView(headlineView);
            } else {
                headlineView.setVisibility(View.GONE);
            }
        }

        ImageView iconView = adView.findViewById(R.id.ad_app_icon);
        if (iconView != null) {
            if (nativeAd.getIcon() != null) {
                iconView.setImageDrawable(nativeAd.getIcon().getDrawable());
                iconView.setVisibility(View.VISIBLE);
                adView.setIconView(iconView);
            } else {
                iconView.setVisibility(View.GONE);
            }
        }

        View callToActionView = adView.findViewById(R.id.ad_call_to_action);
        if (callToActionView != null) {
            if (nativeAd.getCallToAction() != null) {
                if (callToActionView instanceof TextView) {
                    ((TextView) callToActionView).setText(nativeAd.getCallToAction());
                }
                callToActionView.setVisibility(View.VISIBLE);
                adView.setCallToActionView(callToActionView);
            } else {
                callToActionView.setVisibility(View.GONE);
            }
        }

        adView.setNativeAd(nativeAd);
        adView.setVisibility(View.VISIBLE);
    }

    // =========================================================================
    // UNITY ADS
    // =========================================================================

    public void initUnityAds(Activity activity, String appID) {
        UnityAds.initialize(activity, appID, false, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {}

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {}
        });
    }

    public void loadUnityInterstitialAd(Activity activity, String placementId) {
        UnityAds.load(placementId, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {}

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {}
        });
    }

    public void showUnityInterstitialAd(Activity activity, String placementId) {
        if (adTimeManager.canShowAd()) {
            try {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } catch (Exception ignored) {}

            UnityAds.show(activity, placementId, new IUnityAdsShowListener() {
                @Override
                public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                    loadUnityInterstitialAd(activity, placementId);
                    adTimeManager.setLastAdShownTime(System.currentTimeMillis());
                }

                @Override
                public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {}

                @Override
                public void onUnityAdsShowStart(String placementId) {}

                @Override
                public void onUnityAdsShowClick(String placementId) {}
            });
        }
    }
}
