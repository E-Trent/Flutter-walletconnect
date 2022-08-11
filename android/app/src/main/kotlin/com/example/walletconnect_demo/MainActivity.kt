package com.example.walletconnect_demo

import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import android.content.Intent
import android.net.Uri
import android.view.View
import com.example.walletconnect_demo.server.BridgeServer
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import org.komputing.khex.extensions.toNoPrefixHexString
import org.walletconnect.Session
import org.walletconnect.impls.*
import org.walletconnect.nullOnThrow
import java.io.File
import java.util.*
import com.google.gson.Gson
import kotlinx.coroutines.launch


class MainActivity : FlutterActivity(),Session.Callback {
//    private var txRequest: Long? = null
//    private val uiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var client: OkHttpClient
    private lateinit var moshi: Moshi
    private lateinit var bridge: BridgeServer
    private lateinit var storage: WCSessionStore
    lateinit var config: Session.Config
    lateinit var session: Session
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("oncreate:**@&&#*#(#*#*#(#!(()")
        initMoshi()
        initClient()
        initBridge()
        initSessionStorage()
//        initialSetup()
        flutterEngine?.dartExecutor?.let {
            MethodChannel(it.binaryMessenger, "MethodChannelName").setMethodCallHandler {
                    call,
                    result ->
                if (call.method == "android_version") {
                    resetSession()
                    session.addCallback(this)
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(config.toWCUri())
                    startActivity(i)
                    result.success("Android ${android.os.Build.VERSION.RELEASE}")
                } else {
                    result.notImplemented()
                }
            }
        }
    }

    private fun initClient() {
        client = OkHttpClient.Builder().build()
    }

    private fun initMoshi() {
        moshi = Moshi.Builder().build()
    }


    private fun initBridge() {
        bridge = BridgeServer(moshi)
        bridge.start()
    }

    private fun initSessionStorage() {
        storage = FileWCSessionStore(
            File(cacheDir, "session_store.json").apply {
                createNewFile()
            },
            moshi
        )
    }

    private fun resetSession() {
        nullOnThrow { session }?.clearCallbacks()
        val key = ByteArray(32).also { Random().nextBytes(it) }.toNoPrefixHexString()
        config = Session.Config(UUID.randomUUID().toString(), "https://bridge.walletconnect.org/", key)
        session = WCSession(config,
            MoshiPayloadAdapter(moshi),
            storage,
            OkHttpTransport.Builder(client, moshi),
            Session.PeerMeta(name = "Example App")
        )
        session.offer()
    }

    override fun onMethodCall(call: Session.MethodCall) {

    }

    override fun onStatus(status: Session.Status) {
        println("进入Status");
        when(status) {
            Session.Status.Approved -> sessionApproved()
            Session.Status.Closed -> sessionClosed()
            Session.Status.Connected,
            Session.Status.Disconnected,
            is Session.Status.Error -> {
                // Do Stuff
            }
        }
    }

    private fun sessionApproved() {
        println("返回了");
    }

    private fun sessionClosed() {

    }

    ///解析庄给的json字符串  并且使用custom方法发送给钱包
    fun sendCustomMessage(jsonStr:String){
        ///解析成实体类
       var str = Gson().fromJson(jsonStr,argsModel::class.java);
        session.performMethodCall(
            Session.MethodCall.Custom(
                System.currentTimeMillis(),
                 str.method!!,
               str.params!!,
            ),
            ::handleResponse
        )

    }
    private fun handleResponse(resp: Session.MethodCall.Response) {
       println(resp.result as? String)
    }
}
class argsModel {
    var method: String? = null
    var params: List<Any>? = null

    override fun toString(): String {
        return "argsModel(method=$method, params=$params)"
    }
}
