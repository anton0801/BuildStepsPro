package com.buildSteps.BuildStepsPro

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface BatchManagerAppsApi {
    @Headers("Content-Type: application/json")
    @GET(BATCH_MANAGER_MIX_LIN)
    fun eggLabelGetClient(
        @Query("devkey") devkey: String,
        @Query("device_id") deviceId: String,
    ): Call<MutableMap<String, Any>?>
}

sealed interface BatchManagerAppsFlyerState {
    data object BatchManagerDefault : BatchManagerAppsFlyerState
    data class BatchManagerSuccess(val feedMixxChickkData: MutableMap<String, Any>?) : BatchManagerAppsFlyerState
    data object BatchManagerError : BatchManagerAppsFlyerState
}