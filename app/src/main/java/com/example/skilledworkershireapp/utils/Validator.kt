package com.example.skilledworkershireapp.utils

object Validator {

    // Name — letters and spaces only
    fun isValidName(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.isNotEmpty() && trimmed.matches(Regex("^[a-zA-Z ]+$"))
    }

    // Phone — exactly 10 digits after removing "+91"
    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.removePrefix("+91")
            .replace(" ", "")
            .replace("-", "")
            .trim()
        return cleaned.matches(Regex("^[0-9]{10}$"))
    }

    // Location — letters, digits, spaces, commas
    fun isValidLocation(location: String): Boolean {
        val trimmed = location.trim()
        return trimmed.isNotEmpty() && trimmed.matches(Regex("^[a-zA-Z0-9 ,]+$"))
    }

    // Experience — letters, digits, spaces (e.g. "3 years")
    fun isValidExperience(experience: String): Boolean {
        val trimmed = experience.trim()
        return trimmed.isNotEmpty() && trimmed.matches(Regex("^[a-zA-Z0-9 ]+$"))
    }

    // Format phone with +91
    fun formatPhone(phone: String): String {
        val cleaned = phone.removePrefix("+91")
            .replace(" ", "")
            .replace("-", "")
            .trim()
        return "+91$cleaned"
    }
}
