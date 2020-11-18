package com.dandayne.permission.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.PermissionChecker

fun Context.isPermissionGranted(permission: String) =
    PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED

fun Context.areAllPermissionsGranted() =
    getAllPermissionsFromManifest().all { isPermissionGranted(it) }.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Environment.isExternalStorageManager() && it
        else it
    }

fun Context.getAllPermissionsFromManifest(): List<String> {
    @SuppressLint("InlinedApi")
    val specialPermissions = listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    val allPermissions = packageManager
        .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        ?.requestedPermissions
        ?.toMutableList()
        ?: mutableListOf()

    return allPermissions.filterNot { specialPermissions.contains(it) }
}