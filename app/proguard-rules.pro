# Content is bundled and read-only. Keep R8 from stripping serialization metadata.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.osmartapps.yallanghanee.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.osmartapps.yallanghanee.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
