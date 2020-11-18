package com.dandayne.permission.extensions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.dandayne.permission.ui.PermissionDeniedResult

@RequiresApi(Build.VERSION_CODES.M)
fun Fragment.askPermissions(
    vararg permission: String,
    requestCode: Int? = null
) {
    val permissionsToAsk =
        if (permission.isNotEmpty()) permission else context?.getAllPermissionsFromManifest()
            ?.toTypedArray() ?: arrayOf()
    val permissionRequestCode = requestCode ?: 0

    requestPermissions(permissionsToAsk, permissionRequestCode)
}


@RequiresApi(Build.VERSION_CODES.M)
fun Fragment.handlePermissionsResult(
    permissions: Array<out String>,
    grantResults: IntArray,
    onAllPermissionGranted: (List<String>) -> Unit,
    onAnyPermissionDenied: ((PermissionDeniedResult) -> Unit)? = null
) {
    val mappedPermissionResult = permissions.mapIndexedTo(mutableListOf()) { index, permission ->
        permission to grantResults[index]
    }.toMap()
    val allGranted =
        mappedPermissionResult.filter { it.value == PermissionChecker.PERMISSION_GRANTED }
            .map { it.key }
    val (allDeniedRationale, allDeniedForever) = mappedPermissionResult.minus(allGranted)
        .map { it.key }.partition {
            shouldShowRequestPermissionRationale(it)
        }

    if (allDeniedRationale.isNotEmpty() || allDeniedForever.isNotEmpty()) onAnyPermissionDenied?.invoke(
        com.dandayne.permission.ui.PermissionDeniedResult(
            allDeniedRationale,
            allDeniedForever
        )
    ) else onAllPermissionGranted(allGranted)
}