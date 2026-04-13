package com.buildSteps.BuildStepsPro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.buildSteps.BuildStepsPro.navigation.AppNavGraph
import com.buildSteps.BuildStepsPro.ui.theme.BackgroundLight
import com.buildSteps.BuildStepsPro.ui.theme.BuildStepsProTheme
import com.buildSteps.BuildStepsPro.viewmodel.MainViewModel
import com.buildSteps.BuildStepsPro.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var vm: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProvider(
            this,
            MainViewModelFactory(applicationContext)
        )[MainViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            BuildStepsProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundLight
                ) {
                    AppNavGraph(vm = vm)
                }
            }
        }
    }

}
