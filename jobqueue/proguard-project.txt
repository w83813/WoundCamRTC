# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/pierre/Workspace/src/chromium/src/third_party/android_tools/sdk/tools/proguard/proguard-android.txt
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

-keep class !org.itri.** {*;}
-keep class org.itri.woundcamrtc.jsbridge.** {*;}
-keep class org.itri.woundcamrtc.WebviewActivity {*;}


-keep class com.android.internal.R$dimen {*;}
-keep class android.view.View$AttachInfo {*;}
-keep class com.android.org.conscrypt.SSLParametersImpl {*;}
-keep class org.apache.harmony.xnet.provider.jsse.SSLParametersImpl {*;}
-keep class dalvik.system.CloseGuard {*;}
-keep class sun.security.ssl.SSLContextImpl {*;}
-dontwarn **
