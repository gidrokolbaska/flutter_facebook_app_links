package it.remedia.flutter_facebook_app_links


import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import android.content.Intent
import android.os.Bundle
import androidx.annotation.NonNull
import com.facebook.FacebookSdk
import com.facebook.applinks.AppLinkData
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.log
import android.util.Log
/** FlutterFacebookAppLinksPlugin  */
class FlutterFacebookAppLinksPlugin : FlutterPlugin, MethodCallHandler,ActivityAware, PluginRegistry.NewIntentListener {
    private var mContext: Context? = null
    private val mActivity: Activity? = null
    private var deeplinkUrl = ""
    private var methodChannel: MethodChannel? = null
    fun onAttachedToEngine(binding: FlutterPluginBinding) {
        Log.d("tag", "onAttachedToEngine...")
        methodChannel = MethodChannel(binding.getBinaryMessenger(), CHANNEL)
        methodChannel.setMethodCallHandler(this)
        mContext = binding.getApplicationContext()
    }

    fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        methodChannel.setMethodCallHandler(null)
        methodChannel = null
    }

    fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + Build.VERSION.RELEASE)
        } else if (call.method.equals("initFBLinks")) {
            val appId: String = call.argument("appId")
            val clientId: String = call.argument("clientId")
            initFBLinks(result, appId, clientId)
        } else if (call.method.equals("getDeepLinkUrl")) {
            result.success(deeplinkUrl)
        } else {
            result.notImplemented()
        }
    }

    /** Plugin registration.  */ // public static void registerWith(Registrar registrar) {
    //   final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL);
    //   FlutterFacebookAppLinksPlugin instance = new FlutterFacebookAppLinksPlugin(registrar);
    //   channel.setMethodCallHandler(instance);
    // }
    // Constructor to initialize plugin inside the 'registerWith' method
    // private FlutterFacebookAppLinksPlugin(PluginRegistry.Registrar registrar){
    //   this.mContext = registrar.activeContext();
    //   this.mActivity = registrar.activity();
    // }
    private fun initFBLinks(result: Result, appId: String, clientId: String) {
        //Log.d("FB_APP_LINKS", "Facebook App Links initialized");

        // final Map<String, String> data = new HashMap<>();
        val resultDelegate: Result = result
        // Get a handler that can be used to post to the main thread
        val mainHandler: Handler = Handler(mContext.getMainLooper())

        // Get user consent
        FacebookSdk.setAutoLogAppEventsEnabled(false)
        FacebookSdk.setApplicationId(appId)
        FacebookSdk.setClientToken(clientId)
        FacebookSdk.setAutoInitEnabled(true)
        FacebookSdk.fullyInitialize()
        FacebookSdk.sdkInitialize(mContext!!)
        AppLinkData.fetchDeferredAppLinkData(mContext
        ) { appLinkData ->
            // Process app link data
            if (appLinkData != null) {
                if (appLinkData.targetUri != null) {
                    Log.d(
                        "FB_APP_LINKS",
                        "Deferred Deeplink Received: " + appLinkData.targetUri
                            .toString()
                    )
                    // data.put("deeplink", appLinkData.getTargetUri().toString());
                    deeplinkUrl = appLinkData.targetUri.toString()
                }

                //Log.d("FB_APP_LINKS", "Deferred Deeplink Received: " + appLinkData.getPromotionCode());
                // if(appLinkData.getPromotionCode()!=null)
                //   data.put("promotionalCode", appLinkData.getPromotionCode());
                // else
                //   data.put("promotionalCode", "");
                val myRunnable = Runnable {
                    if (resultDelegate != null) resultDelegate.success(deeplinkUrl)
                }
                mainHandler.post(myRunnable)
            } else {
                Log.d("FB_APP_LINKS", "Deferred Deeplink Received: null link")
                val myRunnable = Runnable {
                    if (resultDelegate != null) resultDelegate.success(deeplinkUrl)
                }
                mainHandler.post(myRunnable)
            }
        }
    }

    companion object {
        private const val TAG = "FlutterFacebookAppLinksPlugin"
        private const val CHANNEL = "plugins.remedia.it/flutter_facebook_app_links"
    }
}