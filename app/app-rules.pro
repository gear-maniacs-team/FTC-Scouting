# More optimisations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove Kotlin Null-Checks
-assumenosideeffects class kotlin.jvm.internal.Intrinsics { *; }

# Crshlythics
-keepattributes SourceFile,LineNumberTable
