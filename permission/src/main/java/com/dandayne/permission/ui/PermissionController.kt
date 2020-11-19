package com.dandayne.permission.ui

import android.os.Build
import androidx.annotation.RequiresApi

interface PermissionController {
    fun listAllPermissionsFromManifest(): List<String>
    fun isPermissionGranted(permission: String): Boolean
    fun askPermissions(permissions: Array<out String>)

    @RequiresApi(Build.VERSION_CODES.R)
    fun handleFileAccessPermission()
}