# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Eclipse Paho
-keep class org.eclipse.paho.** { *; }
-dontwarn org.eclipse.paho.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.mbusino.mqttexplorer.data.** { *; }
