# Project Plan

Build a complete, single-module Android app called "Photo Date Changer" in Kotlin using Jetpack Compose. The app lets a user pick a photo from their device and change its embedded date/time metadata.

Key Features:
- Photo selection using ActivityResultContracts.PickVisualMedia.
- Display selected photo and its current EXIF date/time metadata.
- Change date/time using Material 3 DatePickerDialog and TimePicker.
- Save new date/time to EXIF tags (DATETIME, DATETIME_ORIGINAL, DATETIME_DIGITIZED) using ExifInterface.
- Handle MediaStore scoped-storage write flow (RecoverableSecurityException handling).
- Material 3 theming from a Cyan (#00BCD4) seed color, supporting dynamic color on Android 12+.
- State management with ViewModel, StateFlow, and Dispatchers.IO.

Tech Stack:
- Kotlin, Jetpack Compose (BOM).
- Material 3 components only.
- androidx.exifinterface:exifinterface.
- Coil for image loading.
- ViewModel, Lifecycle, Activity Compose.

Permissions:
- READ_MEDIA_IMAGES (API 33+) / READ_EXTERNAL_STORAGE (legacy).
- MediaStore write-request APIs for saving.

## Project Brief

# Project Brief: Photo Date Changer

## Features
1. **Photo Selection**: Seamlessly pick images from the device gallery using the system's `PickVisualMedia` contract.
2. **Metadata Visibility**: View embedded EXIF date and time information (Original and Digitized) immediately after selection.
3. **Intuitive Editing**: Modify timestamps using Material 3 `DatePickerDialog` and `TimePicker` for a modern, user-friendly experience.
4. **Scoped Storage Saving**: Persist changes back to the photo's EXIF tags while handling Android's recoverable security exceptions for media writing.

## High-Level Technical Stack
- **Language**: Kotlin
- **UI Architecture**: Jetpack Compose with Material 3 (Cyan seed color)
- **Navigation**: Jetpack Navigation 3 (State-driven approach)
- **Adaptive Layouts**: Compose Material Adaptive library for multi-device support
- **Core Libraries**: 
    - `androidx.exifinterface:exifinterface` for metadata manipulation
    - Coil for efficient image rendering
    - ViewModel & StateFlow for robust state management
    - Kotlin Coroutines for asynchronous background tasks (Dispatchers.IO)

## Implementation Steps

### Task_1_SetupAndTheme: Configure project dependencies (ExifInterface, Coil, Navigation 3, Adaptive UI) and implement Material 3 Cyan theming.
- **Status:** COMPLETED
- **Updates:** Configured project dependencies, implemented Cyan-based Material 3 theme, and set up the basic MainActivity scaffold. Permissions for media reading were also added to the manifest.
- **Acceptance Criteria:**
  - Dependencies for ExifInterface, Coil, and Navigation 3 are added to build.gradle
  - Material 3 theme with Cyan seed color (#00BCD4) is implemented
  - Navigation scaffold for the main screen is set up
  - Build pass

### Task_2_PhotoPickerAndMetadata: Implement photo selection using PickVisualMedia and logic to extract EXIF metadata.
- **Status:** COMPLETED
- **Updates:** Implemented PhotoViewModel for state management, added photo picking using PickVisualMedia, and integrated ExifInterface to extract metadata. Updated UI to display the image and its date/time info.
- **Acceptance Criteria:**
  - PickVisualMedia contract used to select images from gallery
  - Coil is used to display the selected image
  - ExifInterface correctly reads DATETIME_ORIGINAL and DATETIME_DIGITIZED tags
  - UI displays photo and current metadata
  - Build pass

### Task_3_MetadataEditingUI: Build the editing interface using Material 3 Date/Time pickers and ViewModel.
- **Status:** COMPLETED
- **Updates:** Implemented Date and Time pickers using Material 3 components. Updated ViewModel to track pending metadata changes and reflect them in the UI preview. Enabled API desugaring for Java Time support.
- **Acceptance Criteria:**
  - Material 3 DatePickerDialog and TimePicker implemented for editing
  - ViewModel and StateFlow manage the photo and edited metadata state
  - UI reflects selected date/time changes before saving
  - Build pass

### Task_4_SavingAndStorage: Implement EXIF saving logic with Scoped Storage RecoverableSecurityException handling.
- **Status:** COMPLETED
- **Updates:** Implemented metadata saving logic using ExifInterface and Scoped Storage compliant FileDescriptors. Handled RecoverableSecurityException for API 29+ by launching the required intent for write access. Added Snackbar feedback for success and error states.
- **Acceptance Criteria:**
  - New metadata saved to EXIF tags (DATETIME, DATETIME_ORIGINAL, DATETIME_DIGITIZED)
  - RecoverableSecurityException handled for MediaStore write operations
  - App successfully persists changes to the photo file
  - Build pass
- **Duration:** N/A

### Task_5_RunAndVerify: Finalize adaptive layouts and perform a complete verification of the application.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - App supports adaptive layouts for different screen sizes
  - Critic_agent verifies stability and requirement alignment
  - Make sure all existing tests pass
  - App does not crash
  - Build pass
- **StartTime:** 2026-08-06 20:54:20 IST

