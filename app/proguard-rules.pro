# More optimisations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

-keepattributes SourceFile,LineNumberTable

# Keep Data Classes Members
-keepclassmembers class net.gearmaniacs.ftcscouting.model.Alliance { *; }
-keepclassmembers class net.gearmaniacs.ftcscouting.model.Match { *; }
-keepclassmembers class net.gearmaniacs.ftcscouting.model.AutonomousData { *; }
-keepclassmembers class net.gearmaniacs.ftcscouting.model.TeleOpData { *; }
-keepclassmembers class net.gearmaniacs.ftcscouting.model.Team { *; }
-keepclassmembers class net.gearmaniacs.ftcscouting.model.User { *; }

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
