# Keep Data Classes Members
-keepclassmembers class net.gearmaniacs.core.model.Alliance { *; }
-keepclassmembers class net.gearmaniacs.core.model.Match { *; }
-keepclassmembers class net.gearmaniacs.core.model.AutonomousData { *; }
-keepclassmembers class net.gearmaniacs.core.model.TeleOpData { *; }
-keepclassmembers class net.gearmaniacs.core.model.Team { *; }
-keepclassmembers class net.gearmaniacs.core.model.User { *; }

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
