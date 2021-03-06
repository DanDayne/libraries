package com.dandayne.permission.ui

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dandayne.permission.PermissionChecker
import com.dandayne.permission.R
import com.dandayne.permission.extensions.getAllPermissionsFromManifest
import com.dandayne.permission.extensions.isPermissionGranted

class PermissionsDialog : DialogFragment(), PermissionController {

    private val permissionChecker = PermissionChecker(this)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.permissions_required)
            .setPositiveButton(R.string.check_permissions) { _, _ -> }
            .setCancelable(false)
            .create()
    }

    override fun onResume() {
        super.onResume()
        if (permissionChecker.areAllPermissionsGranted()) dismiss()
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (permissionChecker.areAllPermissionsGranted()) onPermissionsGranted()
            else if (!permissionChecker.areNormalPermissionsGranted())
                permissionChecker.requestPermissions()
            else if (!permissionChecker.areSpecialPermissionsGranted())
                permissionChecker.requestSpecialPermissions()
        }
    }

    companion object {
        const val REQUEST_CODE_SETTINGS = 100
        const val REQUEST_CODE_SETTINGS_ALL_FILES_ACCESS = 101
        const val REQUEST_CODE_PERMISSIONS = 102
        private const val SCHEME = "package"
        const val TAG = "permissionsDialogFragment"
    }


    override fun listAllPermissionsFromManifest(): List<String> {
        return requireContext().getAllPermissionsFromManifest()
    }

    override fun isPermissionGranted(permission: String) =
        requireContext().isPermissionGranted(permission)


    @RequiresApi(Build.VERSION_CODES.R)
    override fun handleFileAccessPermission() {
        AlertDialog.Builder(requireContext())
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

    private fun onPermissionsNotGranted() {
        AlertDialog.Builder(requireContext())
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

    private fun onPermissionsGranted() {
        Toast.makeText(
            requireActivity().baseContext,
            R.string.permissions_granted,
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionChecker.areAllPermissionsGranted()) onPermissionsGranted()
        else if (!permissionChecker.areNormalPermissionsGranted()) onPermissionsNotGranted()
        else if (requestCode == REQUEST_CODE_PERMISSIONS)
            permissionChecker.requestSpecialPermissions()

    }

    override fun askPermissions(permissions: Array<out String>) {
        requestPermissions(permissions, REQUEST_CODE_PERMISSIONS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (permissionChecker.areAllPermissionsGranted()) onPermissionsGranted()
        else onPermissionsNotGranted()
    }
}