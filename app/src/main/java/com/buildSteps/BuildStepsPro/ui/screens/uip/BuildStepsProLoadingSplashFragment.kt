package com.buildSteps.BuildStepsPro.ui.screens.uip

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.buildSteps.BuildStepsPro.MainActivity
import com.buildSteps.BuildStepsPro.R
import com.buildSteps.BuildStepsPro.databinding.FragmentLoadBuildStepsProBinding
import com.buildSteps.BuildStepsPro.data.handlers.BuildStepsProLocalStoreManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class BuildStepsProLoadingSplashFragment : Fragment(R.layout.fragment_load_build_steps_pro) {
    private lateinit var buildStepsProLoadingBinding: FragmentLoadBuildStepsProBinding

    private val buildStepsProLoadingSplashVM by viewModel<BuildStepsProLoadingSplashVM>()

    private val buildStepsProLocalStoreManager by inject<BuildStepsProLocalStoreManager>()

    private var buildStepsProUrl = ""

    private val chickHealthRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            bspToSuccess(buildStepsProUrl)
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                buildStepsProLocalStoreManager.batchNotificationRequest =
                    (System.currentTimeMillis() / 1000) + 2592000000
                bspToSuccess(buildStepsProUrl)
            } else {
                bspToSuccess(buildStepsProUrl)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 999 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            bspToSuccess(buildStepsProUrl)
        } else {
            // твой код на отказ
            bspToSuccess(buildStepsProUrl)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildStepsProLoadingBinding = FragmentLoadBuildStepsProBinding.bind(view)

        buildStepsProLoadingBinding.feedMixGrandButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val eggLabelPermission = Manifest.permission.POST_NOTIFICATIONS
                chickHealthRequestNotificationPermission.launch(eggLabelPermission)
                buildStepsProLocalStoreManager.batchNotificationRequestedBefore = true
            } else {
                bspToSuccess(buildStepsProUrl)
                buildStepsProLocalStoreManager.batchNotificationRequestedBefore = true
            }
        }

        buildStepsProLoadingBinding.feedMixSkipButton.setOnClickListener {
            buildStepsProLocalStoreManager.batchNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            bspToSuccess(buildStepsProUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                buildStepsProLoadingSplashVM.chickHealthHomeScreenState.collect {
                    when (it) {
                        is BuildStepsProLoadingSplashVM.FeedMixHomeScreenState.FeedMixLoading -> {
                        }

                        is BuildStepsProLoadingSplashVM.FeedMixHomeScreenState.FeedMixError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is BuildStepsProLoadingSplashVM.FeedMixHomeScreenState.FeedMixSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val eggLabelPermission = Manifest.permission.POST_NOTIFICATIONS
                                val eggLabelPermissionRequestedBefore =
                                    buildStepsProLocalStoreManager.batchNotificationRequestedBefore

                                if (ContextCompat.checkSelfPermission(
                                        requireContext(),
                                        eggLabelPermission
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    bspToSuccess(it.data)
                                } else if (!eggLabelPermissionRequestedBefore && (System.currentTimeMillis() / 1000 > buildStepsProLocalStoreManager.batchNotificationRequest)) {
                                    // первый раз — показываем UI для запроса
                                    buildStepsProLoadingBinding.feedMixNotiGroup.visibility =
                                        View.VISIBLE
                                    buildStepsProLoadingBinding.feedMixLoadingGroup.visibility =
                                        View.GONE
                                    buildStepsProUrl = it.data
                                } else if (shouldShowRequestPermissionRationale(eggLabelPermission)) {
                                    // временный отказ — через 3 дня можно показать
                                    if (System.currentTimeMillis() / 1000 > buildStepsProLocalStoreManager.batchNotificationRequest) {
                                        buildStepsProLoadingBinding.feedMixNotiGroup.visibility =
                                            View.VISIBLE
                                        buildStepsProLoadingBinding.feedMixLoadingGroup.visibility =
                                            View.GONE
                                        buildStepsProUrl = it.data
                                    } else {
                                        bspToSuccess(it.data)
                                    }
                                } else {
                                    bspToSuccess(it.data)
                                }
                            } else {
                                bspToSuccess(it.data)
                            }
                        }

                        BuildStepsProLoadingSplashVM.FeedMixHomeScreenState.FeedMixNotInternet -> {
                            buildStepsProLoadingBinding.feedMixStateGroup.visibility = View.VISIBLE
                            buildStepsProLoadingBinding.feedMixLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun bspToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_batchManagerLoadingSplashFragment_to_batchManagerMainApplicationV,
            bundleOf(FEED_MIX_D to data)
        )
    }


    companion object {
        const val FEED_MIX_D = "eggLabelData"
    }
}