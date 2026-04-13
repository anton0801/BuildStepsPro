package com.buildSteps.BuildStepsPro.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.buildSteps.BuildStepsPro.data.preferences.AppPreferences
import com.buildSteps.BuildStepsPro.data.repository.AppRepository

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val prefs = AppPreferences(context)
            val repo = AppRepository(prefs)
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
