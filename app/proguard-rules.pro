# Add project specific ProGuard rules here.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Billing classes
-keep class com.android.billingclient.** { *; }

# Keep Apache POI classes
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.schemas.**
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }

# Keep iText PDF classes
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Keep data classes
-keep class com.zaheer.upireconcilepro.data.model.** { *; }
-keep class com.zaheer.upireconcilepro.data.database.entity.** { *; }

# Kotlin serialization
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
