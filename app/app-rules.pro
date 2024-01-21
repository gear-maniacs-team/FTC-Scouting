# More optimisations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontobfuscate

# Remove Kotlin Null-Checks
-assumenosideeffects class kotlin.jvm.internal.Intrinsics { *; }

-keepattributes SourceFile,LineNumberTable

# Firebase
-keepclassmembers class com.google.firebase.database.GenericTypeIndicator { *; }
-keep class net.gearmaniacs.database.model.** { *; }
