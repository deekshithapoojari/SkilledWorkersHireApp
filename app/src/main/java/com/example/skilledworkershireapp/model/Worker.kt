package com.example.skilledworkershireapp.model

data class Worker(
    val id: String = "",
    val fullName: String = "",
    val name: String = "",
    val category: String = "",
    val phone: String = "",
    val location: String = "",
    val experience: String = "",
    val role: String = "worker",
    val isAvailable: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = 0L
) {
    // Fall back to name if fullName is empty
    val displayName: String
        get() = if (fullName.isNotBlank()) fullName else name
}
