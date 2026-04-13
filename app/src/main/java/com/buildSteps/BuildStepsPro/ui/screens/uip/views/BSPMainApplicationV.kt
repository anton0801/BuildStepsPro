package com.buildSteps.BuildStepsPro.ui.screens.uip.views

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.buildSteps.BuildStepsPro.MainApplication
import com.buildSteps.BuildStepsPro.ui.screens.uip.BuildStepsProLoadingSplashFragment
import org.koin.android.ext.android.inject

class BSPMainApplicationV : Fragment() {

    private lateinit var eggLabelPhoto: Uri
    private var eggLabelFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val eggLabelTakeFile: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            eggLabelFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
            eggLabelFilePathFromChrome = null
        }

    private val eggLabelTakePhoto: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                eggLabelFilePathFromChrome?.onReceiveValue(arrayOf(eggLabelPhoto))
                eggLabelFilePathFromChrome = null
            } else {
                eggLabelFilePathFromChrome?.onReceiveValue(null)
                eggLabelFilePathFromChrome = null
            }
        }

    private val BSPDataStore by activityViewModels<BSPDataStore>()


    private val BSPMainViFun by inject<BSPMainViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(MainApplication.MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (BSPDataStore.BSPMainApplicationView.canGoBack()) {
                        BSPDataStore.BSPMainApplicationView.goBack()
                    } else if (BSPDataStore.BSPMainApplicationViList.size > 1) {
                        BSPDataStore.BSPMainApplicationViList.removeAt(
                            BSPDataStore.BSPMainApplicationViList.lastIndex
                        )
                        BSPDataStore.BSPMainApplicationView.destroy()
                        val previousWebView =
                            BSPDataStore.BSPMainApplicationViList.last()
                        eggLabelAttachWebViewToContainer(previousWebView)
                        BSPDataStore.BSPMainApplicationView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (BSPDataStore.feedMixIsFirstCreate) {
            BSPDataStore.feedMixIsFirstCreate = false
            BSPDataStore.feedMixContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return BSPDataStore.feedMixContainerView
        } else {
            return BSPDataStore.feedMixContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BSPDataStore.BSPMainApplicationViList.isEmpty()) {
            BSPDataStore.BSPMainApplicationView = BSPMainApplicationVi(
                requireContext(),
                object :
                    BSPCallBack {
                    override fun feedMixHandleCreateWebWindowRequest(BSPMainApplicationVi: BSPMainApplicationVi) {
                        BSPDataStore.BSPMainApplicationViList.add(
                            BSPMainApplicationVi
                        )
                        BSPDataStore.BSPMainApplicationView =
                            BSPMainApplicationVi
                        BSPMainApplicationVi.eggLabelSetFileChooserHandler { callback ->
                            eggLabelHandleFileChooser(callback)
                        }
                        eggLabelAttachWebViewToContainer(BSPMainApplicationVi)
                    }

                },
                eggLabelWindow = requireActivity().window
            ).apply {
                eggLabelSetFileChooserHandler { callback ->
                    eggLabelHandleFileChooser(callback)
                }
            }
            BSPDataStore.BSPMainApplicationView.eggLabelFLoad(
                arguments?.getString(BuildStepsProLoadingSplashFragment.FEED_MIX_D) ?: ""
            )
            BSPDataStore.BSPMainApplicationViList.add(BSPDataStore.BSPMainApplicationView)
            eggLabelAttachWebViewToContainer(BSPDataStore.BSPMainApplicationView)
        } else {
            BSPDataStore.BSPMainApplicationViList.forEach { webView ->
                webView.eggLabelSetFileChooserHandler { callback ->
                    eggLabelHandleFileChooser(callback)
                }
            }
            BSPDataStore.BSPMainApplicationView =
                BSPDataStore.BSPMainApplicationViList.last()

            eggLabelAttachWebViewToContainer(BSPDataStore.BSPMainApplicationView)
        }
    }

    private fun eggLabelHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        eggLabelFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    eggLabelTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                1 -> {
                    eggLabelPhoto = BSPMainViFun.eggLabelSavePhoto()
                    eggLabelTakePhoto.launch(eggLabelPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                callback?.onReceiveValue(null)
                eggLabelFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun eggLabelAttachWebViewToContainer(w: BSPMainApplicationVi) {
        BSPDataStore.feedMixContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            BSPDataStore.feedMixContainerView.removeAllViews()
            BSPDataStore.feedMixContainerView.addView(w)
        }
    }


}