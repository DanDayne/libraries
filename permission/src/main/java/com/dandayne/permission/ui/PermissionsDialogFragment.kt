package com.dandayne.permission.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.dandayne.permission.extensions.areAllPermissionsGranted
import com.dandayne.permission.extensions.askPermissions
import com.dandayne.permission.extensions.handlePermissionsResult
import com.dandayne.permission.R

@RequiresApi(Build.VERSION_CODES.M)
class PermissionsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.permissions_required)
            .setPositiveButton(R.string.grant_permissions) { _, _ -> }
            .create()
    }

    override fun onResume() {
        super.onResume()
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            checkMissingPermissions()
        }
    }

    companion object {
        const val REQUEST_CODE_SETTINGS = 100
        const val REQUEST_CODE_SETTINGS_ALL_FILES_ACCESS = 101
        const val REQUEST_CODE_PERMISSIONS = 102
        private const val SCHEME = "package"
        const val TAG = "permissionsDialogFragment"
    }

    private fun handleNormalPermissions(permissions: Array<out String>, grantResults: IntArray) {
        handlePermissionsResult(
            permissions,
            grantResults,
            {
                checkMissingPermissions()
            },
            {
                if (it.deniedForever.isNotEmpty() || it.deniedRationale.isNotEmpty()) {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle(R.string.permissions_required)
                        .setMessage((R.string.permissions_not_granted))
                        .setPositiveButton(R.string.go_to_settings) { dialog, _ ->
                            dialog.dismiss()
                            startActivityForResult(
                                Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.fromParts(SCHEME, activity?.packageName, null)
                                },
                                REQUEST_CODE_SETTINGS
                            )
                        }
                        .show()
                }
            }
        )
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleFileAccessPermission() {
        if (Environment.isExternalStorageManager()) {
            checkMissingPermissions()
        } else {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.all_files_access_title))
                .setMessage(getString(R.string.all_files_access_message))
                .setPositiveButton(R.string.go_to_settings) { dialog, _ ->
                    dialog.dismiss()
                    startActivityForResult(
                        Intent().apply {
                            action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                            data = Uri.fromParts(SCHEME, activity?.packageName, null)
                        },
                        REQUEST_CODE_SETTINGS_ALL_FILES_ACCESS
                    )
                }
                .show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> handleNormalPermissions(permissions, grantResults)
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SETTINGS, REQUEST_CODE_SETTINGS_ALL_FILES_ACCESS ->
                askPermissions(requestCode = REQUEST_CODE_PERMISSIONS)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkMissingPermissions() {
        requireContext().apply {
            when {
                isFileAccessDenied() -> handleFileAccessPermission()
                !areAllPermissionsGranted() ->
                    askPermissions(requestCode = REQUEST_CODE_PERMISSIONS)
                else -> dismiss()
            }
        }
    }

    private fun isFileAccessDenied() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            !Environment.isExternalStorageManager()
}