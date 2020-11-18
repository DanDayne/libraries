package com.dandayne.permission.ui

data class PermissionDeniedResult(
    val deniedRationale: List<String>,
    val deniedForever: List<String>
)