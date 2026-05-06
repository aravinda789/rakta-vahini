# Rakta-Vahini - Blood Donor Network App Specification

## 1. Project Overview

**Project Name:** Rakta-Vahini (रक्तवाहिनी - Blood Carrier)
**Project Type:** Native Android Application
**Core Functionality:** A privacy-focused emergency blood donor directory that connects hospitals/requester with eligible blood donors based on blood group and 90-day eligibility criteria.

## 2. Technology Stack & Choices

- **Language:** Kotlin 1.9.x
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Architecture:** MVVM with Clean Architecture
- **UI Framework:** Jetpack Compose with Material Design 3
- **Local Database:** Room (SQLite)
- **Dependency Injection:** Hilt
- **Async Operations:** Kotlin Coroutines + Flow
- **Navigation:** Jetpack Navigation Compose

### Key Dependencies
- Jetpack Compose BOM 2024.02.00
- Room 2.6.1
- Hilt 2.50
- Navigation Compose 2.7.7
- Material 3
- Location Services (Google Play Services)

## 3. Feature List

### Donor Management
- Donor registration with name, phone, blood group, location (city/district)
- "I Am Eligible" toggle to indicate availability status
- Last donation date entry
- Donation history log

### Emergency Search
- Blood group filter (A+, A-, B+, B-, O+, O-, AB+, AB-)
- Distance radius filter (10km/20km)
- Auto-filter ineligible donors (donated within last 90 days)
- Display eligible donor list with contact action

### Security & Privacy
- Phone numbers hidden from public list view
- Direct call via Intent (dialer opens, number pre-filled)
- No public exposure of donor data

### Notifications
- "Thank You" notification when donation is logged

### Data Management
- Local SQLite storage via Room
- Pre-populated sample donors for demo

## 4. UI/UX Design Direction

### Visual Style
- Material Design 3 with dynamic color support
- Clean, professional medical/emergency theme
- High contrast for readability in critical situations

### Color Scheme
- Primary: Deep Red (#B71C1C) - representing blood/life
- Secondary: White/Cream for backgrounds
- Accent: Green for eligible, Red for ineligible status

### Layout Approach
- Bottom navigation with 3 tabs:
  1. **Home** - Emergency search
  2. **Donor** - My profile & donation log
  3. **History** - Donation records

### Screen Flow
1. **Splash Screen** - App branding
2. **Home Screen** - Search blood group, radius, results list
3. **Donor Profile Screen** - Register/edit donor info, eligibility toggle
4. **Donation Log Screen** - Add new donation, view history
5. **Donor Detail Dialog** - Contact donor via phone intent