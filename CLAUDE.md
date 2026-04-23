# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## About

Quick-Wallet is an Android app for tracking money lent to or borrowed from contacts, with transaction history, notifications, balance tracking, and a home screen widget. Published on Google Play as `com.rose.quickwallet`.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Install on connected device
./gradlew installDebug
```

## Architecture

Traditional Android MVC — no MVVM/LiveData/ViewModel. UI lives in Activities and Fragments; data access goes through a ContentProvider.

**Data flow**: Fragments/Adapters → `DatabaseHelper` → `QuickWalletProvider` (ContentProvider) → SQLite (`QuickWallet.db`).

Each contact gets its own dynamically-created transaction history table in the database (created in `DatabaseOpenHelper`).

### Key packages under `app/src/main/java/com/rose/quickwallet/`

| Package | Purpose |
|---|---|
| `transactions/` | Core UI — `MainActivity`, `TransactionsFragment`, `AddNewTransactionFragment`, `DetailsFragment` |
| `transactions/data/` | Data layer — `DatabaseHelper`, `DatabaseOpenHelper`, `QuickWalletProvider`, `QuickWalletContract` |
| `myWallet/` | Secondary expense/income tracker with its own DB (`WalletDatabaseHelper`) |
| `widget/` | Home screen widget (`TransactionsWidgetProvider`, `TransactionsRemoteViewService`) |
| `callbackhelpers/` | Swipe-to-dismiss callbacks (`ItemTouchHelperCallback`, `ItemTouchHelperAdapter`) |
| `tutorial/` | Onboarding screens shown on first launch |
| `quickblox/` | Legacy push notification code (QuickBlox SDK) — mostly commented out, do not enable |

### Key classes

- **`MainActivity`** — Entry point; hosts `NavigationDrawer`, PIN check on resume, `AlarmManager` setup for periodic notifications.
- **`BaseActivity`** — All activities extend this; owns the navigation drawer wiring.
- **`DatabaseHelper`** — All CRUD for contacts and transactions; creates per-contact history tables dynamically.
- **`QuickWalletProvider`** — ContentProvider used by widgets and the main app to query balance data.
- **`RecyclerAdapter`** / **`DetailsRecyclerAdapter`** — Drive the two main list UIs; handle swipe gestures via `ItemTouchHelperCallback`.
- **`AlarmReceiver`** / **`NotificationService`** — Periodic background notifications; interval configurable in Settings (30 min–24 hrs).
- **`EnterPinActivity`** — PIN lock screen; PIN stored in SharedPreferences.
- **`CalcActivity`** — In-app calculator for entering amounts.

### Libraries

- **Android Support Library** (v4, v7, Design, CardView, RecyclerView)
- **Google AdMob** (banner ads shown in main and detail views)
- **QuickBlox SDK 2.3.1** (imported but disabled — do not re-enable without testing)

### Settings (SharedPreferences)

Currency symbol, PIN, notification interval, and first-launch flag are all stored in the default `SharedPreferences`. See `SettingsFragment` for keys.

### Layouts

Tablet-specific layouts exist under `res/layout-sw600dp/`. Both phone and tablet layouts must be kept in sync when changing major UI components.
