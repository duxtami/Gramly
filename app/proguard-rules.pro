# =============================================================================
# ProGuard / R8 rules for Scale
# =============================================================================
# This app is pure Kotlin + Jetpack Compose with no reflection, serialization,
# Parcelable, JNI, or dynamic class loading. Compose, AndroidX, and the Kotlin
# stdlib all ship their own consumer rules inside their AARs/JARs, so R8 already
# knows what to keep for them. proguard-android-optimize.txt handles the generic
# cases (enums' values()/valueOf(), native methods, annotations, R fields, etc.).
#
# As a result we only need a tiny amount of app-specific configuration. Keep this
# file lean: every -keep you add is code R8 can no longer shrink or optimize, so
# the release APK gets bigger. Only add rules for things that actually break.
# =============================================================================

# --- Crash-friendly stack traces ---------------------------------------------
# R8 renames and reorders everything. Without these, any crash report (Play
# Console, logcat) shows meaningless obfuscated names. We keep just enough to map
# stack traces back to source, then hide the original file name. This costs only
# a few KB and does NOT prevent obfuscation.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- ViewModels ---------------------------------------------------------------
# AndroidX lifecycle instantiates ViewModels via their constructor at runtime.
# lifecycle-viewmodel already bundles rules for this, but keeping our subclass
# constructors explicitly makes the build robust against library rule changes.
# (Only the constructors are kept; the class name itself is still obfuscated.)
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# --- Aggressive size/obfuscation tuning --------------------------------------
# Flatten all classes into the root package. Shortens names and improves
# locality; safe here because nothing relies on package structure by name.
-repackageclasses ''
# Let R8 widen access modifiers when it helps inlining/merging (pairs with
# -allowaccessmodification already enabled by proguard-android-optimize.txt;
# restated here so the intent is explicit if that file ever changes).
-allowaccessmodification
