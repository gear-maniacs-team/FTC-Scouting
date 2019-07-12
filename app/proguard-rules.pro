# More optimisations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

-keepattributes SourceFile,LineNumberTable

# Keep Data Classes
-keep class net.gearmaniacs.ftcscouting.data.Alliance
-keep class net.gearmaniacs.ftcscouting.data.Match
-keep class net.gearmaniacs.ftcscouting.data.AutonomousData
-keep class net.gearmaniacs.ftcscouting.data.TeleOpData
-keep class net.gearmaniacs.ftcscouting.data.Team
-keep class net.gearmaniacs.ftcscouting.data.User

-keepclassmembers class net.gearmaniacs.ftcscouting.data.Alliance { *; }
-keepclassmembers class net.gearmaniacs.ftcscouting.data.Match { *; }
-keepclassmembers class net.gearmaniacs.ftcscouting.data.AutonomousData { *; }
-keepclassmembers class net.gearmaniacs.ftcscouting.data.TeleOpData { *; }
-keepclassmembers class net.gearmaniacs.ftcscouting.data.Team { *; }
-keepclassmembers class net.gearmaniacs.ftcscouting.data.User { *; }

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
