# ============================
# General ProGuard Settings
# ============================
-dontwarn android.media.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.bumptech.glide.**
-dontwarn androidx.work.**
-dontwarn androidx.startup.**

# ============================
# Retrofit & Gson Keep Rules
# ============================
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# ============================
# Glide Keep Rules
# ============================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * implements com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.request.BaseRequestOptions
-keep class com.bumptech.glide.Glide { *; }

# ============================
# Application & Project Classes
# ============================
# Adjust to match your current namespace
-keep class livecricket.livecrickettv.cricketstreaming.** { *; }

# ============================
# Room Persistence Library
# ============================
-keep class * extends androidx.room.RoomDatabase
-keep class livecricket.livecrickettv.cricketstreaming.database.** { *; }

# ============================
# Hilt / Dagger
# ============================
-keep class dagger.hilt.android.internal.** { *; }
-keep class * extends android.app.Application
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *

# ============================
# Ads (AdMob & Unity)
# ============================
# AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }

# Unity Ads
-keep class com.unity3d.ads.** { *; }
-keep class com.unity3d.services.** { *; }

# ============================
# Media3 / ExoPlayer
# ============================
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.datasource.** { *; }
-keep class androidx.media3.ui.** { *; }

# ============================
# Android Resources & Parcelable
# ============================
-keep class **.R$* { *; }
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ============================
# WorkManager & Startup
# ============================
-keep class androidx.work.** { *; }
-keep class androidx.startup.** { *; }
