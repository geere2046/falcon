# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\hjx\gitHub\adt-bundle-windows-x86-adt23.0.2\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-libraryjars 'C:\hjx\gitHub\adt-bundle-windows-x86-adt23.0.2\sdk\platforms\android-19\android.jar'

-optimizations !code/simplification/arithmetic
-allowaccessmodification
-repackageclasses ''
-keepattributes *Annotation*
-dontpreverify
-dontwarn android.support.**
-ignorewarnings
#-libraryjars libs/AMap_Location_v2.3.0_20160112.jar
#-libraryjars libs/ksoap2-android-assembly-2.5.4-jar-with-dependencies.jar
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions,InnerClasses,Signature,Deprecated

-keep public class * extends android.app.Activity {
    public void *(android.view.View);
}

-keep public class * extends android.app.Application

-keep public class * extends android.app.Service

-keep public class * extends android.content.BroadcastReceiver

-keep public class * extends android.content.ContentProvider

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
    public void set*(...);
    *** get*();
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

-keepclassmembers class * extends android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.jxtii.wildebeest.services.** { *; }
-keep class com.jxtii.wildebeest.model.** { *; }
-keep class com.jxtii.wildebeest.util.** { *; }
-keep class com.jxtii.wildebeest.webservice.** { *; }
-keep class com.jxtii.wildebeest.bean.RouteFinishBus { *; }

-keepclassmembers class ** {
    public void oreceiveAMapLocation*(**);
		public void receivePointRecordBus*(**);
		public void onEvent*(**);
}