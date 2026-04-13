package com.buildSteps.BuildStepsPro.ui.screens.uip

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildSteps.BuildStepsPro.BatchManagerAppsFlyerState
import com.buildSteps.BuildStepsPro.MainApplication
import com.buildSteps.BuildStepsPro.data.domain.data.BSPSystemSerI
import com.buildSteps.BuildStepsPro.data.domain.usecases.BSPAllUseCaseInApplication
import com.buildSteps.BuildStepsPro.data.handlers.BuildStepsProLocalStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuildStepsProLoadingSplashVM(
    private val BSPAllUseCaseInApplication: BSPAllUseCaseInApplication,
    private val buildStepsProLocalStoreManager: BuildStepsProLocalStoreManager,
    private val BSPSystemSerI: BSPSystemSerI
) : ViewModel() {

    private val _chickHealthHomeScreenState: MutableStateFlow<FeedMixHomeScreenState> =
        MutableStateFlow(FeedMixHomeScreenState.FeedMixLoading)
    val chickHealthHomeScreenState = _chickHealthHomeScreenState.asStateFlow()

    private var eggLabelGetApps = false

    init {
        viewModelScope.launch {
            when (buildStepsProLocalStoreManager.batchAppState) {
                0 -> {
                    if (BSPSystemSerI.feedMixCheckInternetConnection()) {
                        MainApplication.BatchingManagerConversionFlow.collect {
                            when (it) {
                                BatchManagerAppsFlyerState.BatchManagerDefault -> {}
                                BatchManagerAppsFlyerState.BatchManagerError -> {
                                    buildStepsProLocalStoreManager.batchAppState = 2
                                    _chickHealthHomeScreenState.value =
                                        FeedMixHomeScreenState.FeedMixError
                                    eggLabelGetApps = true
                                }

                                is BatchManagerAppsFlyerState.BatchManagerSuccess -> {
                                    if (!eggLabelGetApps) {
                                        feedMixGetData(it.feedMixxChickkData)
                                        eggLabelGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _chickHealthHomeScreenState.value =
                            FeedMixHomeScreenState.FeedMixNotInternet
                    }
                }

                1 -> {
                    if (BSPSystemSerI.feedMixCheckInternetConnection()) {
                        if (MainApplication.BATCH_MANAGER_LI != null) {
                            _chickHealthHomeScreenState.value =
                                FeedMixHomeScreenState.FeedMixSuccess(
                                    MainApplication.BATCH_MANAGER_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > buildStepsProLocalStoreManager.batchExpired) {
                            Log.d(
                                MainApplication.MAIN_TAG,
                                "Current time more then expired, repeat request"
                            )
                            MainApplication.BatchingManagerConversionFlow.collect {
                                when (it) {
                                    BatchManagerAppsFlyerState.BatchManagerDefault -> {}
                                    BatchManagerAppsFlyerState.BatchManagerError -> {
                                        _chickHealthHomeScreenState.value =
                                            FeedMixHomeScreenState.FeedMixSuccess(
                                                buildStepsProLocalStoreManager.batchSavedUrl
                                            )
                                        eggLabelGetApps = true
                                    }

                                    is BatchManagerAppsFlyerState.BatchManagerSuccess -> {
                                        if (!eggLabelGetApps) {
                                            feedMixGetData(it.feedMixxChickkData)
                                            eggLabelGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(
                                MainApplication.MAIN_TAG,
                                "Current time less then expired, use saved url"
                            )
                            _chickHealthHomeScreenState.value =
                                FeedMixHomeScreenState.FeedMixSuccess(
                                    buildStepsProLocalStoreManager.batchSavedUrl
                                )
                        }
                    } else {
                        _chickHealthHomeScreenState.value =
                            FeedMixHomeScreenState.FeedMixNotInternet
                    }
                }

                2 -> {
                    _chickHealthHomeScreenState.value =
                        FeedMixHomeScreenState.FeedMixError
                }
            }
        }
    }


    private suspend fun feedMixGetData(conversation: MutableMap<String, Any>?) {
        val eggLabelData = BSPAllUseCaseInApplication.invoke(conversation)
        if (buildStepsProLocalStoreManager.batchAppState == 0) {
            if (eggLabelData == null) {
                buildStepsProLocalStoreManager.batchAppState = 2
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixError
            } else {
                buildStepsProLocalStoreManager.batchAppState = 1
                buildStepsProLocalStoreManager.apply {
                    batchExpired = eggLabelData.feedMixExpires
                    batchSavedUrl = eggLabelData.feedMixUrl
                }
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixSuccess(eggLabelData.feedMixUrl)
            }
        } else {
            if (eggLabelData == null) {
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixSuccess(buildStepsProLocalStoreManager.batchSavedUrl)
            } else {
                buildStepsProLocalStoreManager.apply {
                    batchExpired = eggLabelData.feedMixExpires
                    batchSavedUrl = eggLabelData.feedMixUrl
                }
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixSuccess(eggLabelData.feedMixUrl)
            }
        }
    }


    sealed class FeedMixHomeScreenState {
        data object FeedMixLoading : FeedMixHomeScreenState()
        data object FeedMixError : FeedMixHomeScreenState()
        data class FeedMixSuccess(val data: String) : FeedMixHomeScreenState()
        data object FeedMixNotInternet : FeedMixHomeScreenState()
    }
}