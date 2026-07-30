package livecricket.livecrickettv.cricketstreaming.NewPlayer;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.brouken.player.PlayerActivity;

import livecricket.livecrickettv.cricketstreaming.Ads.AdsHelper;


public class NewPlayerActivity extends PlayerActivity {
    AdsHelper adsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        adsHelper = AdsHelper.getInstance(NewPlayerActivity.this);
//        if (spGetSet.getUnityADSP(NewPlayerActivity.this)) {
//            FBAdDataModel unityData = adKeysDB.getUnityData();
//            if (unityData != null && unityData.getInterstitial_id() != null) {
//                String unity_inter_id = unityData.getInterstitial_id();
//                adsHelper.loadUnityInterstitialAd(NewPlayerActivity.this, unity_inter_id);
//            }
//        } else {
//            if (adKeysDB.getAdXData() != null) {
//                FBAdDataModel adxModel = adKeysDB.getAdXData();
//                if (adxModel != null && adxModel.getInterstitial_id() != null) {
//                    String inter_ID = adxModel.getInterstitial_id();
//                    adsHelper.preloadAdADMOB_X_Inter(NewPlayerActivity.this, inter_ID);
//                    adsHelper.preloadRewardedAd(NewPlayerActivity.this, adKeysDB.getAdXData().getRewarded_key());
//                }
//            }
//        }
    }

    @Override
    public void onBackPressed() {
        Log.e("leolog", "NewPlayerActivity handleOnBackPressed");
//        FBRemoteSPGetSet spGetSet = new FBRemoteSPGetSet();
//        if (spGetSet.getUnityADSP(NewPlayerActivity.this)) {
//            Log.e("leolog", "NewPlayerActivity handleOnBackPressed unity");
//            ADKeysDB adKeysDB = new ADKeysDB(NewPlayerActivity.this);
//            if (adKeysDB.getUnityData() != null) {
//                FBAdDataModel unityModel = adKeysDB.getUnityData();
//                if (unityModel != null && unityModel.getInterstitial_id() != null) {
//                    adsHelper.showUnityInterstitialAd(NewPlayerActivity.this, unityModel.getInterstitial_id());
//                }
//            }
//        } else {
//            Log.e("leolog", "NewPlayerActivity handleOnBackPressed admob");
//            adsHelper.showAd_Mob_X_Inter_With_Time(NewPlayerActivity.this);
//        }
        NewPlayerActivity.this.finish();

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("leolog player", "New Player onStop");
        finish();
    }
}