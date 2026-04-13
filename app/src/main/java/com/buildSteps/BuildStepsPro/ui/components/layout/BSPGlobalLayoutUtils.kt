package com.buildSteps.BuildStepsPro.ui.components.layout

import android.R
import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.buildSteps.BuildStepsPro.MainApplication

class BSPGlobalLayoutUtils {

    private var eggLabelMChildOfContent: View? = null


    fun feedMixAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(R.id.content)
        eggLabelMChildOfContent = content.getChildAt(0)

        eggLabelMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun feedMixComputeUsableHeight(): Int {
        val r = Rect()
        eggLabelMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top
    }

    private var feedMixUsableHeightPrevious = 0

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val feedMixUsableHeightNow = feedMixComputeUsableHeight()
        if (feedMixUsableHeightNow != feedMixUsableHeightPrevious) {
            val eggLabelUsableHeightSansKeyboard = eggLabelMChildOfContent?.rootView?.height ?: 0
            val eggLabelHeightDifference =
                eggLabelUsableHeightSansKeyboard - feedMixUsableHeightNow

            if (eggLabelHeightDifference > (eggLabelUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(MainApplication.batchiungInputMode)
            } else {
                activity.window.setSoftInputMode(MainApplication.batchiungInputMode)
            }
            feedMixUsableHeightPrevious = feedMixUsableHeightNow
        }
    }

}