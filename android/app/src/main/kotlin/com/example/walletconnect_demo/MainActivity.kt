package com.example.walletconnect_demo

import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import android.content.Intent
import android.net.Uri


class MainActivity : FlutterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        flutterEngine?.dartExecutor?.let {
            MethodChannel(it.binaryMessenger, "MethodChannelName").setMethodCallHandler {
                    call,
                    result ->
                if (call.method == "android_version") {
                    ExampleApplication.resetSession()
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(ExampleApplication.config.toWCUri())
                    startActivity(i)
                    result.success("Android ${android.os.Build.VERSION.RELEASE}")
                } else {
                    result.notImplemented()
                }
            }
        }
    }
}
