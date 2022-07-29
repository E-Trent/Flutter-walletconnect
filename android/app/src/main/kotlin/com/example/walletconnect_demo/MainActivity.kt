package com.example.walletconnect_demo

import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        flutterEngine?.dartExecutor?.let {
            MethodChannel(it.binaryMessenger, "MethodChannelName").setMethodCallHandler { call, result ->
                if (call.method == "android_version") {
                    result.success("Android ${android.os.Build.VERSION.RELEASE}")
                } else {
                    result.notImplemented()
                }
            }
        }
    }
}
