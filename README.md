# HSE Management System - Android

Mobile HSE Management System based on the existing HSE Management System
feature set.

## Current Version

2.0.0 Android Foundation

## Technology

- Native Android
- Kotlin
- Jetpack Compose
- Material 3
- Room SQLite
- Kotlin Coroutines
- GitHub Actions
- JDK 17

## Current Modules

### Dashboard

- Observation count
- Incident count
- Audit count
- CAPA count
- HSE workflow

### HSE Observations

- Observation number
- Observation type
- Category
- Description
- Responsible person
- Priority
- Target date
- Corrective action
- Root cause
- Status

### Incident Management

- Incident register
- Incident type
- Description
- Responsible person
- Priority
- Corrective action
- Root cause
- RCA method

### Audit

- Audit register
- Audit type
- Finding type
- Description/finding
- Responsible person
- Priority
- Corrective action
- Root cause
- Standard / clause

### CAPA

- CAPA register
- Corrective action
- Root cause
- Responsible person
- Priority
- Target date

### Audit Log

The database records creation and deletion actions.

## Database

The application uses a local Room SQLite database:

hse_management.db

The application is offline-first.

## Build

Required:

- JDK 17
- Android SDK 35
- Gradle 8.9

Run:

    gradle test

Then:

    gradle assembleDebug

Then:

    gradle lint

APK location:

    app/build/outputs/apk/debug/app-debug.apk

## GitHub Actions

The workflow is:

    .github/workflows/android.yml

It:

1. Checks out the repository
2. Installs JDK 17
3. Installs Gradle 8.9
4. Runs unit tests
5. Builds the debug APK
6. Runs lint
7. Uploads the APK as a GitHub Actions artifact
8. Uploads the lint report

## Planned Production Features

The Android application will progressively migrate the richer functionality
from the desktop HSE Management System.

Planned modules include:

- Professional STOP Cards
- Observation evidence/photos
- Observation categories
- Observation closeout
- Incident investigation
- 5 Why
- Fishbone/Ishikawa
- ICAM
- Barrier Analysis
- Bow-Tie Analysis
- Fault Tree Analysis
- Causal Tree
- Witness statements
- Investigation timeline
- Incident evidence
- Audit management
- Audit findings
- Auditor/reviewer/approver workflow
- CAPA source linking
- CAPA verification
- Employee master data
- Company master data
- Project/location master data
- HSE categories
- PDF reporting
- Excel reporting
- CSV reporting
- Professional incident reports
- Company logo
- Report footer
- Document numbering
- Backup and restore
- User login
- Roles and permissions
- Audit trail
- Notifications
- Risk assessment
- JSA/JHA
- Permit to Work
- Training and competency
- Contractor management
- Emergency drills
- Environmental management
- Cloud synchronization

## Important

This repository is the Android foundation and is not yet feature-for-feature
equivalent to the 181-page desktop application.

The architecture is intentionally being built so the larger HSE modules can
be added without replacing the database/application foundation.
