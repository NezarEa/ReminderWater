# Add project-specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# Documentation:
# https://developer.android.com/studio/build/shrink-code

# ----------------------------------
# Android and Firebase
# ----------------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class androidx.lifecycle.ViewModel { *; }
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# ----------------------------------
# Navigation Component
# ----------------------------------
-keep class androidx.navigation.** { *; }
-keep class * extends androidx.navigation.fragment.NavHostFragment { *; }

# ----------------------------------
# Keep Fragments, Activities, Views
# ----------------------------------
-keep class com.reminderwater.Auth.** { *; }  # Login/Sign fragments
-keep class com.reminderwater.MainActivity { *; }
-keep class com.reminderwater.SplashScreen.SplashScreen { *; }
-keep class com.reminderwater.Dashbord.WaterGlassView { *; }
-keep class com.reminderwater.OnBoarding.** { *; }  # ViewPager/First/Second/Third

# ----------------------------------
# RecyclerView Adapters and ViewHolders
# ----------------------------------
-keep class com.reminderwater.Dashbord.TimeSlotAdapter { *; }
-keep class com.reminderwater.Dashboard.WaterHistoryAdapter { *; }
-keep public class androidx.recyclerview.widget.RecyclerView$Adapter { *; }
-keep public class androidx.recyclerview.widget.RecyclerView$ViewHolder { *; }

# ----------------------------------
# ViewModels and Factories
# ----------------------------------
-keep class com.reminderwater.Dashboard.Data.WaterViewModel { *; }
-keep class com.reminderwater.Dashbord.Data.WaterViewModelFactory { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ----------------------------------
# Data Classes (Room Entities, Repositories)
# ----------------------------------
-keep class com.reminderwater.Dashboard.Data.** { *; }  # Entities like UserData, WaterEntry
-keep class com.reminderwater.Dashboard.Data.WaterRepository { *; }

# ----------------------------------
# Workers (WorkManager)
# ----------------------------------
-keep class com.reminderwater.Dashbord.NotificationWorker { *; }
-keep class * extends androidx.work.Worker { *; }

# ----------------------------------
# MPAndroidChart (For BarChart)
# ----------------------------------
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# ----------------------------------
# Retrofit & Gson (If used)
# ----------------------------------
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# ----------------------------------
# General Rules
# ----------------------------------
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class **.R$* { public static <fields>; }
-dontwarn androidx.room.**  # Suppress Room warnings if needed
