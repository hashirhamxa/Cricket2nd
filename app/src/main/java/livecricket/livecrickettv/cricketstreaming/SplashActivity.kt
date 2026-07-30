package livecricket.livecrickettv.cricketstreaming

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import livecricket.livecrickettv.cricketstreaming.Ads.AdsHelper
import livecricket.livecrickettv.cricketstreaming.Ads.AppOpenManager
import livecricket.livecrickettv.cricketstreaming.Database.AdEntity
import livecricket.livecrickettv.cricketstreaming.Network.AppRepository
import livecricket.livecrickettv.cricketstreaming.Utilities.CricketApp
import livecricket.livecrickettv.cricketstreaming.Utilities.Utils
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: AppRepository

    private var configRetryCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            handleSplashFlow()
        }
    }

    private suspend fun handleSplashFlow() {
        if (!Utils.isInternetAvailable(this)) {
            showNoInternetDialog()
            return
        }

        if (AppOpenManager.checkSniffer(this)) return

        fetchConfigAndProceed()
    }

    private suspend fun fetchConfigAndProceed() {
        // Linear call to suspending function
        val isSuccess = repository.fetchAndSaveConfig { _, _ -> }

        if (isSuccess) {
            val allAds = repository.getAllAds()
            preloadAds(allAds)
            loadAndShowAppOpenAd(allAds)
        } else {
            if (configRetryCount < 1) {
                configRetryCount++
                fetchConfigAndProceed()
            } else {
                showErrorDialog()
            }
        }
    }

    private fun preloadAds(ads: List<AdEntity>) {
        val adsHelper = AdsHelper.getInstance(this)
        ads.find { it.adPlacement.equals("Interstitial", ignoreCase = true) }?.adUnitId?.let {
            adsHelper.preloadAdADMOB_X_Inter(this, it)
        }
        ads.find { it.adPlacement.equals("Rewarded", ignoreCase = true) }?.adUnitId?.let {
            adsHelper.preloadRewardedAd(this, it)
        }
    }

    private suspend fun loadAndShowAppOpenAd(ads: List<AdEntity>) {
        val appOpenId = ads.find { it.adPlacement.equals("AppOpen", ignoreCase = true) }?.adUnitId
        val appOpenManager = (application as CricketApp).appOpenManager

        if (appOpenId != null && appOpenManager != null) {
            withTimeoutOrNull(4000.milliseconds) {
                suspendCancellableCoroutine { continuation ->
                    appOpenManager.fetchAndShowAd(appOpenId) {
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }
                }
            }
        }
        navigateToMain()
    }

    private fun showNoInternetDialog() {
        Utils.showCustomDialog(
            this,
            "No Internet Available!",
            "Please check your internet connection and try again",
            "OK",
            "Cancel",
            false,
            false,
            { finishAffinity() },
            { }
        )
    }

    private fun showErrorDialog() {
        Utils.showCustomDialog(
            this,
            "Something went wrong!",
            "An error occurred while fetching data from the server. Please try again later.",
            "OK",
            "",
            false,
            false,
            { finishAffinity() },
            null
        )
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
