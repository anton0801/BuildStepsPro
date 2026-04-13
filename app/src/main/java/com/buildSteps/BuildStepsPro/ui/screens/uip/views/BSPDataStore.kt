package com.buildSteps.BuildStepsPro.ui.screens.uip.views

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class BSPDataStore : ViewModel() {
    val BSPMainApplicationViList: MutableList<BSPMainApplicationVi> =
        mutableListOf()
    var feedMixIsFirstCreate = true

    @SuppressLint("StaticFieldLeak")
    lateinit var feedMixContainerView: FrameLayout

    @SuppressLint("StaticFieldLeak")
    lateinit var BSPMainApplicationView: BSPMainApplicationVi

}