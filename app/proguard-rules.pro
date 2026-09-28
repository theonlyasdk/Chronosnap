# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\User\AppData\Local\Android\Sdk\tools\proguard\proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.

# Keep ExifInterface tags and fields if needed
-keepclassmembers class androidx.exifinterface.media.ExifInterface { *; }

# Coil image loader rules
-keep class coil.** { *; }

# Cloudy blur library rules
-keep class com.skydoves.cloudy.** { *; }
