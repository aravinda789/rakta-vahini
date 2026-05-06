package com.raktavahini.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object DonorProfile : Screen("donor_profile/{donorId}") {
        fun createRoute(donorId: Long) = "donor_profile/$donorId"
    }
    object RegisterDonor : Screen("register_donor")
    object DonationHistory : Screen("donation_history/{donorId}") {
        fun createRoute(donorId: Long) = "donation_history/$donorId"
    }
}

sealed class BottomNavItem(val route: String, val title: String, val icon: String) {
    object Home : BottomNavItem("home", "Search", "search")
    object Donor : BottomNavItem("donor", "My Profile", "person")
    object History : BottomNavItem("history", "History", "history")
}