package com.dandayne.permission

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Environment
import com.dandayne.permission.ui.PermissionController

class PermissionChecker(private val permissionController: PermissionController) {

    data class RequestedPermissions(
        val normal: List<String>,
        val special: List<String>
    )

    data class SpecialPermission(
        val name: String,
        val check: () -> Boolean,
        val request: () -> Unit
    )

    @SuppressLint("InlinedApi")
    private val specialPermissions = mutableListOf<SpecialPermission>().apply {

        add(
            SpecialPermission(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    Environment.isExternalStorageManager() else true
                },
                { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    permissionController.handleFileAccessPermission()
                }
            )
        )
    }

    private fun getPermissionsFromManifest(): RequestedPermissions {
        val allPermissions = permissionController.listAllPermissionsFromManifest()
        return RequestedPermissions(
            allPermissions.filterNot { permission ->
                specialPermissions.map { it.name }.contains(permission)
            },
            allPermissions.filter { permission ->
                specialPermissions.map { it.name }.contains(permission)
            }
        )
    }

    fun requestPermissions() {
        permissionController.askPermissions(
            getPermissionsFromManifest().normal.filter {
                !permissionController.isPermissionGranted(it)
            }.toTypedArray()
        )
    }

    fun requestSpecialPermissions() {
        specialPermissions.filter { !it.check() }.forEach { it.request() }
    }

    fun areAllPermissionsGranted(): Boolean =
        areNormalPermissionsGranted() && areSpecialPermissionsGranted()

    fun areNormalPermissionsGranted() = getPermissionsFromManifest().let { permissions ->
        permissions.normal.all { permissionController.isPermissionGranted(it) }
    }

    fun areSpecialPermissionsGranted() = getPermissionsFromManifest().let { permissions ->
        permissions.special.all { permissionName ->
            specialPermissions.find {
                it.name == permissionName
            }?.let { it.check() }
                ?: false
        }
    }

}