package com.raktavahini.data.model

enum class BloodGroup(val displayName: String) {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-");

    companion object {
        val ALL_BLOOD_GROUPS = entries.toList()

        fun fromDisplayName(name: String): BloodGroup? {
            return entries.find { it.displayName == name }
        }
    }
}