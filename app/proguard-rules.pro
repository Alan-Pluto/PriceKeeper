# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK tools proguard rules.

# Keep Room entities
-keep class com.pricekeeper.app.data.local.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Compose/Lifecycle composition locals used by lifecycle-runtime-compose.
# Lifecycle 2.8+ checks Compose's LocalLifecycleOwner for compatibility via
# reflection. R8 can otherwise optimize/rename this path in release builds and
# make collectAsStateWithLifecycle() crash with:
# "CompositionLocal LocalLifecycleOwner not present".
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    public static * getLocalLifecycleOwner(...);
}
-keep class androidx.lifecycle.compose.LocalLifecycleOwnerKt { *; }
