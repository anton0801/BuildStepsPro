package com.buildSteps.BuildStepsPro.data.domain.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.buildSteps.BuildStepsPro.MainApplication
import java.util.Locale

class BSPSystemSerI(private val context: Context) {

    fun getLocaleOfUserFeedMix(): String {
        return Locale.getDefault().language
    }

//    suspend fun feedMixRetriveDeviceGaid(): String = withContext(Dispatchers.IO) {
//        val gaid = AdvertisingIdClient.getAdvertisingIdInfo(context).id
//            ?: "00000000-0000-0000-0000-000000000000"
//        Log.d(MainApplication.SLEEPING_MAIN_TAG, "Gaid: $gaid")
//        return@withContext gaid
//    }

    fun feedMixCheckInternetConnection(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                return true
            }
        }
        return false
    }

    fun getAppsflyerIdForApp(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(context) ?: ""
        Log.d(MainApplication.MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
        return appsflyrid
    }


}