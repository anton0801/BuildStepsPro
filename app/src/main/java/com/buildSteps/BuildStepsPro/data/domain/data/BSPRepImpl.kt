package com.buildSteps.BuildStepsPro.data.domain.data

import com.buildSteps.BuildStepsPro.data.domain.model.BSPEntity
import com.buildSteps.BuildStepsPro.data.domain.model.BSPMainParam
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface BSPLabelApiInterface {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun feedMixGetClient(
        @Body jsonString: JsonObject,
    ): Call<BSPEntity>
}


private const val BSP_MAIN_L = "https://buildstepspro.com/"

class BSPRepImpl {

    suspend fun bspAppObtainClient(
        BSPMainParam: BSPMainParam,
        eggLabelConversion: MutableMap<String, Any>?
    ): BSPEntity? {
        val gson = Gson()
        val api = bspAppAGetApi(BSP_MAIN_L, null)

        val eggLabelJsonObject = gson.toJsonTree(BSPMainParam).asJsonObject
        eggLabelConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            eggLabelJsonObject.add(key, element)
        }
        return try {
            val eggLabelRequest: Call<BSPEntity> = api.feedMixGetClient(
                jsonString = eggLabelJsonObject,
            )
            val eggLabelResult = eggLabelRequest.awaitResponse()
            if (eggLabelResult.code() == 200) {
                eggLabelResult.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


    private fun bspAppAGetApi(
        url: String,
        client: OkHttpClient?
    ): BSPLabelApiInterface {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
