package it.remedia.flutter_facebook_app_links

import android.os.Build
import android.os.Handler

/** FlutterFacebookAppLinksPlugin  */
class FlutterFacebookAppLinksPlugin : FlutterPlugin, MethodCallHandler {
    private var mContext: Context? = null
    private val mActivity: Activity? = null
    private var deeplinkUrl = ""
    private var methodChannel: MethodChannel? = null
    fun onAttachedToEngine(binding: FlutterPluginBinding) {
        Log.d(TAG, "onAttachedToEngine...")
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
        setAutoLogAppEventsEnabled(false)
        setApplicationId(appId)
        setClientToken(clientId)
        setAutoInitEnabled(true)
        fullyInitialize()
        sdkInitialize(mContext)
        AppLinkData.fetchDeferredAppLinkData(mContext,
            object : AppLinkData.CompletionHandler {
                override fun onDeferredAppLinkDataFetched(appLinkData: AppLinkData?) {
                    // Process app link data
                    if (appLinkData != null) {
                        if (appLinkData.getTargetUri() != null) {
                            Log.d(
                                "FB_APP_LINKS",
                                "Deferred Deeplink Received: " + appLinkData.getTargetUri()
                                    .toString()
                            )
                            // data.put("deeplink", appLinkData.getTargetUri().toString());
                            deeplinkUrl = appLinkData.getTargetUri().toString()
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
        )
    }

    companion object {
        private const val TAG = "FlutterFacebookAppLinksPlugin"
        private const val CHANNEL = "plugins.remedia.it/flutter_facebook_app_links"
    }
}