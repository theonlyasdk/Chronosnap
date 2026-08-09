# Chronosnap - Photo Date Changer

Chronosnap is a lightweight Android application designed to view and modify the EXIF date and time metadata of photos on your device.

## Application Summary

- **Core Functionality**:
  - **Select Photos**: Pick an image using the system's Document Picker (which grants read/write URI access).
  - **Exif Metadata Inspection**: Read and display the image's original creation date, digitized date, and modification date tags.
  - **Modify Metadata**: Edit the date and time using clean, modern material design date and time pickers.
  - **In-Place Write**: Write the updated date/time back to the photo file's EXIF attributes (`TAG_DATETIME`, `TAG_DATETIME_ORIGINAL`, `TAG_DATETIME_DIGITIZED`) using Android's `ExifInterface`.

- **Tech Stack**:
  - **UI Framework**: Jetpack Compose (Material 3).
  - **Architecture**: MVVM pattern using `PhotoViewModel` and Jetpack Flow for UI state management.
  - **Image Loading**: Coil for Jetpack Compose.
  - **Metadata Engine**: `androidx.exifinterface:exifinterface` for reliable metadata operations on files.

## Recent Fixes
- **Write Permission Issue**: Replaced the Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) with the System Document Picker (`ActivityResultContracts.OpenDocument`).
  - *Rationale*: Photo Picker URIs are strictly read-only by design and cannot be opened with `"rw"` access. Changing to the Document Picker allows the app to request and obtain write permission directly for the selected photo URI, allowing seamless in-place editing of EXIF tags without needing high-risk permissions like `MANAGE_EXTERNAL_STORAGE` (All Files Access).
- **UI and TopAppBar Enhancement**: Moved the "Pick Another" and "Save" buttons from the bottom content layout to the TopAppBar actions when a photo is selected.
  - Replaced the "Pick Another" button with a Back button (`Icons.AutoMirrored.Rounded.ArrowBack`) aligned to the left (navigation icon) of the TopAppBar.
  - Added a 1-second success animation on the Save button: changes the background color to green, icon to a checkmark, and text to "Saved" showing a success message without automatically returning the user to the home screen.
  - Added a 3-dot overflow menu (dropdown menu) to the `TopAppBar` containing Settings and About options, powered by a native Android `PopupMenu` styled dynamically with Material theme colors. Configured the 3-dot menu to display exclusively on the main selection screen, and integrated dynamic context theme wrapping to match active dark/light settings.
  - Centered the empty state document picker card vertically and horizontally on the screen for a cleaner initial presentation.
  - Implemented a Material You styled Settings Screen featuring dynamic Switch and List preferences, using smooth slide transitions to navigate back and forth.
  - Integrated a smooth fade-in transition (crossfade) for the selected image preview after it has loaded.
- **Visual Clean-up and Overlays**:
  - Removed the standalone metadata card and overlayed the EXIF metadata text directly on the top-left of the image preview card.
  - Applied a subtle text shadow (`Shadow`) to the overlay EXIF metadata text elements to maximize readability on any image background, keeping the overlay clean and native without blocky background cards or scrim overlays.
  - Removed the redundant "New Metadata Preview" header label and icon from the editor card.
  - Styled the Save button using dynamic Monet theme coloring by default for active editing, and a strong dark green (`#1B5E20`) for success status.
  - Redesigned the screen layout so the image preview card fills the screen height, stretches end-to-end (edge-to-edge) without margins or border radius, and overlays the date/time editor at the bottom with standard margins and `navigationBarsPadding()` to avoid overlapping the navigation bar.
- **Advanced Interactive Refinements**:
  - Implemented multi-touch zoom (pinch-to-zoom) and pan gestures on the image preview using `detectTransformGestures` for detailed inspection, with smooth spring-based snap-back animations when releasing a zoom-out gesture. Added double-tap to toggle zoom and double-tap-and-drag (one-finger zoom) gestures, optimized to run seamlessly without interfering with multi-touch pinch gestures.
  - Allowed the image content to go fully edge-to-edge behind the bottom system navigation bar.
  - Wrapped the bottom date editor panel in a unified glassmorphic overlay using `com.github.skydoves:cloudy` version `0.6.0` (`Modifier.sky()` and `Modifier.cloudy(sky = sky)`) to render a true live frosted backdrop blur over the image content and system navigation bar.
  - Split the date-time preview into two individual clickable cards (Date card and Time card) which open their respective pickers when tapped, removing redundant outline buttons, and styled using `0.9f` opacity on their background cards.
  - Added dynamic 12/24 hour format detection using `DateFormat.is24HourFormat` to output dates/times and customize the TimePicker to display 24h format or 12h format (AM/PM) based on the system locale settings.
  - Wrapped the `TopAppBar` in a Box utilizing `Modifier.cloudy(sky = sky, radius = 20)` to apply a matching backdrop blur over the image content behind the top bar area.
- **View Transitions & Predictive Back**:
  - Implemented smooth iOS-like horizontal slide transitions using Jetpack Compose's `AnimatedContent` for navigating between the photo picker selection home screen and details view.
  - Integrated `BackHandler` support to intercept system back navigation (such as Predictive Back gestures), seamlessly returning the user to the selection screen with slide-back animation.
