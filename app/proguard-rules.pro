# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep the application class and its members
-keep class com.samyak2403.flashlightmine.** { *; }

# Preserve the special static methods that are required in all enumeration classes.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep all native method names and the names of their classes.
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep setters in Views so that animations can still work.
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# Keep Activity class names for screen navigation
-keep,allowshrinking class * extends android.app.Activity { <init>(); }
-keep,allowshrinking class * extends androidx.appcompat.app.AppCompatActivity { <init>(); }

# Keep custom exceptions
-keep public class * extends java.lang.Exception { <init>(); }

# Keep the BuildConfig
-keep class *.BuildConfig { *; }

# Keep the support library
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Keep attributes for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep any classes used in AndroidManifest.xml
-keep public class * extends android.app.Application { <init>(); }
-keep public class * extends android.app.Service { <init>(); }
-keep public class * extends android.content.BroadcastReceiver { <init>(); }
-keep public class * extends android.content.ContentProvider { <init>(); }

# Keep onClick handlers
-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}