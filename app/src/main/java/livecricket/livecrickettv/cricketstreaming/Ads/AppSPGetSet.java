package livecricket.livecrickettv.cricketstreaming.Ads;

import android.content.Context;
import android.content.SharedPreferences;


public class AppSPGetSet {

    public void setGettingDataFromServerSuccessfullySP(Context context, boolean value) {
        SharedPreferences GettingDataFromServerSuccessfullySP = context.getSharedPreferences("GettingDataFromServerSuccessfullySP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = GettingDataFromServerSuccessfullySP.edit();
        editor.putBoolean("GettingDataFromServerSuccessfullySP", value);
        editor.apply();
    }
    public Boolean getGettingDataFromServerSuccessfullySP(Context context) {
        SharedPreferences GettingDataFromServerSuccessfullySP = context.getSharedPreferences("GettingDataFromServerSuccessfullySP", Context.MODE_PRIVATE);
        return GettingDataFromServerSuccessfullySP.getBoolean("GettingDataFromServerSuccessfullySP", false);
    }

    public void setAppCategory(Context context, String value) {
        SharedPreferences AppCategory = context.getSharedPreferences("AppCategory", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = AppCategory.edit();
        editor.putString("AppCategory", value);
        editor.apply();
    }

    public String getAppCategory(Context context) {
        SharedPreferences AppCategory = context.getSharedPreferences("AppCategory", Context.MODE_PRIVATE);
        return AppCategory.getString("AppCategory", "null");
    }

    public void setISCricketLiveSP(Context context, boolean value) {
        SharedPreferences ISCricketLiveSP = context.getSharedPreferences("ISCricketLiveSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ISCricketLiveSP.edit();
        editor.putBoolean("ISCricketLiveSP", value);
        editor.apply();
    }

    public Boolean getIsCricketLive(Context context) {
        SharedPreferences ISCricketLiveSP = context.getSharedPreferences("ISCricketLiveSP", Context.MODE_PRIVATE);
        return ISCricketLiveSP.getBoolean("ISCricketLiveSP", false);
    }

    public void setIsFootballLiveSP(Context context, boolean value) {
        SharedPreferences IsFootballLiveSP = context.getSharedPreferences("IsFootballLiveSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = IsFootballLiveSP.edit();
        editor.putBoolean("IsFootballLiveSP", value);
        editor.apply();
    }

    public Boolean getIsFootballLiveSP(Context context) {
        SharedPreferences IsFootballLiveSP = context.getSharedPreferences("IsFootballLiveSP", Context.MODE_PRIVATE);
        return IsFootballLiveSP.getBoolean("IsFootballLiveSP", false);
    }

    public void setOtherSportsLiveSP(Context context, boolean value) {
        SharedPreferences OtherSportsLiveSP = context.getSharedPreferences("OtherSportsLiveSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = OtherSportsLiveSP.edit();
        editor.putBoolean("OtherSportsLiveSP", value);
        editor.apply();
    }

    public Boolean getOtherSportsLiveSP(Context context) {
        SharedPreferences OtherSportsLiveSP = context.getSharedPreferences("OtherSportsLiveSP", Context.MODE_PRIVATE);
        return OtherSportsLiveSP.getBoolean("OtherSportsLiveSP", false);
    }


    public void setOutsideLinkSP(Context context, String value) {
        SharedPreferences OutsideLinkSP = context.getSharedPreferences("OutsideLinkSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = OutsideLinkSP.edit();
        editor.putString("OutsideLinkSP", value);
        editor.apply();
    }

    public String getOutsideLinkSP(Context context) {
        SharedPreferences OutsideLinkSP = context.getSharedPreferences("OutsideLinkSP", Context.MODE_PRIVATE);
        return OutsideLinkSP.getString("OutsideLinkSP", "null");
    }

    public void setTelegramLinkSP(Context context, String value) {
        SharedPreferences TelegramLinkSP = context.getSharedPreferences("TelegramLinkSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = TelegramLinkSP.edit();
        editor.putString("TelegramLinkSP", value);
        editor.apply();
    }

    public String getTelegramLinkSP(Context context) {
        SharedPreferences TelegramLinkSP = context.getSharedPreferences("TelegramLinkSP", Context.MODE_PRIVATE);
        return TelegramLinkSP.getString("TelegramLinkSP", "null");
    }

    public void setWACommunitySP(Context context, String value) {
        SharedPreferences WACommunitySP = context.getSharedPreferences("WACommunitySP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = WACommunitySP.edit();
        editor.putString("WACommunitySP", value);
        editor.apply();
    }

    public String getWACommunitySP(Context context) {
        SharedPreferences WACommunitySP = context.getSharedPreferences("WACommunitySP", Context.MODE_PRIVATE);
        return WACommunitySP.getString("WACommunitySP", "null");
    }

    public void setMoreAppsSP(Context context, String value) {
        SharedPreferences MoreAppsSP = context.getSharedPreferences("MoreAppsSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = MoreAppsSP.edit();
        editor.putString("MoreAppsSP", value);
        editor.apply();
    }

    public String getMoreAppsSP(Context context) {
        SharedPreferences MoreAppsSP = context.getSharedPreferences("MoreAppsSP", Context.MODE_PRIVATE);
        return MoreAppsSP.getString("MoreAppsSP", "null");
    }

    public void setSplashImageSP(Context context, String value) {
        SharedPreferences SplashImageSP = context.getSharedPreferences("SplashImageSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = SplashImageSP.edit();
        editor.putString("SplashImageSP", value);
        editor.apply();
    }

    public String getSplashImageSP(Context context) {
        SharedPreferences SplashImageSP = context.getSharedPreferences("SplashImageSP", Context.MODE_PRIVATE);
        return SplashImageSP.getString("SplashImageSP", "null");
    }


    public void setAppOpenCountSP(Context context, int value) {
        SharedPreferences AppOpenCountSP = context.getSharedPreferences("AppOpenCountSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = AppOpenCountSP.edit();
        editor.putInt("AppOpenCountSP", value);
        editor.apply();
    }

    public int getAppOpenCountSP(Context context) {
        SharedPreferences AppOpenCountSP = context.getSharedPreferences("AppOpenCountSP", Context.MODE_PRIVATE);
        return AppOpenCountSP.getInt("AppOpenCountSP", -1);
    }

    public void setAddFirstTimeSP(Context context, boolean value) {
        SharedPreferences AddFirstTimeSP = context.getSharedPreferences("AddFirstTimeSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = AddFirstTimeSP.edit();
        editor.putBoolean("AddFirstTimeSP", value);
        editor.apply();
    }

    public Boolean getAddFirstTimeSP(Context context) {
        SharedPreferences AddFirstTimeSP = context.getSharedPreferences("AddFirstTimeSP", Context.MODE_PRIVATE);
        return AddFirstTimeSP.getBoolean("AddFirstTimeSP", true);
    }


    public void setRewardAdShownSP(Context context, boolean value) {
        SharedPreferences RewardAdShownSP = context.getSharedPreferences("RewardAdShownSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = RewardAdShownSP.edit();
        editor.putBoolean("RewardAdShownSP", value);
        editor.apply();
    }

    public Boolean getRewardAdShownSP(Context context) {
        SharedPreferences RewardAdShownSP = context.getSharedPreferences("RewardAdShownSP", Context.MODE_PRIVATE);
        return RewardAdShownSP.getBoolean("RewardAdShownSP", false);
    }


    public void setDataFromServerSuccessfulSP(Context context, boolean value) {
        SharedPreferences DataFromServerSuccessfulSP = context.getSharedPreferences("DataFromServerSuccessfulSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = DataFromServerSuccessfulSP.edit();
        editor.putBoolean("DataFromServerSuccessfulSP", value);
        editor.apply();
    }

    public Boolean getDataFromServerSuccessfulSP(Context context) {
        SharedPreferences DataFromServerSuccessfulSP = context.getSharedPreferences("DataFromServerSuccessfulSP", Context.MODE_PRIVATE);
        return DataFromServerSuccessfulSP.getBoolean("DataFromServerSuccessfulSP", false);
    }


}
