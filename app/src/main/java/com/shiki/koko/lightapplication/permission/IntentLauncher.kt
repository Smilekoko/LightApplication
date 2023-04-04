package com.shiki.koko.lightapplication.permission

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts

/**
 * 对应Intent的处理
 */
class IntentLauncher(activityResultCaller: ActivityResultCaller) {

    private var activityResultCallback: ActivityResultCallback<ActivityResult>? = null

    private val intentLauncher =
        activityResultCaller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult: ActivityResult ->
            activityResultCallback?.onActivityResult(activityResult)
        }

    /**
     * it.resultCode == Activity.RESULT_OK
     */
    fun launch(
        intent: Intent,
        activityResultCallback: ActivityResultCallback<ActivityResult>? = null
    ) {
        this.activityResultCallback = activityResultCallback
        intentLauncher.launch(intent)
    }
}