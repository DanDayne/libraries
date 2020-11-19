package com.dandayne.permission.extensions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.PermissionChecker

fun Context.isPermissionGranted(permission: String) = PermissionChecker.checkSelfPermission(
        this,
        permission
    ) == PermissionChecker.PERMISSION_GRANTED

fun Context.getAllPermissionsFromManifest() =
    packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        ?.requestedPermissions
        ?.toMutableList()
        ?: mutableListOf()