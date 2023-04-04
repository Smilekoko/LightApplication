package com.shiki.koko.lightapplication.permission

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

/**
 * @param activityResultCaller [ComponentActivity] 实现该接口
 *
 */
class PermissionLauncher(activityResultCaller: ActivityResultCaller) {

    /**
     * [ActivityResultContracts.RequestMultiplePermissions] 权限协议回调
     * ActivityResultContract<String[], java.util.Map<String, Boolean>>
     * String[] 表示输入权限
     * Map<String, Boolean> 表示权限回调的结果
     * 可以参考范型实现 [ResultLauncher]
     */
    private var permissionCallback: ActivityResultCallback<Map<String, Boolean>>? = null

    //必须在Activity或者Fragment显示之前注册
    //说实话有点傻逼,进过测试,可以直接在Activity的onCreate中初始化
    private val permissionLauncher =
        activityResultCaller.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            permissionCallback?.onActivityResult(result)
        }

    fun launch(
        permissionArray: Array<String>,
        permissionCallback: ActivityResultCallback<Map<String, Boolean>>?
    ) {
        this.permissionCallback = permissionCallback
        permissionLauncher.launch(permissionArray)
    }
}
