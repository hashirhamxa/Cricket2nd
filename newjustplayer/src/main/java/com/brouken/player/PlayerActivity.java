package com.brouken.player;

import static android.content.pm.PackageManager.FEATURE_EXPANDED_PICTURE_IN_PICTURE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriPermission;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.LoudnessEnhancer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.view.HapticFeedbackConstants;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.accessibility.CaptioningManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager;
import androidx.media3.exoplayer.drm.FrameworkMediaDrm;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory;
import androidx.media3.extractor.ts.TsExtractor;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;
import androidx.media3.ui.TimeBar;

import com.brouken.player.dtpv.DoubleTapPV;
import com.brouken.player.dtpv.youtube.YTOverlay;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.snackbar.Snackbar;
import com.homesoft.exo.extractor.PlayerAviExtractorsFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends Activity {
    private PlayerListener playerListener;
    private BroadcastReceiver broadcastReceiver;
    private AudioManager audioManager;
    private MediaSession mediaSession;
    private DefaultTrackSelector defaultTrackSelector;
    public static LoudnessEnhancer loudnessEnhancer;

    public CustomPlayerView customPlayerView;
    public static ExoPlayer exoPlayer;
    private YTOverlay YTOverlay;

    private Object pictureInPictureParamsBuilder;

    public SPHandler spHandler;
    public BrightnessControlHandler brightnessControlHandler;
    public static boolean haveMedia;
    private boolean videoLoading;
    public static boolean controllerVisible;
    public static boolean controllerVisibleFully;
    public static Snackbar snackbar;
    private ExoPlaybackException errorToShow;
    public static int boostLevel = 0;
    private boolean isScaling = false;
    private boolean isScaleStarting = false;
    private float scaleFactor = 1.0f;

    private static final int REQUEST_CHOOSER_VIDEO = 1;
    private static final int REQUEST_CHOOSER_SUBTITLE = 2;
    private static final int REQUEST_CHOOSER_SCOPE_DIR = 10;
    private static final int REQUEST_CHOOSER_VIDEO_MEDIA_STORE = 20;
    private static final int REQUEST_CHOOSER_SUBTITLE_MEDIA_STORE = 21;
    private static final int REQUEST_SETTINGS = 100;
    private static final int REQUEST_SYSTEM_CAPTIONS = 200;
    public static final int CONTROLLER_TIMEOUT = 3500;
    private static final String ACTION_MEDIA_CONTROL = "media_control";
    private static final String EXTRA_CONTROL_TYPE = "control_type";
    private static final int REQUEST_PLAY = 1;
    private static final int REQUEST_PAUSE = 2;
    private static final int CONTROL_TYPE_PLAY = 1;
    private static final int CONTROL_TYPE_PAUSE = 2;

    private CoordinatorLayout coordinatorLayout;
    //    private TextView titleView;
    private RelativeLayout topPanel;
    private TextView videoTitleTxt;


    private ImageButton openBtn;
    //    private ImageButton piPBtn;
    private ImageButton aspectRatioBtn;
    //  private ImageButton rotationBtn;
    private ImageButton exoSettingsBtn;
    private ImageButton exoPlayPauseBtn;
    private ProgressBar loadingProgressBar;
    private PlayerControlView playerControlView;
    private CustomTimeBar customTimeBar;

    private boolean restoreOrientationLock;
    private boolean restorePlayState;
    private boolean restorePlayStateAllowed;
    private boolean play;
    private float subtitlesScale;
    private boolean isScrubbing;
    private boolean scrubbingNoticeable;
    private long scrubbingStart;
    public boolean frameRendered;
    private boolean alive;
    public static boolean focusPlay = false;
    private Uri nextUri;
    private static boolean isTvBox;
    public static boolean locked = false;
    private Thread nextUriThread;
    public Thread frameRateSwitchThread;
    public Thread chaptersThread;
    private long lastScrubbingPosition;
    public static long[] chapterStarts;

    public static boolean restoreControllerTimeout = false;
    public static boolean shortControllerTimeout = false;

    final Rational rationalLimitWide = new Rational(239, 100);
    final Rational rationalLimitTall = new Rational(100, 239);

    static final String API_POSITION = "position";
    static final String API_DURATION = "duration";
    static final String API_RETURN_RESULT = "return_result";
    static final String API_SUBS = "subs";
    static final String API_SUBS_ENABLE = "subs.enable";
    static final String API_SUBS_NAME = "subs.name";
    static final String API_TITLE = "title";
    static final String API_END_BY = "end_by";
    boolean apiAccess;
    String apiTitle;
    List<MediaItem.SubtitleConfiguration> apiSubs = new ArrayList<>();
    boolean intentReturnResult;
    boolean playbackFinished;

    DisplayManager displayManager;
    DisplayManager.DisplayListener displayListener;
    SubtitleFinderHandler subtitleFinderHandler;

    Runnable barsHider = () -> {
        if (customPlayerView != null && !controllerVisible) {
            Utility.toggleSystemUi(PlayerActivity.this, customPlayerView, false);
        }
    };


    //leo
    boolean isVideoLoop = false;
    String videoTitleForVideo;
    String linkType;
    String mpdLink;
    String mpdKey;
    String refererHeader;
    String originHeader;
    String userAgentHeader;
    RelativeLayout bannerAdLayout;
    private TextView slidingMessage;

    // Copyright Blocker
    private boolean showCopyrightBlocker = false;
    private String copyrightBlockerPosition = null;
    private String copyrightBlockerSize = null;
    private FrameLayout copyrightBlockerContainer;
    private ImageView imgCopyrightBlocker;
    private final ExecutorService blurExecutor = Executors.newSingleThreadExecutor();
    private int lastBlockerWidth = -1;
    private int lastBlockerHeight = -1;
    private boolean blockerBlurApplied = false;
    private boolean firstFrameRendered = false;
    private boolean blockerHasBeenShown = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Rotate ASAP to Landscape to avoid initial portrait launch or glitches
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        spHandler = new SPHandler(this);

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT == 28 && Build.MANUFACTURER.equalsIgnoreCase("xiaomi") &&
                (Build.DEVICE.equalsIgnoreCase("oneday") || Build.DEVICE.equalsIgnoreCase("once"))) {
            setContentView(R.layout.activity_player_textureview);
        } else {
            setContentView(R.layout.activity_player);
        }


        if (Build.VERSION.SDK_INT >= 31) {
            Window window = getWindow();
            if (window != null) {
                window.setDecorFitsSystemWindows(false);
                WindowInsetsController windowInsetsController = window.getInsetsController();
                if (windowInsetsController != null) {
                    // On Android 12 BEHAVIOR_DEFAULT allows system gestures without visible system bars
                    windowInsetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
                }
            }
        }

        isTvBox = Utility.isTvBox(this);

        if (isTvBox) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        final Intent launchIntent = getIntent();
        if (launchIntent != null) {
            showCopyrightBlocker = launchIntent.getBooleanExtra("show_copyright_blocker", launchIntent.getBooleanExtra("showCopyrightBlocker", false));
            copyrightBlockerPosition = launchIntent.getStringExtra("copyright_blocker_position");
            if (copyrightBlockerPosition == null) {
                copyrightBlockerPosition = launchIntent.getStringExtra("copyrightBlockerPosition");
            }
            copyrightBlockerSize = launchIntent.getStringExtra("copyright_blocker_size");
            if (copyrightBlockerSize == null) {
                copyrightBlockerSize = launchIntent.getStringExtra("copyrightBlockerSize");
            }
        }
        final String action = launchIntent.getAction();
        final String type = launchIntent.getType();

        if ("com.brouken.player.action.SHORTCUT_VIDEOS".equals(action)) {
            openFile(Utility.getMoviesFolderUri());
        } else if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String text = launchIntent.getStringExtra(Intent.EXTRA_TEXT);
            isVideoLoop = getIntent().getBooleanExtra("isVideoLoop", false);
            videoTitleForVideo = getIntent().getStringExtra("videoTittle");
            linkType = getIntent().getStringExtra("linkType");
            mpdLink = getIntent().getStringExtra("mpdLink");
            mpdKey = getIntent().getStringExtra("mpdKey");
            refererHeader = getIntent().getStringExtra("refererHeader");
            originHeader = getIntent().getStringExtra("originHeader");
            userAgentHeader = getIntent().getStringExtra("userAgentHeader");
            String link = getIntent().getStringExtra("videoLink");


            if (link != null && !link.equals("null")) {
                if (!link.startsWith("http://") && !link.startsWith("https://") && (link.startsWith("ey") || link.length() > 20)) {
                    try {
                        byte[] decoded = Base64.decode(link, Base64.DEFAULT);
                        String jsonStr = new String(decoded, StandardCharsets.UTF_8);
                        if (jsonStr.startsWith("{") && (jsonStr.contains("\"url\"") || jsonStr.contains("\"api\""))) {
                            org.json.JSONObject obj = new org.json.JSONObject(jsonStr);
                            String streamUrl = obj.optString("url", obj.optString("api", ""));
                            String typeStr = obj.optString("type", "");
                            String defStr = obj.optString("default_string", "");
                            if (!streamUrl.isEmpty()) {
                                if (!defStr.isEmpty()) {
                                    String encodedKey = Base64.encodeToString(defStr.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE | Base64.NO_WRAP).trim();
                                    String sep = streamUrl.contains("?") ? "&" : "?";
                                    streamUrl = streamUrl + sep + "ls_b64=" + encodedKey;
                                }
                                link = streamUrl;
                            }
                        }
                    } catch (Exception ignored) {}
                }
                final Uri parsedUri = Uri.parse(link);
                if (parsedUri.isAbsolute()) {
                    spHandler.updateMedia(this, parsedUri, null);
                    focusPlay = true;
                }
            } else if (mpdLink != null && !mpdLink.equals("null")) {
                final Uri parsedUri = Uri.parse(mpdLink);

                if (parsedUri.isAbsolute()) {
                    spHandler.updateMedia(this, parsedUri, null);
                    focusPlay = true;
                }
            }

        } else if (launchIntent.getData() != null) {
            resetApiAccess();
            final Uri uri = launchIntent.getData();
            if (SubtitleUtility.isSubtitle(uri, type)) {
                handleVideoSubtitles(uri);
            } else {
                Bundle bundle = launchIntent.getExtras();
                if (bundle != null) {
                    apiAccess = bundle.containsKey(API_POSITION) || bundle.containsKey(API_RETURN_RESULT) || bundle.containsKey(API_TITLE)
                            || bundle.containsKey(API_SUBS) || bundle.containsKey(API_SUBS_ENABLE);
                    if (apiAccess) {
                        spHandler.setPersistent(false);
                    }
                    apiTitle = bundle.getString(API_TITLE);
                }

                spHandler.updateMedia(this, uri, type);

                if (bundle != null) {
                    Uri defaultSub = null;
                    Parcelable[] subsEnable = bundle.getParcelableArray(API_SUBS_ENABLE);
                    if (subsEnable != null && subsEnable.length > 0) {
                        defaultSub = (Uri) subsEnable[0];
                    }

                    Parcelable[] subs = bundle.getParcelableArray(API_SUBS);
                    String[] subsName = bundle.getStringArray(API_SUBS_NAME);
                    if (subs != null && subs.length > 0) {
                        for (int i = 0; i < subs.length; i++) {
                            Uri sub = (Uri) subs[i];
                            String name = null;
                            if (subsName != null && subsName.length > i) {
                                name = subsName[i];
                            }
                            apiSubs.add(SubtitleUtility.buildSubtitle(this, sub, name, sub.equals(defaultSub)));
                        }
                    }
                }

                if (apiSubs.isEmpty()) {
                    searchVideoSubtitles();
                }

                if (bundle != null) {
                    intentReturnResult = bundle.getBoolean(API_RETURN_RESULT);

                    if (bundle.containsKey(API_POSITION)) {
                        spHandler.updatePosition((long) bundle.getInt(API_POSITION));
                    }
                }
            }
            focusPlay = true;
        }

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        slidingMessage = findViewById(R.id.sliding_message);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        customPlayerView = findViewById(R.id.video_view);
        if (customPlayerView != null) {
            customPlayerView.setResizeMode(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL);
            copyrightBlockerContainer = customPlayerView.findViewById(R.id.copyright_blocker_container);
            imgCopyrightBlocker = customPlayerView.findViewById(R.id.img_copyright_blocker);
        }

        if (showCopyrightBlocker && copyrightBlockerContainer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && imgCopyrightBlocker != null) {
                imgCopyrightBlocker.setClipToOutline(true);
            }
        }

        if (showCopyrightBlocker && customPlayerView != null) {
            customPlayerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    int w = right - left;
                    int h = bottom - top;
                    if (w > 0 && h > 0) {
                        updateCopyrightBlockerBounds(w, h);
                    }
                }
            });
        }

        topPanel = customPlayerView.findViewById(R.id.custom_player_top_panel);
        if (topPanel != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            topPanel.setElevation(20f);
            topPanel.setTranslationZ(20f);
        }
        ImageButton backImageButton = customPlayerView.findViewById(R.id.custom_player_img_bck);
        videoTitleTxt = customPlayerView.findViewById(R.id.custom_player_tittle);

        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerActivity.this.onBackPressed();
            }
        });

        exoPlayPauseBtn = findViewById(R.id.exo_play_pause);
        loadingProgressBar = findViewById(R.id.loading);

        customPlayerView.setShowNextButton(false);
        customPlayerView.setShowPreviousButton(false);
        customPlayerView.setShowFastForwardButton(false);
        customPlayerView.setShowRewindButton(false);


        customPlayerView.setRepeatToggleModes(Player.REPEAT_MODE_ONE);

        customPlayerView.setControllerHideOnTouch(false);
        customPlayerView.setControllerAutoShow(true);

        ((DoubleTapPV) customPlayerView).setDoubleTapEnabled(false);

        customTimeBar = customPlayerView.findViewById(R.id.exo_progress);
        customTimeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
                if (exoPlayer == null) {
                    return;
                }
                restorePlayState = exoPlayer.isPlaying();
                if (restorePlayState) {
                    exoPlayer.pause();
                }
                lastScrubbingPosition = position;
                scrubbingNoticeable = false;
                isScrubbing = true;
                frameRendered = true;
                customPlayerView.setControllerShowTimeoutMs(-1);
                scrubbingStart = exoPlayer.getCurrentPosition();
                exoPlayer.setSeekParameters(SeekParameters.CLOSEST_SYNC);
                reportScrubbing(position);
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                reportScrubbing(position);
                for (long start : chapterStarts) {
                    if ((lastScrubbingPosition < start && position >= start) || (lastScrubbingPosition > start && position <= start)) {
                        customPlayerView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                    }
                }
                lastScrubbingPosition = position;
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                customPlayerView.setCustomErrorMessage(null);
                isScrubbing = false;
                if (restorePlayState) {
                    restorePlayState = false;
                    customPlayerView.setControllerShowTimeoutMs(PlayerActivity.CONTROLLER_TIMEOUT);
                    if (exoPlayer != null) {
                        exoPlayer.setPlayWhenReady(true);
                    }
                }
            }
        });

        openBtn = new ImageButton(this, null, 0, androidx.media3.ui.R.style.ExoStyledControls_Button_Bottom);
        openBtn.setImageResource(R.drawable.ic_folder_open_24dp);
        openBtn.setId(View.generateViewId());
        openBtn.setContentDescription(getString(R.string.button_open));

        openBtn.setOnClickListener(view -> openFile(spHandler.mediaUri));

        openBtn.setOnLongClickListener(view -> {
            if (!isTvBox && spHandler.askScope) {
                askForVideoScope(true, false);
            } else {
                loadSubtitleFileForVideo(spHandler.mediaUri);
            }
            return true;
        });

//        if (Utility.isPiPSupported(this)) {
//            // TODO: Android 12 improvements:
//            // https://developer.android.com/about/versions/12/features/pip-improvements
//            pictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
//            boolean success = updatePictureInPictureActions(R.drawable.ic_play_arrow_24dp, androidx.media3.ui.R.string.exo_controls_play_description, CONTROL_TYPE_PLAY, REQUEST_PLAY);
//
//            if (success) {
//                piPBtn = new ImageButton(this, null, 0, androidx.media3.ui.R.style.ExoStyledControls_Button_Bottom);
//                piPBtn.setContentDescription(getString(R.string.button_pip));
//                piPBtn.setImageResource(R.drawable.ic_picture_in_picture_alt_24dp);
//
//                piPBtn.setOnClickListener(view -> enterVideoPiP());
//            }
//        }

        aspectRatioBtn = new ImageButton(this, null, 0, androidx.media3.ui.R.style.ExoStyledControls_Button_Bottom);
        aspectRatioBtn.setId(Integer.MAX_VALUE - 100);
        aspectRatioBtn.setContentDescription(getString(R.string.button_crop));
        updatebuttonAspectRatioIcon();
        aspectRatioBtn.setOnClickListener(view -> {
            customPlayerView.setScale(1.f);
            if (customPlayerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                customPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                Utility.showText(customPlayerView, getString(R.string.video_resize_crop));
            } else {
                // Default mode
                customPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                Utility.showText(customPlayerView, getString(R.string.video_resize_fit));
            }
            updatebuttonAspectRatioIcon();
            resetPlayerHideCallbacks();
        });
        if (isTvBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            aspectRatioBtn.setOnLongClickListener(v -> {
                scaleStart();
                updatebuttonAspectRatioIcon();
                return true;
            });
        }
//        rotationBtn = new ImageButton(this, null, 0, androidx.media3.ui.R.style.ExoStyledControls_Button_Bottom);
//        rotationBtn.setContentDescription(getString(R.string.button_rotate));
//        updateButtonRotation();
//        rotationBtn.setOnClickListener(view -> {
//            spHandler.orientation = Utility.getNextOrientation(spHandler.orientation);
//            Utility.setOrientation(PlayerActivity.this, spHandler.orientation);
//            updateButtonRotation();
//            Utility.showText(customPlayerView, getString(spHandler.orientation.description), 2500);
//            resetPlayerHideCallbacks();
//            if (getString(spHandler.orientation.description).contains("Device orientation")) {
//                customPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
//                updatebuttonAspectRatioIcon();
//                resetPlayerHideCallbacks();
//            } else if (getString(spHandler.orientation.description).contains("Video orientation")) {
//                customPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//                updatebuttonAspectRatioIcon();
//                resetPlayerHideCallbacks();
//            }
//        });

        final int titleViewPaddingHorizontal = Utility.dpToPx(14);
        final int titleViewPaddingVertical = getResources().getDimensionPixelOffset(androidx.media3.ui.R.dimen.exo_styled_bottom_bar_time_padding);



        playerControlView = customPlayerView.findViewById(androidx.media3.ui.R.id.exo_controller);
        playerControlView.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            if (windowInsets != null) {
                if (Build.VERSION.SDK_INT >= 31) {
                    boolean visibleBars = windowInsets.isVisible(WindowInsets.Type.statusBars());
                    if (visibleBars && !controllerVisible) {
                        customPlayerView.postDelayed(barsHider, 2500);
                    } else {
                        customPlayerView.removeCallbacks(barsHider);
                    }
                }

                view.setPadding(0, windowInsets.getSystemWindowInsetTop(),
                        0, windowInsets.getSystemWindowInsetBottom());

                int insetLeft = windowInsets.getSystemWindowInsetLeft();
                int insetRight = windowInsets.getSystemWindowInsetRight();

                int paddingLeft = 0;
                int marginLeft = insetLeft;

                int paddingRight = 0;
                int marginRight = insetRight;

                if (Build.VERSION.SDK_INT >= 28 && windowInsets.getDisplayCutout() != null) {
                    if (windowInsets.getDisplayCutout().getSafeInsetLeft() == insetLeft) {
                        paddingLeft = insetLeft;
                        marginLeft = 0;
                    }
                    if (windowInsets.getDisplayCutout().getSafeInsetRight() == insetRight) {
                        paddingRight = insetRight;
                        marginRight = 0;
                    }
                }
                Utility.setViewParams(topPanel, paddingLeft + titleViewPaddingHorizontal, titleViewPaddingVertical, paddingRight + titleViewPaddingHorizontal, titleViewPaddingVertical,
                        marginLeft, windowInsets.getSystemWindowInsetTop(), marginRight, 0);

                Utility.setViewParams(findViewById(R.id.exo_bottom_bar), paddingLeft, 0, paddingRight, 0,
                        marginLeft, 0, marginRight, 0);

                findViewById(R.id.exo_progress).setPadding(windowInsets.getSystemWindowInsetLeft(), 0,
                        windowInsets.getSystemWindowInsetRight(), 0);

                Utility.setViewMargins(findViewById(androidx.media3.ui.R.id.exo_error_message), 0, windowInsets.getSystemWindowInsetTop() / 2, 0, getResources().getDimensionPixelSize(R.dimen.exo_error_message_margin_bottom) + windowInsets.getSystemWindowInsetBottom() / 2);

                windowInsets.consumeSystemWindowInsets();
            }
            return windowInsets;
        });
        customTimeBar.setAdMarkerColor(Color.argb(0x00, 0xFF, 0xFF, 0xFF));
        customTimeBar.setPlayedAdMarkerColor(Color.argb(0x98, 0xFF, 0xFF, 0xFF));

        try {
            CustomDefaultTrackNameProvider customDefaultTrackNameProvider = new CustomDefaultTrackNameProvider(getResources());
            final Field field = PlayerControlView.class.getDeclaredField("trackNameProvider");
            field.setAccessible(true);
            field.set(playerControlView, customDefaultTrackNameProvider);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        findViewById(R.id.delete).setOnClickListener(view -> askDeleteMedia());

        findViewById(R.id.next).setOnClickListener(view -> {
            if (!isTvBox && spHandler.askScope) {
                askForVideoScope(false, true);
            } else {
                skipToNext();
            }
        });

        exoPlayPauseBtn.setOnClickListener(view -> dispatchPlayPause());

        // Prevent double tap actions in controller
        findViewById(R.id.exo_bottom_bar).setOnTouchListener((v, event) -> true);
        //titleView.setOnTouchListener((v, event) -> true);

        playerListener = new PlayerListener();

        brightnessControlHandler = new BrightnessControlHandler(this);
        if (spHandler.brightness >= 0) {
            brightnessControlHandler.currentBrightnessLevel = spHandler.brightness;
            brightnessControlHandler.setPlayerScreenBrightness(brightnessControlHandler.levelBrightness(brightnessControlHandler.currentBrightnessLevel));
        }
        customPlayerView.setBrightnessControl(brightnessControlHandler);

        final LinearLayout exoBasicControls = customPlayerView.findViewById(R.id.exo_basic_controls);
        final ImageButton exoSubtitle = exoBasicControls.findViewById(R.id.exo_subtitle);
        exoBasicControls.removeView(exoSubtitle);

        exoSettingsBtn = exoBasicControls.findViewById(R.id.exo_settings);
        exoBasicControls.removeView(exoSettingsBtn);
        final ImageButton exoRepeat = exoBasicControls.findViewById(R.id.exo_repeat_toggle);
        exoBasicControls.removeView(exoRepeat);
        //exoBasicControls.setVisibility(View.GONE);

        exoSettingsBtn.setOnLongClickListener(view -> {
            //askForScope(false, false);
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_SETTINGS);
            return true;
        });

        exoSubtitle.setOnLongClickListener(v -> {
            enableRotation();
            safelyStartActivityForResult(new Intent(Settings.ACTION_CAPTIONING_SETTINGS), REQUEST_SYSTEM_CAPTIONS);
            return true;
        });

        updateButtons(false);

        final HorizontalScrollView horizontalScrollView = (HorizontalScrollView) getLayoutInflater().inflate(R.layout.controls, null);
        final LinearLayout controls = horizontalScrollView.findViewById(R.id.controls);

        controls.addView(openBtn);
        openBtn.setVisibility(View.GONE);
        controls.addView(exoSubtitle);
        exoSubtitle.setVisibility(View.GONE);
        controls.addView(aspectRatioBtn);

//        if (Utility.isPiPSupported(this) && piPBtn != null) {
//            controls.addView(piPBtn);
//        }

        if (spHandler.repeatToggle) {
            controls.addView(exoRepeat);
        }
//        if (!isTvBox) {
//            controls.addView(rotationBtn);
//        }
        controls.addView(exoSettingsBtn);
        exoSettingsBtn.setVisibility(View.GONE);

        exoBasicControls.addView(horizontalScrollView);

        if (Build.VERSION.SDK_INT > 23) {
            horizontalScrollView.setOnScrollChangeListener((view, i, i1, i2, i3) -> resetPlayerHideCallbacks());
        }

        customPlayerView.setControllerVisibilityListener(new PlayerView.ControllerVisibilityListener() {
            @Override
            public void onVisibilityChanged(int visibility) {
                controllerVisible = visibility == View.VISIBLE;
                controllerVisibleFully = customPlayerView.isControllerFullyVisible();

                if (PlayerActivity.restoreControllerTimeout) {
                    restoreControllerTimeout = false;
                    if (exoPlayer == null || !exoPlayer.isPlaying()) {
                        customPlayerView.setControllerShowTimeoutMs(-1);
                    } else {
                        customPlayerView.setControllerShowTimeoutMs(PlayerActivity.CONTROLLER_TIMEOUT);
                    }
                }

                // https://developer.android.com/training/system-ui/immersive
                Utility.toggleSystemUi(PlayerActivity.this, customPlayerView, visibility == View.VISIBLE);
                if (visibility == View.VISIBLE) {
                    // Because when using dpad controls, focus resets to first item in bottom controls bar
                    findViewById(R.id.exo_play_pause).requestFocus();
                }

                if (controllerVisible && customPlayerView.isControllerFullyVisible()) {
                    if (spHandler.firstRun && false) {
                        TapTargetView.showFor(PlayerActivity.this,
                                TapTarget.forView(openBtn, getString(R.string.onboarding_open_title), getString(R.string.onboarding_open_description))
                                        .outerCircleColor(R.color.green)
                                        .targetCircleColor(R.color.white)
                                        .titleTextSize(22)
                                        .titleTextColor(R.color.white)
                                        .descriptionTextSize(14)
                                        .cancelable(true),
                                new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        super.onTargetClick(view);
                                        openBtn.performClick();
                                    }
                                });
                        // TODO: Explain gestures?
                        //  "Use vertical and horizontal gestures to change brightness, volume and seek in video"
                        spHandler.markFirstRun();
                    }
                    if (errorToShow != null) {
                        showErrorMessage(errorToShow);
                        errorToShow = null;
                    }
                }
            }
        });

        YTOverlay = findViewById(R.id.youtube_overlay);
        YTOverlay.performListener(new YTOverlay.PerformListener() {
            @Override
            public void onAnimationStart() {
                YTOverlay.setAlpha(1.0f);
                YTOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd() {
                YTOverlay.animate()
                        .alpha(0.0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                YTOverlay.setVisibility(View.GONE);
                                YTOverlay.setAlpha(1.0f);
                            }
                        });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        alive = true;
        if (!(isTvBox && Build.VERSION.SDK_INT >= 31)) {
            updateVideoSubtitleStyle(this);
        }
        if (Build.VERSION.SDK_INT >= 31) {
            customPlayerView.removeCallbacks(barsHider);
            Utility.toggleSystemUi(this, customPlayerView, true);
        }
        initPlayer();
        updateButtonRotation();
    }

    @Override
    public void onResume() {
        super.onResume();
        restorePlayStateAllowed = true;
        if (isTvBox && Build.VERSION.SDK_INT >= 31) {
            updateVideoSubtitleStyle(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePlayerValues();
    }

    @Override
    public void onStop() {
        super.onStop();
        alive = false;
        if (Build.VERSION.SDK_INT >= 31) {
            customPlayerView.removeCallbacks(barsHider);
        }
        customPlayerView.setCustomErrorMessage(null);
        releasePlayer(false);
    }

    @Override
    public void onBackPressed() {
        restorePlayStateAllowed = false;
        super.onBackPressed();
    }

    @Override
    public void finish() {
        if (intentReturnResult) {
            Intent intent = new Intent("com.mxtech.intent.result.VIEW");
            intent.putExtra(API_END_BY, playbackFinished ? "playback_completion" : "user");
            if (!playbackFinished) {
                if (exoPlayer != null) {
                    long duration = exoPlayer.getDuration();
                    if (duration != C.TIME_UNSET) {
                        intent.putExtra(API_DURATION, (int) exoPlayer.getDuration());
                    }
                    if (exoPlayer.isCurrentMediaItemSeekable()) {
                        if (spHandler.persistentMode) {
                            intent.putExtra(API_POSITION, (int) spHandler.nonPersitentPosition);
                        } else {
                            intent.putExtra(API_POSITION, (int) exoPlayer.getCurrentPosition());
                        }
                    }
                }
            }
            setResult(Activity.RESULT_OK, intent);
        }

        super.finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            final String action = intent.getAction();
            final String type = intent.getType();
            final Uri uri = intent.getData();

            if (Intent.ACTION_VIEW.equals(action) && uri != null) {
                if (SubtitleUtility.isSubtitle(uri, type)) {
                    handleVideoSubtitles(uri);
                } else {
                    spHandler.updateMedia(this, uri, type);
                    searchVideoSubtitles();
                }
                focusPlay = true;
                initPlayer();
            } else if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                isVideoLoop = getIntent().getBooleanExtra("isVideoLoop", false);
                videoTitleForVideo = getIntent().getStringExtra("videoTittle");
                String link = getIntent().getStringExtra("videoLink");

                if (link != null) {
//                    final Uri parsedUri = Uri.parse(text);
                    final Uri parsedUri = Uri.parse(link);
                    if (parsedUri.isAbsolute()) {
                        spHandler.updateMedia(this, parsedUri, null);
                        focusPlay = true;
                        initPlayer();
                    }
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                if (exoPlayer == null)
                    break;
                if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                    exoPlayer.pause();
                } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                    exoPlayer.play();
                } else if (exoPlayer.isPlaying()) {
                    exoPlayer.pause();
                } else {
                    exoPlayer.play();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Utility.adjustVolume(this, audioManager, customPlayerView, keyCode == KeyEvent.KEYCODE_VOLUME_UP, event.getRepeatCount() == 0, true);
                return true;
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_SPACE:
                if (exoPlayer == null)
                    break;
                if (!controllerVisibleFully) {
                    if (exoPlayer.isPlaying()) {
                        exoPlayer.pause();
                    } else {
                        exoPlayer.play();
                    }
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_BUTTON_L2:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (!controllerVisibleFully || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                    if (exoPlayer == null)
                        break;
                    customPlayerView.removeCallbacks(customPlayerView.textClearRunnable);
                    long pos = exoPlayer.getCurrentPosition();
                    if (customPlayerView.keySeekStart == -1) {
                        customPlayerView.keySeekStart = pos;
                    }
                    long seekTo = pos - 10_000;
                    if (seekTo < 0)
                        seekTo = 0;
                    exoPlayer.setSeekParameters(SeekParameters.PREVIOUS_SYNC);
                    exoPlayer.seekTo(seekTo);
                    final String message = Utility.formatMilisSign(seekTo - customPlayerView.keySeekStart) + "\n" + Utility.formatMilis(seekTo);
                    customPlayerView.setCustomErrorMessage(message);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_BUTTON_R2:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if (!controllerVisibleFully || keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                    if (exoPlayer == null)
                        break;
                    customPlayerView.removeCallbacks(customPlayerView.textClearRunnable);
                    long pos = exoPlayer.getCurrentPosition();
                    if (customPlayerView.keySeekStart == -1) {
                        customPlayerView.keySeekStart = pos;
                    }
                    long seekTo = pos + 10_000;
                    long seekMax = exoPlayer.getDuration();
                    if (seekMax != C.TIME_UNSET && seekTo > seekMax)
                        seekTo = seekMax;
                    PlayerActivity.exoPlayer.setSeekParameters(SeekParameters.NEXT_SYNC);
                    exoPlayer.seekTo(seekTo);
                    final String message = Utility.formatMilisSign(seekTo - customPlayerView.keySeekStart) + "\n" + Utility.formatMilis(seekTo);
                    customPlayerView.setCustomErrorMessage(message);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                if (isTvBox) {
                    if (controllerVisible && exoPlayer != null && exoPlayer.isPlaying()) {
                        customPlayerView.hideController();
                        return true;
                    } else {
                        onBackPressed();
                    }
                }
                break;
            default:
                if (!controllerVisibleFully) {
                    customPlayerView.showController();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                customPlayerView.postDelayed(customPlayerView.textClearRunnable, CustomPlayerView.MESSAGE_TIMEOUT_KEY);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_BUTTON_L2:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_BUTTON_R2:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if (!isScrubbing) {
                    customPlayerView.postDelayed(customPlayerView.textClearRunnable, 1000);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isScaling) {
            final int keyCode = event.getKeyCode();
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        scale(true);
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        scale(false);
                        break;
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        break;
                    default:
                        if (isScaleStarting) {
                            isScaleStarting = false;
                        } else {
                            scaleEnd();
                        }
                }
            }
            return true;
        }

        if (isTvBox && !controllerVisibleFully) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                onKeyDown(event.getKeyCode(), event);
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                onKeyUp(event.getKeyCode(), event);
            }
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_SCROLL:
                    final float value = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                    Utility.adjustVolume(this, audioManager, customPlayerView, value > 0.0f, Math.abs(value) > 1.0f, true);
                    return true;
            }
        } else if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {
            // TODO: This somehow works, but it would use better filtering
            float value = event.getAxisValue(MotionEvent.AXIS_RZ);
            for (int i = 0; i < event.getHistorySize(); i++) {
                float historical = event.getHistoricalAxisValue(MotionEvent.AXIS_RZ, i);
                if (Math.abs(historical) > value) {
                    value = historical;
                }
            }
            if (Math.abs(value) == 1.0f) {
                Utility.adjustVolume(this, audioManager, customPlayerView, value < 0, true, true);
            }
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);

        if (isInPictureInPictureMode) {
            // On Android TV it is required to hide controller in this PIP change callback
            customPlayerView.hideController();
            setVideoSubtitleTextSizePiP();
            customPlayerView.setScale(1.f);
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent == null || !ACTION_MEDIA_CONTROL.equals(intent.getAction()) || exoPlayer == null) {
                        return;
                    }

                    switch (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                        case CONTROL_TYPE_PLAY:
                            exoPlayer.play();
                            break;
                        case CONTROL_TYPE_PAUSE:
                            exoPlayer.pause();
                            break;
                    }
                }
            };
//            registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(broadcastReceiver, new IntentFilter(ACTION_MEDIA_CONTROL), Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(broadcastReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
            }
        } else {
            setVideoSubtitleTextSize();
            if (spHandler.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
                customPlayerView.setScale(spHandler.scale);
            }
            if (broadcastReceiver != null) {
                unregisterReceiver(broadcastReceiver);
                broadcastReceiver = null;
            }
            customPlayerView.setControllerAutoShow(true);
            if (exoPlayer != null) {
                if (exoPlayer.isPlaying())
                    Utility.toggleSystemUi(this, customPlayerView, false);
                else
                    customPlayerView.showController();
            }
        }
    }

    void resetApiAccess() {
        apiAccess = false;
        apiTitle = null;
        apiSubs.clear();
        spHandler.setPersistent(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (restoreOrientationLock) {
                Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                restoreOrientationLock = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (resultCode == RESULT_OK && alive) {
            releasePlayer();
        }

        if (requestCode == REQUEST_CHOOSER_VIDEO || requestCode == REQUEST_CHOOSER_VIDEO_MEDIA_STORE) {
            if (resultCode == RESULT_OK) {
                resetApiAccess();
                restorePlayState = false;

                final Uri uri = data.getData();

                if (requestCode == REQUEST_CHOOSER_VIDEO) {
                    boolean uriAlreadyTaken = false;

                    // https://commonsware.com/blog/2020/06/13/count-your-saf-uri-permission-grants.html
                    final ContentResolver contentResolver = getContentResolver();
                    for (UriPermission persistedUri : contentResolver.getPersistedUriPermissions()) {
                        if (persistedUri.getUri().equals(spHandler.scopeUri)) {
                            continue;
                        } else if (persistedUri.getUri().equals(uri)) {
                            uriAlreadyTaken = true;
                        } else {
                            try {
                                contentResolver.releasePersistableUriPermission(persistedUri.getUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!uriAlreadyTaken && uri != null) {
                        try {
                            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                }

                spHandler.setPersistent(true);
                spHandler.updateMedia(this, uri, data.getType());

                if (requestCode == REQUEST_CHOOSER_VIDEO) {
                    searchVideoSubtitles();
                }
            }
        } else if (requestCode == REQUEST_CHOOSER_SUBTITLE || requestCode == REQUEST_CHOOSER_SUBTITLE_MEDIA_STORE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();

                if (requestCode == REQUEST_CHOOSER_SUBTITLE) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }

                handleVideoSubtitles(uri);
            }
        } else if (requestCode == REQUEST_CHOOSER_SCOPE_DIR) {
            if (resultCode == RESULT_OK) {
                final Uri uri = data.getData();
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    spHandler.updateScope(uri);
                    spHandler.markScopeAsked();
                    searchVideoSubtitles();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_SETTINGS) {
            spHandler.loadUserPreferences();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        // Init here because onStart won't follow when app was only paused when file chooser was shown
        // (for example pop-up file chooser on tablets)
        if (resultCode == RESULT_OK && alive) {
            initPlayer();
        }
    }

    private void handleVideoSubtitles(Uri uri) {
        // Convert subtitles to UTF-8 if necessary
        SubtitleUtility.clearCache(this);
        uri = SubtitleUtility.convertToUTF(this, uri);
        spHandler.updateSubtitle(uri);
    }

    public void initPlayer() {
        boolean isNetworkUri = Utility.isSupportedNetworkUri(spHandler.mediaUri);
        haveMedia = spHandler.mediaUri != null;

        if (exoPlayer != null) {
            exoPlayer.removeListener(playerListener);
            exoPlayer.clearMediaItems();
            exoPlayer.release();
            exoPlayer = null;
        }

        defaultTrackSelector = new DefaultTrackSelector(this);
        if (spHandler.tunneling) {
            defaultTrackSelector.setParameters(defaultTrackSelector.buildUponParameters()
                    .setTunnelingEnabled(true)
            );
        }
        switch (spHandler.languageAudio) {
            case SPHandler.TRACK_DEFAULT:
                break;
            case SPHandler.TRACK_DEVICE:
                defaultTrackSelector.setParameters(defaultTrackSelector.buildUponParameters()
                        .setPreferredAudioLanguages(Utility.getDeviceLanguages())
                );
                break;
            default:
                defaultTrackSelector.setParameters(defaultTrackSelector.buildUponParameters()
                        .setPreferredAudioLanguages(spHandler.languageAudio)
                );
        }
        switch (spHandler.languageSubtitle) {
            case SPHandler.TRACK_DEFAULT:
                break;
            case SPHandler.TRACK_DEVICE:
                defaultTrackSelector.setParameters(defaultTrackSelector.buildUponParameters()
                        .setPreferredTextLanguages(Utility.getDeviceLanguages())
                );
                break;
            case SPHandler.TRACK_NONE:
                defaultTrackSelector.setParameters(defaultTrackSelector.buildUponParameters()
                        .setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT | C.SELECTION_FLAG_FORCED)
                );
                break;
            default:
                defaultTrackSelector.setParameters(defaultTrackSelector.buildUponParameters()
                        .setPreferredTextLanguage(spHandler.languageSubtitle)
                );
        }
        PlayerAviExtractorsFactory playerAviExtractorsFactory = new PlayerAviExtractorsFactory();
        playerAviExtractorsFactory.getDefaultExtractorsFactory()
                .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS)
                .setTsExtractorTimestampSearchBytes(1500 * TsExtractor.TS_PACKET_SIZE);
        @SuppressLint("WrongConstant") RenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setExtensionRendererMode(spHandler.decoderPriority)
                .setMapDV7ToHevc(spHandler.mapDV7ToHevc);

        ExoPlayer.Builder playerBuilder = new ExoPlayer.Builder(this, renderersFactory)
                .setTrackSelector(defaultTrackSelector)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this, playerAviExtractorsFactory));

        if (haveMedia && isNetworkUri) {
            if (spHandler.mediaUri.getScheme().toLowerCase().startsWith("http")) {
                HashMap<String, String> headers = new HashMap<>();
                String userInfo = spHandler.mediaUri.getUserInfo();
                if (userInfo != null && userInfo.length() > 0 && userInfo.contains(":")) {
                    headers.put("Authorization", "Basic " + Base64.encodeToString(userInfo.getBytes(), Base64.NO_WRAP));
                    DefaultHttpDataSource.Factory defaultHttpDataSourceFactory = new DefaultHttpDataSource.Factory();
                    defaultHttpDataSourceFactory.setDefaultRequestProperties(headers);
                    playerBuilder.setMediaSourceFactory(new DefaultMediaSourceFactory(defaultHttpDataSourceFactory, playerAviExtractorsFactory));
                }
            }
        }

        exoPlayer = playerBuilder.build();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();
        exoPlayer.setAudioAttributes(audioAttributes, true);

        if (spHandler.skipSilence) {
            exoPlayer.setSkipSilenceEnabled(true);
        }

        YTOverlay.player(exoPlayer);
        customPlayerView.setPlayer(exoPlayer);

        if (mediaSession != null) {
            mediaSession.release();
        }

        if (exoPlayer.canAdvertiseSession()) {
            try {
                mediaSession = new MediaSession.Builder(this, exoPlayer).build();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        customPlayerView.setControllerShowTimeoutMs(-1);

        locked = false;

        chapterStarts = new long[0];

        if (haveMedia) {
            if (isNetworkUri) {
                customTimeBar.setBufferedColor(DefaultTimeBar.DEFAULT_BUFFERED_COLOR);
            } else {
                customTimeBar.setBufferedColor(0x33FFFFFF);
            }

            customPlayerView.setResizeMode(spHandler.resizeMode);

            if (spHandler.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
                customPlayerView.setScale(spHandler.scale);
            } else {
                customPlayerView.setScale(1.f);
            }
            updatebuttonAspectRatioIcon();

            // Build unified HttpDataSource with headers & timeouts
            HashMap<String, String> headers = new HashMap<>();
            if (refererHeader != null && !refererHeader.isEmpty()) headers.put("Referer", refererHeader);
            if (originHeader != null && !originHeader.isEmpty()) headers.put("Origin", originHeader);

            DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setDefaultRequestProperties(headers)
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(20000)
                    .setReadTimeoutMs(20000);

            String effectiveUserAgent = (userAgentHeader != null && !userAgentHeader.isEmpty())
                    ? userAgentHeader
                    : "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
            httpDataSourceFactory.setUserAgent(effectiveUserAgent);

            if (mpdLink == null || Objects.equals(mpdLink, "null")) {
                boolean isLs = "ls".equalsIgnoreCase(linkType) || (spHandler.mediaUri != null && (spHandler.mediaUri.toString().contains("ls_b64") || spHandler.mediaUri.toString().contains(":8089")));
                Uri playbackUri = isLs ? resolveLsTokenIfPresent(spHandler.mediaUri) : spHandler.mediaUri;
                String uriStr = playbackUri != null ? playbackUri.toString().toLowerCase() : "";
                boolean isHls = "0".equals(linkType) || "stream".equalsIgnoreCase(linkType) || "hls".equalsIgnoreCase(linkType) || isLs
                        || uriStr.contains(".m3u8") || uriStr.contains(".php") || uriStr.contains(".js") || uriStr.contains(".json") || uriStr.contains("=m3u8");

                if (refererHeader != null || originHeader != null || userAgentHeader != null || isHls) {
                    MediaSource mediaSource;
                    if (isHls) {
                        mediaSource = new HlsMediaSource.Factory(httpDataSourceFactory)
                                .setAllowChunklessPreparation(false)
                                .createMediaSource(new MediaItem.Builder()
                                        .setUri(playbackUri)
                                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                                        .build());
                    } else {
                        mediaSource = new DefaultMediaSourceFactory(httpDataSourceFactory)
                                .createMediaSource(MediaItem.fromUri(playbackUri));
                    }
                    exoPlayer.setMediaSource(mediaSource, isHls ? C.TIME_UNSET : spHandler.getPosition());
                } else {
                    MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
                            .setUri(playbackUri)
                            .setMimeType(spHandler.mediaType);
                    String title;
                    if (apiTitle != null) {
                        title = apiTitle;
                    } else {
                        title = Utility.getFileName(PlayerActivity.this, playbackUri);
                    }
                    if (title != null) {
                        final MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                                .setTitle(title)
                                .setDisplayTitle(title)
                                .build();
                        mediaItemBuilder.setMediaMetadata(mediaMetadata);
                    }
                    if (apiAccess && apiSubs.size() > 0) {
                        mediaItemBuilder.setSubtitleConfigurations(apiSubs);
                    } else if (spHandler.subtitleUri != null && Utility.fileExists(this, spHandler.subtitleUri)) {
                        MediaItem.SubtitleConfiguration subtitle = SubtitleUtility.buildSubtitle(this, spHandler.subtitleUri, null, true);
                        mediaItemBuilder.setSubtitleConfigurations(Collections.singletonList(subtitle));
                    }
                    exoPlayer.setMediaItem(mediaItemBuilder.build(), spHandler.getPosition());
                }
            } else {
                DashMediaSource.Factory dashMediaSourceFactory = new DashMediaSource.Factory(httpDataSourceFactory);
                if (mpdKey == null || Objects.equals(mpdKey, "null") || !mpdKey.contains(":")) {
                    MediaSource mediaSource = dashMediaSourceFactory.createMediaSource(MediaItem.fromUri(Uri.parse(mpdLink)));
                    exoPlayer.setMediaSource(mediaSource);
                } else {
                    // Your MPD URL and Clear Key
                    String[] parts = mpdKey.split(":", 2);
                    String KEY_ID_HEX = parts[0];
                    String CLEAR_KEY_HEX = parts[1];
                    String KEY_ID = hexToBase64UrlSafe(KEY_ID_HEX); // Must be URL-safe Base64
                    String CLEAR_KEY = hexToBase64UrlSafe(CLEAR_KEY_HEX);
                    // Use Widevine UUID (standard for DASH)
                    UUID drmSchemeUuid = C.CLEARKEY_UUID;
                    // Create DRM session manager with our ClearKey callback
                    DefaultDrmSessionManager drmSessionManager = new DefaultDrmSessionManager.Builder()
                            .setUuidAndExoMediaDrmProvider(drmSchemeUuid, FrameworkMediaDrm.DEFAULT_PROVIDER)
                            .build(new ClearKeyMediaDrmCallback(KEY_ID, CLEAR_KEY));
                    // Build DASH media source
                    dashMediaSourceFactory.setDrmSessionManagerProvider(mediaItem -> drmSessionManager);
                    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(mpdLink));
                    DashMediaSource dashMediaSource = dashMediaSourceFactory.createMediaSource(mediaItem);
                    exoPlayer.setMediaSource(dashMediaSource);
                }
            }

            if (loudnessEnhancer != null) {
                loudnessEnhancer.release();
            }
            try {
                loudnessEnhancer = new LoudnessEnhancer(exoPlayer.getAudioSessionId());
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            notifyAudioSessionUpdate(true);

            videoLoading = true;

            updateVideoLoading(true);

            if (spHandler.getPosition() == 0L || apiAccess) {
                play = true;
            }

//            if (apiTitle != null) {
//                titleView.setText(apiTitle);
//            } else {
//                titleView.setText(Utility.getFileName(this, mSPHandler.mediaUri));
//            }
//            titleView.setVisibility(View.VISIBLE);

            if (videoTitleForVideo != null) {
                videoTitleTxt.setText(videoTitleForVideo);
            } else {
                videoTitleTxt.setText(Utility.getFileName(this, spHandler.mediaUri));
            }
            videoTitleTxt.setVisibility(View.VISIBLE);


            updateButtons(true);

            ((DoubleTapPV) customPlayerView).setDoubleTapEnabled(true);

            if (!apiAccess) {
                if (nextUriThread != null) {
                    nextUriThread.interrupt();
                }
                nextUri = null;
                nextUriThread = new Thread(() -> {
                    Uri uri = findNext();
                    if (!Thread.currentThread().isInterrupted()) {
                        nextUri = uri;
                    }
                });
                nextUriThread.start();
            }

            Utility.markChapters(this, spHandler.mediaUri, playerControlView);

            exoPlayer.setHandleAudioBecomingNoisy(!isTvBox);
//            mediaSession.setActive(true);
        } else {
            customPlayerView.showController();
        }

        exoPlayer.addListener(playerListener);
        exoPlayer.prepare();

        if (restorePlayState) {
            restorePlayState = false;
            customPlayerView.showController();
            customPlayerView.setControllerShowTimeoutMs(PlayerActivity.CONTROLLER_TIMEOUT);
            exoPlayer.setPlayWhenReady(true);
        }


        //leo
        if (isVideoLoop) {
            customPlayerView.setRepeatToggleModes(Player.REPEAT_MODE_ONE);
        }
        handleBannerAD();
    }

    public static String hexToBase64UrlSafe(String input) {
        if (input == null) return "";
        // Base64 URL-safe regex: only A-Z a-z 0-9 _ -
        if (input.matches("^[A-Za-z0-9_-]{11,24}$")) {
            // Looks like Base64URL, return as is
            return input;
        }
        // Otherwise treat as hex (convert exactly 32 hex chars to 16 bytes)
        String cleaned = input.replaceAll("[^0-9a-fA-F]", "");
        while (cleaned.length() < 32) cleaned = "0" + cleaned;
        if (cleaned.length() > 32) cleaned = cleaned.substring(cleaned.length() - 32);

        byte[] bytes = new byte[16];
        for (int i = 0; i < 32; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(cleaned.charAt(i), 16) << 4)
                    + Character.digit(cleaned.charAt(i + 1), 16));
        }
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private static volatile String sCachedPublicIp = null;
    private static volatile long sIpFetchTimestamp = 0;

    public static Uri resolveLsTokenIfPresent(Uri originalUri) {
        if (originalUri == null) return null;

        String lsKey = null;
        String lsB64 = originalUri.getQueryParameter("ls_b64");
        if (lsB64 != null && !lsB64.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(lsB64, Base64.URL_SAFE | Base64.DEFAULT);
                lsKey = new String(decoded, StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }

        if (lsKey == null || lsKey.isEmpty()) {
            String uriStr = originalUri.toString();
            if (uriStr.contains("ls_key=")) {
                int start = uriStr.indexOf("ls_key=") + 7;
                int end = uriStr.indexOf("&", start);
                if (end == -1) end = uriStr.length();
                lsKey = uriStr.substring(start, end);
            }
        }

        // If no dynamic key is found, return original URI as-is
        if (lsKey == null || lsKey.isEmpty()) {
            return originalUri;
        }

        try {
            Uri.Builder builder = originalUri.buildUpon().clearQuery();
            for (String paramName : originalUri.getQueryParameterNames()) {
                if (!"ls_key".equals(paramName) && !"ls_b64".equals(paramName) && !"token".equals(paramName)) {
                    for (String val : originalUri.getQueryParameters(paramName)) {
                        builder.appendQueryParameter(paramName, val);
                    }
                }
            }
            Uri baseUri = builder.build();
            String baseUrl = baseUri.toString();

            String path = baseUri.getPath();
            String channelId = "";
            if (path != null) {
                String[] segments = path.split("/");
                for (int i = segments.length - 1; i >= 0; i--) {
                    if (!segments[i].isEmpty() && !segments[i].contains(".")) {
                        channelId = segments[i];
                        break;
                    }
                }
            }
            if (channelId.isEmpty()) {
                String[] urlParts = baseUrl.split("/");
                if (urlParts.length >= 2) {
                    channelId = urlParts[urlParts.length - 2];
                }
            }

            long now = System.currentTimeMillis();
            if (sCachedPublicIp == null || (now - sIpFetchTimestamp) > 10 * 60 * 1000) {
                android.os.StrictMode.ThreadPolicy oldPolicy = android.os.StrictMode.getThreadPolicy();
                try {
                    android.os.StrictMode.setThreadPolicy(new android.os.StrictMode.ThreadPolicy.Builder().permitAll().build());
                    String ip = fetchPublicIp();
                    if (ip != null && !ip.trim().isEmpty()) {
                        sCachedPublicIp = ip.trim();
                        sIpFetchTimestamp = now;
                    }
                } finally {
                    android.os.StrictMode.setThreadPolicy(oldPolicy);
                }
            }

            String clientIp = sCachedPublicIp != null ? sCachedPublicIp : "127.0.0.1";
            long nowSec = System.currentTimeMillis() / 1000L;
            long expireSec = nowSec + 77L;

            String payload = channelId + lsKey + nowSec + clientIp;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(payload.getBytes(StandardCharsets.ISO_8859_1));

            StringBuilder hexSb = new StringBuilder();
            for (byte b : hashBytes) {
                int high = (b >>> 4) & 15;
                hexSb.append((char) (high < 10 ? high + '0' : high - 10 + 'a'));
                int low = b & 15;
                hexSb.append((char) (low < 10 ? low + '0' : low - 10 + 'a'));
            }
            String tokenHex = hexSb.toString();
            String tokenParam = tokenHex + "-" + expireSec + "-" + nowSec;
            String sep = baseUrl.contains("?") ? "&" : "?";
            String signedUrl = baseUrl + sep + "token=" + tokenParam;

            return Uri.parse(signedUrl);
        } catch (Exception e) {
            return originalUri;
        }
    }

    private static String fetchPublicIp() {
        String[] ipServices = new String[] {
                "https://api4.ipify.org",
                "https://ip-api.streamingucms.com/",
                "https://ipv4.icanhazip.com",
                "https://ifconfig.me/ip",
                "https://api.ipify.org"
        };
        for (String service : ipServices) {
            try {
                URL url = new URL(service);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                if (conn.getResponseCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String ip = reader.readLine();
                        if (ip != null && !ip.trim().isEmpty()) {
                            return ip.trim();
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void savePlayerValues() {
        if (exoPlayer != null) {
            spHandler.updateBrightness(brightnessControlHandler.currentBrightnessLevel);
//            mSPHandler.updateOrientation();

//            if (haveMedia) {
//                // Prevent overwriting temporarily inaccessible media position
//                if (player.isCurrentMediaItemSeekable()) {
//                    mSPHandler.updatePosition(player.getCurrentPosition());
//                }
//                mSPHandler.updateMeta(getSelectedTrack(C.TRACK_TYPE_AUDIO),
//                        getSelectedTrack(C.TRACK_TYPE_TEXT),
//                        playerView.getResizeMode(),
//                        playerView.getVideoSurfaceView().getScaleX(),
//                        player.getPlaybackParameters().speed);
//            }
        }
    }

    public void releasePlayer() {
        releasePlayer(true);
    }

    public void releasePlayer(boolean save) {
        if (save) {
            savePlayerValues();
        }

        if (exoPlayer != null) {
            notifyAudioSessionUpdate(false);

//            mediaSession.setActive(false);
            if (mediaSession != null) {
                mediaSession.release();
            }

            if (exoPlayer.isPlaying() && restorePlayStateAllowed) {
                restorePlayState = true;
            }
            exoPlayer.removeListener(playerListener);
            exoPlayer.clearMediaItems();
            exoPlayer.release();
            exoPlayer = null;
        }
//        titleView.setVisibility(View.GONE);
        videoTitleTxt.setVisibility(View.GONE);
        updateButtons(false);
    }

    private class PlayerListener implements Player.Listener {
        @Override
        public void onAudioSessionIdChanged(int audioSessionId) {
            if (loudnessEnhancer != null) {
                loudnessEnhancer.release();
            }
            try {
                loudnessEnhancer = new LoudnessEnhancer(audioSessionId);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            notifyAudioSessionUpdate(true);
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            customPlayerView.setKeepScreenOn(isPlaying);

//            if (Utility.isPiPSupported(PlayerActivity.this)) {
//                if (isPlaying) {
//                    updatePictureInPictureActions(R.drawable.ic_pause_24dp, androidx.media3.ui.R.string.exo_controls_pause_description, CONTROL_TYPE_PAUSE, REQUEST_PAUSE);
//                } else {
//                    updatePictureInPictureActions(R.drawable.ic_play_arrow_24dp, androidx.media3.ui.R.string.exo_controls_play_description, CONTROL_TYPE_PLAY, REQUEST_PLAY);
//                }
//            }

            if (!isScrubbing) {
                if (isPlaying) {
                    if (shortControllerTimeout) {
                        customPlayerView.setControllerShowTimeoutMs(CONTROLLER_TIMEOUT / 3);
                        shortControllerTimeout = false;
                        restoreControllerTimeout = true;
                    } else {
                        customPlayerView.setControllerShowTimeoutMs(CONTROLLER_TIMEOUT);
                    }
                } else {
                    customPlayerView.setControllerShowTimeoutMs(-1);
                }
            }

            if (!isPlaying) {
                PlayerActivity.locked = false;
            }
        }

        @SuppressLint("SourceLockedOrientationActivity")
        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_BUFFERING) {
                updateVideoLoading(true);
            }

            boolean isNearEnd = false;
            final long duration = exoPlayer.getDuration();
            if (duration != C.TIME_UNSET) {
                final long position = exoPlayer.getCurrentPosition();
                if (position + 4000 >= duration) {
                    isNearEnd = true;
                } else {
                    // Last chapter is probably "Credits" chapter
                    final int chapters = chapterStarts.length;
                    if (chapters > 1) {
                        final long lastChapter = chapterStarts[chapters - 1];
                        if (duration - lastChapter < duration / 10 && position > lastChapter) {
                            isNearEnd = true;
                        }
                    }
                }
            }
            setEndControlsVisible(haveMedia && (state == Player.STATE_ENDED || isNearEnd));

            if (state == Player.STATE_READY) {
                frameRendered = true;

                if (videoLoading) {
                    videoLoading = false;

                    final Format format = exoPlayer.getVideoFormat();

                    if (format != null) {
                        customPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                        updatebuttonAspectRatioIcon();
                        resetPlayerHideCallbacks();
                        updateButtonRotation();
                        updateVideoSubtitleViewMargin(format);
                    }

                    if (duration != C.TIME_UNSET && duration > TimeUnit.MINUTES.toMillis(20)) {
                        customTimeBar.setKeyTimeIncrement(TimeUnit.MINUTES.toMillis(1));
                    } else {
                        customTimeBar.setKeyCountIncrement(20);
                    }

                    boolean switched = false;
                    if (spHandler.frameRateMatching) {
                        if (play) {
                            if (displayManager == null) {
                                displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
                            }
                            if (displayListener == null) {
                                displayListener = new DisplayManager.DisplayListener() {
                                    @Override
                                    public void onDisplayAdded(int displayId) {

                                    }

                                    @Override
                                    public void onDisplayRemoved(int displayId) {

                                    }

                                    @Override
                                    public void onDisplayChanged(int displayId) {
                                        if (play) {
                                            play = false;
                                            displayManager.unregisterDisplayListener(this);
                                            if (exoPlayer != null) {
                                                exoPlayer.play();
                                            }
                                            if (customPlayerView != null) {
                                                customPlayerView.hideController();
                                            }
                                        }
                                    }
                                };
                            }
                            displayManager.registerDisplayListener(displayListener, null);
                        }
                        switched = Utility.switchFrameRate(PlayerActivity.this, spHandler.mediaUri, play);
                    }
                    if (!switched) {
                        if (displayManager != null) {
                            displayManager.unregisterDisplayListener(displayListener);
                        }
                        if (play) {
                            play = false;
                            exoPlayer.play();
                            customPlayerView.hideController();
                        }
                    }


                    if (spHandler.speed <= 0.99f || spHandler.speed >= 1.01f) {
                        exoPlayer.setPlaybackSpeed(spHandler.speed);
                    }
                    if (!apiAccess) {
                        setSelectedTracks(spHandler.subtitleTrackId, spHandler.audioTrackId);
                    }
                }
                //leo
                hideSlidingMessage();
                updateVideoLoading(false);

            } else if (state == Player.STATE_ENDED) {
                playbackFinished = true;
//                Toast.makeText(PlayerActivity.this, "Video Ended! Retrying...", Toast.LENGTH_SHORT).show();
                dispatchPlayPause();
                if (apiAccess) {
                    finish();
                }
            }
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            updateVideoLoading(false);
            if (error instanceof ExoPlaybackException) {
                final ExoPlaybackException exoPlaybackException = (ExoPlaybackException) error;
                if (exoPlaybackException.type == ExoPlaybackException.TYPE_SOURCE) {
                    //leo
                    showSlidingMessage("Video cannot be played. Please try another link or check your internet connection.");
                    //
                    releasePlayer(false);
                    return;
                }
                if (controllerVisible && controllerVisibleFully) {
                    showErrorMessage(exoPlaybackException);
                } else {
                    errorToShow = exoPlaybackException;
                }
            }
        }

        @Override
        public void onRenderedFirstFrame() {
            firstFrameRendered = true;
            blockerHasBeenShown = true;
            if (showCopyrightBlocker) {
                runOnUiThread(() -> {
                    if (customPlayerView != null) {
                        updateCopyrightBlockerBounds(customPlayerView.getWidth(), customPlayerView.getHeight());
                        applyCopyrightBlockerBlur(false);
                    }
                });
            }
        }

        @Override
        public void onVideoSizeChanged(androidx.media3.common.VideoSize videoSize) {
            if (showCopyrightBlocker && firstFrameRendered) {
                runOnUiThread(() -> {
                    if (customPlayerView != null) {
                        updateCopyrightBlockerBounds(customPlayerView.getWidth(), customPlayerView.getHeight());
                        applyCopyrightBlockerBlur(true);
                    }
                });
            }
        }
    }

    private void showSlidingMessage(String message) {
        runOnUiThread(() -> {
            slidingMessage.setText(message);
            slidingMessage.setVisibility(View.VISIBLE);
            slidingMessage.setEllipsize(null); // Disable truncation

            // Wait for layout to complete
            slidingMessage.post(() -> {
                // Get EXACT text width (pixel-perfect)
                float textWidth = slidingMessage.getPaint().measureText(message);
                int screenWidth = getResources().getDisplayMetrics().widthPixels;

                if (textWidth > screenWidth) {
                    // Start off-screen right
                    slidingMessage.setTranslationX(screenWidth);

                    // Animate to left edge
                    ObjectAnimator animator = ObjectAnimator.ofFloat(
                            slidingMessage,
                            "translationX",
                            screenWidth,
                            -textWidth
                    );
                    animator.setDuration(20000); // 20 seconds
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setRepeatCount(ObjectAnimator.INFINITE);
                    animator.start();
                } else {
                    // Center if text fits
                    slidingMessage.setTranslationX((screenWidth - textWidth) / 2);
                }
            });
        });
    }

    public void hideSlidingMessage() {
        runOnUiThread(() -> {
            slidingMessage.setVisibility(View.GONE);
            slidingMessage.setSelected(false);
        });
    }

    private void enableRotation() {
        try {
            if (Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 0) {
                Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                restoreOrientationLock = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFile(Uri pickerInitialUri) {
        final int targetSdkVersion = getApplicationContext().getApplicationInfo().targetSdkVersion;
        if ((isTvBox && Build.VERSION.SDK_INT >= 30 && targetSdkVersion >= 30 && spHandler.fileAccess.equals("auto")) || spHandler.fileAccess.equals("mediastore")) {
            Intent intent = new Intent(this, MediaStoreChooserAct.class);
            startActivityForResult(intent, REQUEST_CHOOSER_VIDEO_MEDIA_STORE);
        } else if ((isTvBox && spHandler.fileAccess.equals("auto")) || spHandler.fileAccess.equals("legacy")) {
            Utility.alternativeChooser(this, pickerInitialUri, true);
        } else {
            enableRotation();

            if (pickerInitialUri == null || Utility.isSupportedNetworkUri(pickerInitialUri)) {
                pickerInitialUri = Utility.getMoviesFolderUri();
            }

            final Intent intent = createBaseFileIntent(Intent.ACTION_OPEN_DOCUMENT, pickerInitialUri);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, Utility.supportedMimeTypesVideo);

            if (Build.VERSION.SDK_INT < 30) {
                final ComponentName systemComponentName = Utility.getSystemComponent(this, intent);
                if (systemComponentName != null) {
                    intent.setComponent(systemComponentName);
                }
            }

            safelyStartActivityForResult(intent, REQUEST_CHOOSER_VIDEO);
        }
    }

    private void loadSubtitleFileForVideo(Uri pickerInitialUri) {
        Toast.makeText(PlayerActivity.this, R.string.open_subtitles, Toast.LENGTH_SHORT).show();
        final int targetSdkVersion = getApplicationContext().getApplicationInfo().targetSdkVersion;
        if ((isTvBox && Build.VERSION.SDK_INT >= 30 && targetSdkVersion >= 30 && spHandler.fileAccess.equals("auto")) || spHandler.fileAccess.equals("mediastore")) {
            Intent intent = new Intent(this, MediaStoreChooserAct.class);
            intent.putExtra(MediaStoreChooserAct.SUBTITLES, true);
            startActivityForResult(intent, REQUEST_CHOOSER_SUBTITLE_MEDIA_STORE);
        } else if ((isTvBox && spHandler.fileAccess.equals("auto")) || spHandler.fileAccess.equals("legacy")) {
            Utility.alternativeChooser(this, pickerInitialUri, false);
        } else {
            enableRotation();

            final Intent intent = createBaseFileIntent(Intent.ACTION_OPEN_DOCUMENT, pickerInitialUri);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            final String[] supportedMimeTypes = {
                    MimeTypes.APPLICATION_SUBRIP,
                    MimeTypes.TEXT_SSA,
                    MimeTypes.TEXT_VTT,
                    MimeTypes.APPLICATION_TTML,
                    "text/*",
                    "application/octet-stream"
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes);

            if (Build.VERSION.SDK_INT < 30) {
                final ComponentName systemComponentName = Utility.getSystemComponent(this, intent);
                if (systemComponentName != null) {
                    intent.setComponent(systemComponentName);
                }
            }

            safelyStartActivityForResult(intent, REQUEST_CHOOSER_SUBTITLE);
        }
    }

    private void requestDirectoryAccess() {
        enableRotation();
        final Intent intent = createBaseFileIntent(Intent.ACTION_OPEN_DOCUMENT_TREE, Utility.getMoviesFolderUri());
        safelyStartActivityForResult(intent, REQUEST_CHOOSER_SCOPE_DIR);
    }

    private Intent createBaseFileIntent(final String action, final Uri initialUri) {
        final Intent intent = new Intent(action);

        // http://stackoverflow.com/a/31334967/1615876
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);

        if (Build.VERSION.SDK_INT >= 26 && initialUri != null) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
        }

        return intent;
    }

    void safelyStartActivityForResult(final Intent intent, final int code) {
        if (intent.resolveActivity(getPackageManager()) == null)
            showSnackMessage(getText(R.string.error_files_missing).toString(), intent.toString());
        else
            startActivityForResult(intent, code);
    }

    private TrackGroup getTrackGroupFromFormatId(int trackType, String id) {
        if ((id == null && trackType == C.TRACK_TYPE_AUDIO) || exoPlayer == null) {
            return null;
        }
        for (Tracks.Group group : exoPlayer.getCurrentTracks().getGroups()) {
            if (group.getType() == trackType) {
                final TrackGroup trackGroup = group.getMediaTrackGroup();
                final Format format = trackGroup.getFormat(0);
                if (Objects.equals(id, format.id)) {
                    return trackGroup;
                }
            }
        }
        return null;
    }

    public void setSelectedTracks(final String subtitleId, final String audioId) {
        if ("#none".equals(subtitleId)) {
            if (defaultTrackSelector == null) {
                return;
            }
            defaultTrackSelector.setParameters(defaultTrackSelector.buildUponParameters().setDisabledTextTrackSelectionFlags(C.SELECTION_FLAG_DEFAULT | C.SELECTION_FLAG_FORCED));
        }

        TrackGroup subtitleGroup = getTrackGroupFromFormatId(C.TRACK_TYPE_TEXT, subtitleId);
        TrackGroup audioGroup = getTrackGroupFromFormatId(C.TRACK_TYPE_AUDIO, audioId);

        TrackSelectionParameters.Builder overridesBuilder = new TrackSelectionParameters.Builder(this);
        TrackSelectionOverride trackSelectionOverride = null;
        final List<Integer> tracks = new ArrayList<>();
        tracks.add(0);
        if (subtitleGroup != null) {
            trackSelectionOverride = new TrackSelectionOverride(subtitleGroup, tracks);
            overridesBuilder.addOverride(trackSelectionOverride);
        }
        if (audioGroup != null) {
            trackSelectionOverride = new TrackSelectionOverride(audioGroup, tracks);
            overridesBuilder.addOverride(trackSelectionOverride);
        }

        if (exoPlayer != null) {
            TrackSelectionParameters.Builder trackSelectionParametersBuilder = exoPlayer.getTrackSelectionParameters().buildUpon();
            if (trackSelectionOverride != null) {
                trackSelectionParametersBuilder.setOverrideForType(trackSelectionOverride);
            }
            exoPlayer.setTrackSelectionParameters(trackSelectionParametersBuilder.build());
        }
    }

    private boolean hasOverrideType(final int trackType) {
        TrackSelectionParameters trackSelectionParameters = exoPlayer.getTrackSelectionParameters();
        for (TrackSelectionOverride override : trackSelectionParameters.overrides.values()) {
            if (override.getType() == trackType)
                return true;
        }
        return false;
    }

    public String getSelectedTrack(final int trackType) {
        if (exoPlayer == null) {
            return null;
        }
        Tracks tracks = exoPlayer.getCurrentTracks();

        // Disabled (e.g. selected subtitle "None" - different than default)
        if (!tracks.isTypeSelected(trackType)) {
            return "#none";
        }

        // Audio track set to "Auto"
        if (trackType == C.TRACK_TYPE_AUDIO) {
            if (!hasOverrideType(C.TRACK_TYPE_AUDIO)) {
                return null;
            }
        }

        for (Tracks.Group group : tracks.getGroups()) {
            if (group.isSelected() && group.getType() == trackType) {
                Format format = group.getMediaTrackGroup().getFormat(0);
                return format.id;
            }
        }

        return null;
    }

    void setVideoSubtitleTextSize() {
        setVideoSubtitleTextSize(getResources().getConfiguration().orientation);
    }

    void setVideoSubtitleTextSize(final int orientation) {
        // Tweak text size as fraction size doesn't work well in portrait
        final SubtitleView subtitleView = customPlayerView.getSubtitleView();
        if (subtitleView != null) {
            final float size;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                size = SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * subtitlesScale;
            } else {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                float ratio = ((float) metrics.heightPixels / (float) metrics.widthPixels);
                if (ratio < 1)
                    ratio = 1 / ratio;
                size = SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * subtitlesScale / ratio;
            }

            subtitleView.setFractionalTextSize(size);
        }
    }

    void updateVideoSubtitleViewMargin() {
        if (exoPlayer == null) {
            return;
        }

        updateVideoSubtitleViewMargin(exoPlayer.getVideoFormat());
    }

    // Set margins to fix PGS aspect as subtitle view is outside of content frame
    void updateVideoSubtitleViewMargin(Format format) {
        if (format == null) {
            return;
        }

        final Rational aspectVideo = Utility.getRational(format);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        final Rational aspectDisplay = new Rational(metrics.widthPixels, metrics.heightPixels);

        int marginHorizontal = 0;
        int marginVertical = 0;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (aspectDisplay.floatValue() > aspectVideo.floatValue()) {
                // Left & right bars
                int videoWidth = metrics.heightPixels / aspectVideo.getDenominator() * aspectVideo.getNumerator();
                marginHorizontal = (metrics.widthPixels - videoWidth) / 2;
            }
        }

        Utility.setViewParams(customPlayerView.getSubtitleView(), 0, 0, 0, 0,
                marginHorizontal, marginVertical, marginHorizontal, marginVertical);
    }

    void setVideoSubtitleTextSizePiP() {
        final SubtitleView subtitleView = customPlayerView.getSubtitleView();
        if (subtitleView != null)
            subtitleView.setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 2);
    }

//    @TargetApi(26)
//    boolean updatePictureInPictureActions(final int iconId, final int resTitle, final int controlType, final int requestCode) {
//        try {
//            final ArrayList<RemoteAction> actions = new ArrayList<>();
//            final PendingIntent intent = PendingIntent.getBroadcast(PlayerActivity.this, requestCode,
//                    new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType), PendingIntent.FLAG_IMMUTABLE);
//            final Icon icon = Icon.createWithResource(PlayerActivity.this, iconId);
//            final String title = getString(resTitle);
//            actions.add(new RemoteAction(icon, title, title, intent));
//            ((PictureInPictureParams.Builder) pictureInPictureParamsBuilder).setActions(actions);
//            setPictureInPictureParams(((PictureInPictureParams.Builder) pictureInPictureParamsBuilder).build());
//            return true;
//        } catch (IllegalStateException e) {
//            // On Samsung devices with Talkback active:
//            // Caused by: java.lang.IllegalStateException: setPictureInPictureParams: Device doesn't support picture-in-picture mode.
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    private boolean isInPip() {
//        if (!Utility.isPiPSupported(this))
//            return false;
//        return isInPictureInPictureMode();
//    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

//        if (!isInPip()) {
//            setVideoSubtitleTextSize(newConfig.orientation);
//        }

        updateVideoSubtitleViewMargin();

        updateButtonRotation();

        if (showCopyrightBlocker && customPlayerView != null) {
            updateCopyrightBlockerBounds(customPlayerView.getWidth(), customPlayerView.getHeight());
        }
    }

    void showErrorMessage(ExoPlaybackException error) {
        final String errorGeneral = error.getLocalizedMessage();
        String errorDetailed;

        switch (error.type) {
            case ExoPlaybackException.TYPE_SOURCE:
                errorDetailed = error.getSourceException().getLocalizedMessage();
                break;
            case ExoPlaybackException.TYPE_RENDERER:
                errorDetailed = error.getRendererException().getLocalizedMessage();
                break;
            case ExoPlaybackException.TYPE_UNEXPECTED:
                errorDetailed = error.getUnexpectedException().getLocalizedMessage();
                break;
            case ExoPlaybackException.TYPE_REMOTE:
            default:
                errorDetailed = errorGeneral;
                break;
        }

        showSnackMessage(errorGeneral, errorDetailed);
    }

    void showSnackMessage(final String textPrimary, final String textSecondary) {
        snackbar = Snackbar.make(coordinatorLayout, textPrimary, Snackbar.LENGTH_LONG);
        if (textSecondary != null) {
            snackbar.setAction(R.string.error_details, v -> {
                final AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
                builder.setMessage(textSecondary);
                builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
                final AlertDialog dialog = builder.create();
                dialog.show();
            });
        }
        snackbar.setAnchorView(R.id.exo_bottom_bar);
        snackbar.show();
    }

    void reportScrubbing(long position) {
        final long diff = position - scrubbingStart;
        if (Math.abs(diff) > 1000) {
            scrubbingNoticeable = true;
        }
        if (scrubbingNoticeable) {
            customPlayerView.clearIcon();
            customPlayerView.setCustomErrorMessage(Utility.formatMilisSign(diff));
        }
        if (frameRendered) {
            frameRendered = false;
            if (exoPlayer != null) {
                exoPlayer.seekTo(position);
            }
        }
    }

    void updateVideoSubtitleStyle(final Context context) {
        final CaptioningManager captioningManager = (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
        final SubtitleView subtitleView = customPlayerView.getSubtitleView();
        final boolean isTablet = Utility.isTablet(context);
        subtitlesScale = SubtitleUtility.normalizeFontScale(captioningManager.getFontScale(), isTvBox || isTablet);
        if (subtitleView != null) {
            final CaptioningManager.CaptionStyle userStyle = captioningManager.getUserStyle();
            final CaptionStyleCompat userStyleCompat = CaptionStyleCompat.createFromCaptionStyle(userStyle);
            final CaptionStyleCompat captionStyle = new CaptionStyleCompat(
                    userStyle.hasForegroundColor() ? userStyleCompat.foregroundColor : Color.WHITE,
                    userStyle.hasBackgroundColor() ? userStyleCompat.backgroundColor : Color.TRANSPARENT,
                    userStyle.hasWindowColor() ? userStyleCompat.windowColor : Color.TRANSPARENT,
                    userStyle.hasEdgeType() ? userStyleCompat.edgeType : CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                    userStyle.hasEdgeColor() ? userStyleCompat.edgeColor : Color.BLACK,
                    userStyleCompat.typeface != null ? userStyleCompat.typeface : Typeface.DEFAULT_BOLD);
            subtitleView.setStyle(captionStyle);

            if (captioningManager.isEnabled()) {
                // Do not apply embedded style as currently the only supported color style is PrimaryColour
                // Embedded style config
                // This may result in poorly visible text (depending on user's selected edgeColor)
                // The same can happen with style provided using setStyle but enabling CaptioningManager should be a way to change the behavior
                subtitleView.setApplyEmbeddedStyles(false);
            } else {
                subtitleView.setApplyEmbeddedStyles(true);
            }

            subtitleView.setBottomPaddingFraction(SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION * 2f / 3f);
        }
        setVideoSubtitleTextSize();
    }

    void searchVideoSubtitles() {
        if (spHandler.mediaUri == null)
            return;

        if (Utility.isSupportedNetworkUri(spHandler.mediaUri) && Utility.isProgressiveContainerUri(spHandler.mediaUri)) {
            SubtitleUtility.clearCache(this);
            if (SubtitleFinderHandler.isUriCompatible(spHandler.mediaUri)) {
                subtitleFinderHandler = new SubtitleFinderHandler(PlayerActivity.this, spHandler.mediaUri);
                subtitleFinderHandler.start();
            }
            return;
        }

        if (spHandler.scopeUri != null || isTvBox) {
            DocumentFile video = null;
            File videoRaw = null;
            final String scheme = spHandler.mediaUri.getScheme();

            if (spHandler.scopeUri != null) {
                if ("com.android.externalstorage.documents".equals(spHandler.mediaUri.getHost()) ||
                        "org.courville.nova.provider".equals(spHandler.mediaUri.getHost())) {
                    // Fast search based on path in uri
                    video = SubtitleUtility.findUriInScope(this, spHandler.scopeUri, spHandler.mediaUri);
                } else {
                    // Slow search based on matching metadata, no path in uri
                    // Provider "com.android.providers.media.documents" when using "Videos" tab in file picker
                    DocumentFile fileScope = DocumentFile.fromTreeUri(this, spHandler.scopeUri);
                    DocumentFile fileMedia = DocumentFile.fromSingleUri(this, spHandler.mediaUri);
                    video = SubtitleUtility.findDocInScope(fileScope, fileMedia);
                }
            } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                videoRaw = new File(spHandler.mediaUri.getSchemeSpecificPart());
                video = DocumentFile.fromFile(videoRaw);
            }

            if (video != null) {
                DocumentFile subtitle = null;
                if (spHandler.scopeUri != null) {
                    subtitle = SubtitleUtility.findSubtitle(video);
                } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                    File parentRaw = videoRaw.getParentFile();
                    DocumentFile dir = DocumentFile.fromFile(parentRaw);
                    subtitle = SubtitleUtility.findSubtitle(video, dir);
                }

                if (subtitle != null) {
                    handleVideoSubtitles(subtitle.getUri());
                }
            }
        }
    }

    Uri findNext() {
        // TODO: Unify with searchSubtitles()
        if (spHandler.scopeUri != null || isTvBox) {
            DocumentFile video = null;
            File videoRaw = null;

            if (!isTvBox && spHandler.scopeUri != null) {
                if ("com.android.externalstorage.documents".equals(spHandler.mediaUri.getHost())) {
                    // Fast search based on path in uri
                    video = SubtitleUtility.findUriInScope(this, spHandler.scopeUri, spHandler.mediaUri);
                } else {
                    // Slow search based on matching metadata, no path in uri
                    // Provider "com.android.providers.media.documents" when using "Videos" tab in file picker
                    DocumentFile fileScope = DocumentFile.fromTreeUri(this, spHandler.scopeUri);
                    DocumentFile fileMedia = DocumentFile.fromSingleUri(this, spHandler.mediaUri);
                    video = SubtitleUtility.findDocInScope(fileScope, fileMedia);
                }
            } else if (isTvBox) {
                videoRaw = new File(spHandler.mediaUri.getSchemeSpecificPart());
                video = DocumentFile.fromFile(videoRaw);
            }

            if (video != null) {
                DocumentFile next;
                if (!isTvBox) {
                    next = SubtitleUtility.findNext(video);
                } else {
                    File parentRaw = videoRaw.getParentFile();
                    DocumentFile dir = DocumentFile.fromFile(parentRaw);
                    next = SubtitleUtility.findNext(video, dir);
                }
                if (next != null) {
                    return next.getUri();
                }
            }
        }
        return null;
    }

    void askForVideoScope(boolean loadSubtitlesOnCancel, boolean skipToNextOnCancel) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
        builder.setMessage(String.format(getString(R.string.request_scope), getString(R.string.app_name)));
        builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> requestDirectoryAccess()
        );
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            spHandler.markScopeAsked();
            if (loadSubtitlesOnCancel) {
                loadSubtitleFileForVideo(spHandler.mediaUri);
            }
            if (skipToNextOnCancel) {
                nextUri = findNext();
                if (nextUri != null) {
                    skipToNext();
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    void resetPlayerHideCallbacks() {
        if (haveMedia && exoPlayer != null && exoPlayer.isPlaying()) {
            // Keep controller UI visible - alternative to resetHideCallbacks()
            customPlayerView.setControllerShowTimeoutMs(PlayerActivity.CONTROLLER_TIMEOUT);
        }
    }

    private void updateVideoLoading(final boolean enableLoading) {
        if (enableLoading) {
            exoPlayPauseBtn.setVisibility(View.GONE);
            loadingProgressBar.setVisibility(View.VISIBLE);
        } else {
            loadingProgressBar.setVisibility(View.GONE);
            exoPlayPauseBtn.setVisibility(View.VISIBLE);
            if (focusPlay) {
                focusPlay = false;
                exoPlayPauseBtn.requestFocus();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onUserLeaveHint() {
//        if (spHandler != null && spHandler.autoPiP && exoPlayer != null && exoPlayer.isPlaying() && Utility.isPiPSupported(this))
//            enterVideoPiP();
//        else
        super.onUserLeaveHint();
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void enterVideoPiP() {
//        final AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
//        if (AppOpsManager.MODE_ALLOWED != appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(), getPackageName())) {
//            final Intent intent = new Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS", Uri.fromParts("package", getPackageName(), null));
//            if (intent.resolveActivity(getPackageManager()) != null) {
//                startActivity(intent);
//            }
//            return;
//        }
//
//        if (exoPlayer == null) {
//            return;
//        }
//
//        customPlayerView.setControllerAutoShow(false);
//        customPlayerView.hideController();
//
//        final Format format = exoPlayer.getVideoFormat();
//
//        if (format != null) {
//            // Test/disable on Android 11+
//            // TODO: Test/disable on Android 11+
//            final View videoSurfaceView = customPlayerView.getVideoSurfaceView();
//            if (videoSurfaceView instanceof SurfaceView) {
//                ((SurfaceView) videoSurfaceView).getHolder().setFixedSize(format.width, format.height);
//            }
//
//            Rational rational = Utility.getRational(format);
//            if (Build.VERSION.SDK_INT >= 33 &&
//                    getPackageManager().hasSystemFeature(FEATURE_EXPANDED_PICTURE_IN_PICTURE) &&
//                    (rational.floatValue() > rationalLimitWide.floatValue() || rational.floatValue() < rationalLimitTall.floatValue())) {
//                ((PictureInPictureParams.Builder) pictureInPictureParamsBuilder).setExpandedAspectRatio(rational);
//            }
//            if (rational.floatValue() > rationalLimitWide.floatValue())
//                rational = rationalLimitWide;
//            else if (rational.floatValue() < rationalLimitTall.floatValue())
//                rational = rationalLimitTall;
//
//            ((PictureInPictureParams.Builder) pictureInPictureParamsBuilder).setAspectRatio(rational);
//        }
//        enterPictureInPictureMode(((PictureInPictureParams.Builder) pictureInPictureParamsBuilder).build());
//    }

    void setEndControlsVisible(boolean visible) {
        final int deleteVisible = (visible && haveMedia && Utility.isDeletable(this, spHandler.mediaUri)) ? View.VISIBLE : View.INVISIBLE;
        final int nextVisible = (visible && haveMedia && (nextUri != null || (spHandler.askScope && !isTvBox))) ? View.VISIBLE : View.INVISIBLE;
        findViewById(R.id.delete).setVisibility(deleteVisible);
//        findViewById(R.id.next).setVisibility(nextVisible);
    }

    void askDeleteMedia() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
        builder.setMessage(getString(R.string.delete_query));
        builder.setPositiveButton(R.string.delete_confirmation, (dialogInterface, i) -> {
            releasePlayer();
            deleteMedia();
            if (nextUri == null) {
                haveMedia = false;
                setEndControlsVisible(false);
                customPlayerView.setControllerShowTimeoutMs(-1);
            } else {
                skipToNext();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    void deleteMedia() {
        try {
            if (ContentResolver.SCHEME_CONTENT.equals(spHandler.mediaUri.getScheme())) {
                DocumentsContract.deleteDocument(getContentResolver(), spHandler.mediaUri);
            } else if (ContentResolver.SCHEME_FILE.equals(spHandler.mediaUri.getScheme())) {
                final File file = new File(spHandler.mediaUri.getSchemeSpecificPart());
                if (file.canWrite()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatchPlayPause() {
        if (exoPlayer == null)
            return;

        @Player.State int state = exoPlayer.getPlaybackState();
        String methodName;
        if (state == Player.STATE_IDLE || state == Player.STATE_ENDED || !exoPlayer.getPlayWhenReady()) {
            methodName = "dispatchPlay";
            shortControllerTimeout = true;
        } else {
            methodName = "dispatchPause";
        }
        try {
            final Method method = PlayerControlView.class.getDeclaredMethod(methodName, Player.class);
            method.setAccessible(true);
            method.invoke(playerControlView, (Player) exoPlayer);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    void skipToNext() {
        if (nextUri != null) {
            releasePlayer();
            spHandler.updateMedia(this, nextUri, null);
            searchVideoSubtitles();
            initPlayer();
        }
    }

    void notifyAudioSessionUpdate(final boolean active) {
        final Intent intent = new Intent(active ? AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION
                : AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, exoPlayer.getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        if (active) {
            intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MOVIE);
        }
        try {
            sendBroadcast(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    void updateButtons(final boolean enable) {
//        if (piPBtn != null) {
//            Utility.setButtonEnabled(this, piPBtn, enable);
//        }
        Utility.setButtonEnabled(this, aspectRatioBtn, enable);
        if (isTvBox) {
            Utility.setButtonEnabled(this, exoSettingsBtn, true);
        } else {
            Utility.setButtonEnabled(this, exoSettingsBtn, enable);
        }
    }

    private void scaleStart() {
        isScaling = true;
        if (customPlayerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FILL) {
            customPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        }
        scaleFactor = customPlayerView.getVideoSurfaceView().getScaleX();
        customPlayerView.removeCallbacks(customPlayerView.textClearRunnable);
        customPlayerView.clearIcon();
        customPlayerView.setCustomErrorMessage((int) (scaleFactor * 100) + "%");
        customPlayerView.hideController();
        isScaleStarting = true;
    }

    private void scale(boolean up) {
        if (up) {
            scaleFactor += 0.01;
        } else {
            scaleFactor -= 0.01;
        }
        scaleFactor = Utility.normalizeScaleFactor(scaleFactor, customPlayerView.getScaleFit());
        customPlayerView.setScale(scaleFactor);
        customPlayerView.setCustomErrorMessage((int) (scaleFactor * 100) + "%");
    }

    private void scaleEnd() {
        isScaling = false;
        customPlayerView.postDelayed(customPlayerView.textClearRunnable, 200);
        if (exoPlayer != null && !exoPlayer.isPlaying()) {
            customPlayerView.showController();
        }
        if (Math.abs(customPlayerView.getScaleFit() - scaleFactor) < 0.01 / 2) {
            customPlayerView.setScale(1.f);
            customPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        }
        updatebuttonAspectRatioIcon();
    }

    private void updatebuttonAspectRatioIcon() {
        if (customPlayerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
            aspectRatioBtn.setImageResource(R.drawable.ic_fit_screen_24dp);
        } else {
            aspectRatioBtn.setImageResource(R.drawable.ic_aspect_ratio_24dp);
        }
    }

    private void updateButtonRotation() {
        boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        boolean auto = false;
        try {
            auto = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 1;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

//        if (spHandler.orientation == Utility.Orientation.VIDEO) {
//            if (auto) {
//                rotationBtn.setImageResource(R.drawable.ic_screen_lock_rotation_24dp);
//            } else if (portrait) {
//                rotationBtn.setImageResource(R.drawable.ic_screen_lock_portrait_24dp);
//            } else {
//                rotationBtn.setImageResource(R.drawable.ic_screen_lock_landscape_24dp);
//            }
//        } else {
//            if (auto) {
//                rotationBtn.setImageResource(R.drawable.ic_screen_rotation_24dp);
//            } else if (portrait) {
//                rotationBtn.setImageResource(R.drawable.ic_screen_portrait_24dp);
//            } else {
//                rotationBtn.setImageResource(R.drawable.ic_screen_landscape_24dp);
//            }
//        }
    }

    //leo for ad
    boolean unityAds;
    boolean showAdInExo;
    String bannerAdKey;

    public void handleBannerAD() {
        bannerAdLayout = findViewById(R.id.ad_layout);


        unityAds = getIntent().getBooleanExtra("unityAds", false);
        showAdInExo = getIntent().getBooleanExtra("showAdInExo", true);
        bannerAdKey = getIntent().getStringExtra("bannerAdKey");

        Log.e("leolog exo", "handleBannerAD showAdInExo " + showAdInExo);
        if (!unityAds) {
            if (showAdInExo) {
                loadAdaptiveADMOB_X_Banner(PlayerActivity.this, bannerAdLayout, bannerAdKey);
                bannerAdLayout.setVisibility(View.VISIBLE);
            } else {
                bannerAdLayout.setVisibility(View.GONE);
            }
        }
    }

    boolean bannerAdLoadedOneTime = false;

    public void loadAdaptiveADMOB_X_Banner(Activity activity,
                                           RelativeLayout adContainerView,
                                           String banner_id) {
        if (bannerAdLoadedOneTime) {
            return;
        }

        if (banner_id == null || banner_id.trim().isEmpty()) {
            Log.e("AdMob", "PlayerActivity Invalid banner_id: " + banner_id);
            adContainerView.setVisibility(View.GONE); // Hide if invalid
            return;
        }

        Log.e("AdMob", "PlayerActivity loadAdaptiveADMOB_X_Banner banner_id: " + banner_id);

        // Create an AdView
        AdView adView = new AdView(activity);
        adView.setAdUnitId(banner_id);

        // Set adaptive ad size before loading
        AdSize adSize = getBannerAdSize(activity);
        adView.setAdSize(adSize);

        // Replace any previous ad view
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        // Listener for logging
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                super.onAdFailedToLoad(adError);
                bannerAdLoadedOneTime = false;
                Log.e("AdMob", "PlayerActivity Banner failed: " + adError.getMessage());
                adContainerView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                bannerAdLoadedOneTime = true;
                Log.d("AdMob", "PlayerActivity Banner loaded");
            }
        });

        // Load the ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }


    private AdSize getBannerAdSize(Activity activity) {
        // Get the display metrics to calculate the screen width in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Calculate the width of the screen in density-independent pixels (dp)
        float density = displayMetrics.density;
        int adWidth = (int) (displayMetrics.widthPixels / density);

        // Return the adaptive AdSize
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (blurExecutor != null && !blurExecutor.isShutdown()) {
            blurExecutor.shutdown();
        }
    }

    private void updateCopyrightBlockerBounds(int viewWidth, int viewHeight) {
        if (!showCopyrightBlocker || copyrightBlockerContainer == null || viewWidth <= 0 || viewHeight <= 0) {
            if (copyrightBlockerContainer != null) {
                copyrightBlockerContainer.setVisibility(View.GONE);
            }
            return;
        }

        // Do NOT show blocker on initial black screen before first video frame is rendered
        if (!firstFrameRendered && !blockerHasBeenShown) {
            copyrightBlockerContainer.setVisibility(View.GONE);
            return;
        }

        // Restrict copyright blocker to Landscape mode ONLY; hide completely in Portrait
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                || viewWidth > viewHeight;

        if (!isLandscape) {
            copyrightBlockerContainer.setVisibility(View.GONE);
            return;
        }

        boolean sizeChanged = (viewWidth != lastBlockerWidth || viewHeight != lastBlockerHeight);
        if (!sizeChanged && blockerBlurApplied && copyrightBlockerContainer.getVisibility() == View.VISIBLE) {
            return; // Prevent touch/click control toggles from re-triggering blur or layout calculations
        }

        try {
            if (sizeChanged) {
                blockerBlurApplied = false; // Re-evaluate blur for new orientation/size
            }
            lastBlockerWidth = viewWidth;
            lastBlockerHeight = viewHeight;

            float posX = 0f, posY = 0f, posW = 0f, posH = 0f;
            if (copyrightBlockerPosition != null && copyrightBlockerPosition.contains(",")) {
                String[] parts = copyrightBlockerPosition.split(",");
                if (parts.length >= 2) {
                    posX = Float.parseFloat(parts[0].trim());
                    posY = Float.parseFloat(parts[1].trim());
                }
            }
            if (copyrightBlockerSize != null && copyrightBlockerSize.contains(",")) {
                String[] parts = copyrightBlockerSize.split(",");
                if (parts.length >= 2) {
                    posW = Float.parseFloat(parts[0].trim());
                    posH = Float.parseFloat(parts[1].trim());
                }
            }

            if (posW <= 0f || posH <= 0f) {
                copyrightBlockerContainer.setVisibility(View.GONE);
                return;
            }

            // Reference Landscape Dimensions (Standard 1080p Stream Space: 1920x1080)
            float refWidth = 1920.0f;
            float refHeight = 1080.0f;
            if (posX > 1920f) refWidth = Math.max(1920.0f, posX + posW);
            if (posY > 1080f) refHeight = Math.max(1080.0f, posY + posH);

            // Calculate relative normalized fractions (0.0 to 1.0)
            float normX = posX / refWidth;
            float normY = posY / refHeight;
            float normW = posW / refWidth;
            float normH = posH / refHeight;

            // Scale proportionally to current landscape view dimensions
            int targetW = Math.max(10, Math.round(normW * viewWidth));
            int targetH = Math.max(10, Math.round(normH * viewHeight));
            int targetX = Math.round(normX * viewWidth);
            int targetY = Math.round(normY * viewHeight);

            // Clamp to stay 100% inside video bounds
            targetX = Math.max(0, Math.min(targetX, viewWidth - targetW));
            targetY = Math.max(0, Math.min(targetY, viewHeight - targetH));

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) copyrightBlockerContainer.getLayoutParams();
            if (params != null) {
                params.width = targetW;
                params.height = targetH;
                params.leftMargin = targetX;
                params.topMargin = targetY;
                copyrightBlockerContainer.setLayoutParams(params);
            }

            // Once set in Landscape, keep blocker VISIBLE permanently across seeking/buffering
            copyrightBlockerContainer.setVisibility(View.VISIBLE);
            if (imgCopyrightBlocker != null) {
                imgCopyrightBlocker.setAlpha(1.0f);
            }

            // Apply frame blur if player is active
            if (exoPlayer != null && (exoPlayer.isPlaying() || exoPlayer.getPlaybackState() == Player.STATE_READY)) {
                applyCopyrightBlockerBlur(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyCopyrightBlockerBlur(boolean force) {
        if (!showCopyrightBlocker || imgCopyrightBlocker == null) return;
        if (blockerBlurApplied && !force) return;

        if (imgCopyrightBlocker != null) {
            imgCopyrightBlocker.setAlpha(1.0f);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                imgCopyrightBlocker.setRenderEffect(
                        android.graphics.RenderEffect.createBlurEffect(50f, 50f, android.graphics.Shader.TileMode.CLAMP)
                );
                blockerBlurApplied = true;
            } catch (Exception ignored) {}
        }

        blurExecutor.execute(() -> {
            try {
                if (customPlayerView == null) return;
                View surfaceView = customPlayerView.getVideoSurfaceView();
                if (surfaceView instanceof TextureView) {
                    final TextureView textureView = (TextureView) surfaceView;
                    if (textureView.isAvailable()) {
                        final Bitmap fullFrame = textureView.getBitmap();
                        if (fullFrame != null) {
                            int frameW = fullFrame.getWidth();
                            int frameH = fullFrame.getHeight();

                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) copyrightBlockerContainer.getLayoutParams();
                            int containerW = customPlayerView.getWidth();
                            int containerH = customPlayerView.getHeight();

                            if (containerW > 0 && containerH > 0 && params != null) {
                                float scaleX = (float) frameW / containerW;
                                float scaleY = (float) frameH / containerH;

                                int cropX = Math.max(0, Math.round(params.leftMargin * scaleX));
                                int cropY = Math.max(0, Math.round(params.topMargin * scaleY));
                                int cropW = Math.min(frameW - cropX, Math.max(1, Math.round(params.width * scaleX)));
                                int cropH = Math.min(frameH - cropY, Math.max(1, Math.round(params.height * scaleY)));

                                if (cropW > 0 && cropH > 0 && (cropX + cropW <= frameW) && (cropY + cropH <= frameH)) {
                                    Bitmap cropped = Bitmap.createBitmap(fullFrame, cropX, cropY, cropW, cropH);
                                    Bitmap firstPass = fastStackBlur(cropped, 25, 4);
                                    final Bitmap blurred = fastStackBlur(firstPass, 25, 1);

                                    runOnUiThread(() -> {
                                        if (alive && imgCopyrightBlocker != null) {
                                            imgCopyrightBlocker.setImageBitmap(blurred);
                                            imgCopyrightBlocker.setAlpha(1.0f);
                                            blockerBlurApplied = true;
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static Bitmap fastStackBlur(Bitmap sentBitmap, int radius, int scale) {
        if (sentBitmap == null) return null;
        try {
            int width = Math.max(1, sentBitmap.getWidth() / scale);
            int height = Math.max(1, sentBitmap.getHeight() / scale);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);
            Bitmap bitmap = scaledBitmap.copy(scaledBitmap.getConfig(), true);

            if (radius < 1) return bitmap;

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int[] pix = new int[w * h];
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int[] r = new int[wh];
            int[] g = new int[wh];
            int[] b = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int[] vmin = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int[] dv = new int[256 * divsum];
            for (i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;
            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {
                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }

            bitmap.setPixels(pix, 0, w, 0, 0, w, h);
            return bitmap;
        } catch (Exception e) {
            return sentBitmap;
        }
    }

}