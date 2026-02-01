# Linphone SDK - Keep everything to ensure SIP functionality
-keep class org.linphone.** { *; }
-keep interface org.linphone.** { *; }
-dontwarn org.linphone.**
-dontnote org.linphone.**

# Native methods - Essential for JNI
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * {
    native <methods>;
}

# App SIP classes
-keep class com.callunlimited.sip.** { *; }
-keep interface com.callunlimited.sip.** { *; }

# Data Models & API - CRITICAL for Retrofit/Gson reflection
-keep class com.callunlimited.data.** { *; }
-keep interface com.callunlimited.data.** { *; }
# Explicitly keep CredentialApi methods to preserve signatures
-keep interface com.callunlimited.data.CredentialApi {
    <methods>;
}

# Retrofit 2 & OkHttp 3
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, SourceFile, LineNumberTable, Exceptions

# Gson
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Coroutines & Kotlin (Fixes ClassCastException in Release)
-keep class kotlin.coroutines.Continuation { *; }
-keep class kotlin.Result { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class com.callunlimited.CallUnlimitedApp { *; }
-keep class * extends android.app.Application

# Firebase
-keep class com.google.firebase.** { *; }

# Lifecycle & ViewModel
-keep class androidx.lifecycle.** { *; }

# General Safety
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
