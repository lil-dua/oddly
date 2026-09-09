# kotlinx.serialization generates a `Companion.serializer()` per @Serializable
# class and looks it up reflectively; R8 cannot see that use and would strip it,
# which surfaces at runtime as an empty save file rather than a build error.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class dev.lildua.oddly.** {
    *** Companion;
}
-keepclasseswithmembers class dev.lildua.oddly.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class dev.lildua.oddly.**$$serializer { *; }

# The reminder receivers are named in AndroidManifest.xml, so nothing in code
# references them and R8 would otherwise consider them unreachable.
-keep class dev.lildua.oddly.notifications.ReminderReceiver { *; }
-keep class dev.lildua.oddly.notifications.BootReceiver { *; }
