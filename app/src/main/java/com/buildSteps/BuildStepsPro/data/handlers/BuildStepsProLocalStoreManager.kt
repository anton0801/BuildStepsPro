package com.buildSteps.BuildStepsPro.data.handlers

import android.content.Context
import androidx.core.content.edit

class BuildStepsProLocalStoreManager(context: Context) {
    private val batchMixPrefs =
        context.getSharedPreferences("feedMixsharedPrefsAb", Context.MODE_PRIVATE)

    var batchNotificationRequestedBefore: Boolean
        get() = batchMixPrefs.getBoolean(BATCH_NOTIFICATION_REQUEST_BEFORE, false)
        set(value) = batchMixPrefs.edit {
            putBoolean(
                BATCH_NOTIFICATION_REQUEST_BEFORE, value
            )
        }
    var batchExpired: Long
        get() = batchMixPrefs.getLong(BATCH_EXPIRED, 0L)
        set(value) = batchMixPrefs.edit { putLong(BATCH_EXPIRED, value) }

    var batchAppState: Int
        get() = batchMixPrefs.getInt(BATCH_APPLICATION_STATE, 0)
        set(value) = batchMixPrefs.edit { putInt(BATCH_APPLICATION_STATE, value) }

    var batchSavedUrl: String
        get() = batchMixPrefs.getString(BATCH_SAVED_URL, "") ?: ""
        set(value) = batchMixPrefs.edit { putString(BATCH_SAVED_URL, value) }

    var batchNotificationRequest: Long
        get() = batchMixPrefs.getLong(BATCH_NOTIFICAITON_REQUEST, 0L)
        set(value) = batchMixPrefs.edit { putLong(BATCH_NOTIFICAITON_REQUEST, value) }


    companion object {
        private const val BATCH_SAVED_URL = "eggLabelSavedUrl"
        private const val BATCH_EXPIRED = "eggLabelExpired"
        private const val BATCH_APPLICATION_STATE = "eggLabelApplicationState"
        private const val BATCH_NOTIFICAITON_REQUEST = "eggLabelNotificationRequest"
        private const val BATCH_NOTIFICATION_REQUEST_BEFORE =
            "eggLabelNotificationRequestedBefore"
    }
}