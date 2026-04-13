package com.buildSteps.BuildStepsPro.data.handlers

import android.os.Bundle
import android.util.Log
import com.buildSteps.BuildStepsPro.MainApplication

class BSPNotifPushHandler {

    fun batchAppHandlePush(extras: Bundle?) {
        Log.d(MainApplication.MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map = batchBundleToMap(extras)
            Log.d(MainApplication.MAIN_TAG, "Map from Push = $map")
            map?.let {
                if (map.containsKey("url")) {
                    MainApplication.BATCH_MANAGER_LI = map["url"]
                    Log.d(MainApplication.MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(MainApplication.MAIN_TAG, "Push data no!")
        }
    }

    private fun batchBundleToMap(extras: Bundle): Map<String, String?>? {
        val map: MutableMap<String, String?> = HashMap()
        val ks = extras.keySet()
        val iterator: Iterator<String> = ks.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = extras.getString(key)
        }
        return map
    }

}