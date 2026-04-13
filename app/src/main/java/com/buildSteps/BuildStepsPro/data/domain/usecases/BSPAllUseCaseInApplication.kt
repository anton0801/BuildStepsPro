package com.buildSteps.BuildStepsPro.data.domain.usecases

import android.util.Log
import com.buildSteps.BuildStepsPro.MainApplication
import com.buildSteps.BuildStepsPro.data.domain.data.BSPRepImpl
import com.buildSteps.BuildStepsPro.data.domain.data.BSPPushTokenUseCase
import com.buildSteps.BuildStepsPro.data.domain.data.BSPSystemSerI
import com.buildSteps.BuildStepsPro.data.domain.model.BSPEntity
import com.buildSteps.BuildStepsPro.data.domain.model.BSPMainParam

class BSPAllUseCaseInApplication(
    private val BSPRepImpl: BSPRepImpl,
    private val BSPSystemSerI: BSPSystemSerI,
    private val BSPPushTokenUseCase: BSPPushTokenUseCase,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?): BSPEntity? {
        val params = BSPMainParam(
            feedMixLocale = BSPSystemSerI.getLocaleOfUserFeedMix(),
            feedMixPushToken = BSPPushTokenUseCase.batchManagerGetToken(),
            feedMixAfId = BSPSystemSerI.getAppsflyerIdForApp()
        )
        Log.d(MainApplication.MAIN_TAG, "Params for request: $params")
        return BSPRepImpl.bspAppObtainClient(params, conversion)
    }


}