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

-keep public class com.turtle.hsun.jumptube.JavaScriptInterface
-keep public class * implements com.turtle.hsun.jumptube.JavaScriptInterface
-keepclassmembers class ccom.turtle.hsun.jumptube.JavaScriptInterface {
    <methods>;
}
#For crashlytics in fabric
-keepattributes SourceFile,LineNumberTable,*Annotation*
-keep public class * extends java.lang.Exception
-printmapping mapping.txt
#For SearchView
-keep class android.support.v7.widget.SearchView { *; }