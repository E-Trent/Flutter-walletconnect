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
    lateinit var sessions: Session
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("oncreate:**@&&#*#(#*#*#(#!(()")
        initMoshi()
        initClient()
        initBridge()
        initSessionStorage()
        initialSetup()
        flutterEngine?.dartExecutor?.let {
            MethodChannel(it.binaryMessenger, "MethodChannelName").setMethodCallHandler {
                    call,
                    result ->
                print(call.method)
                if (call.method == "walletConnect") {
                    resetSession()
                    sessions.addCallback(this)
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(config.toWCUri())
                    startActivity(i)
                    result.success("Android!@@*#&#^^#")
                }else if(call.method == "sendMessage") {
                    ///字符串
                    var str  = "{\"method\": \"personal_sign\",\"params\": [\"0x49206861766520313030e282ac\",\"0x2eB535d54382eA5CED9183899916A9d39e093877\"]}";
                    ///发送请求
                    sendCustomMessage(str);
                    ///打开链接
//                    val i = Intent(Intent.ACTION_VIEW)
//                    i.data = Uri.parse(config.toWCUri())
//                    startActivity(i)
//                    result.success("Android!@@*#&#^^#")
                }else {
                    print(call.method)
                    // result.notImplemented()
                    result.success("找不到对应method")
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
//        nullOnThrow { sessions }?.clearCallbacks()
        val key = ByteArray(32).also { Random().nextBytes(it) }.toNoPrefixHexString()
        config =  Session.Config(UUID.randomUUID().toString(), "https://bridge.walletconnect.org/", key)
        var d = config.toFullyQualifiedConfig();
        sessions = WCSession(d,
            MoshiPayloadAdapter(moshi),
            storage,
            OkHttpTransport.Builder(client, moshi),
            Session.PeerMeta(name = "Example App")
        )
        sessions.offer()
    }

    override fun onMethodCall(call: Session.MethodCall) {

    }

    override fun onStatus(status: Session.Status) {
        println("进入Status");
        when(status) {
            Session.Status.Approved -> {
                println("返回生命周期");
            }
            Session.Status.Closed -> sessionClosed()
            Session.Status.Connected,
            Session.Status.Disconnected,
            is Session.Status.Error -> {
                // Do Stuff
                println("status错误")
            }
        }
    }
    private fun initialSetup() {
        println("进入了initialSetup")
        val session = nullOnThrow { sessions } ?: return
        session.addCallback(this)
        sessionApproved()
    }

    private fun sessionApproved() {
        println("返回了"+sessions.approvedAccounts());

    }

    private fun sessionClosed() {

    }

    ///解析庄给的json字符串  并且使用custom方法发送给钱包
    fun sendCustomMessage(jsonStr:String){
        println("进入了");
        try {
            ///解析成实体类
            var str = Gson().fromJson(jsonStr, argsModel::class.java)
            println(sessions.approvedAccounts());
            val from = sessions.approvedAccounts()?.first()!!
            val txRequest = System.currentTimeMillis()
            val gasPrice = "0x02540be400"
            val gasLimit = "0x9c40"
            sessions.performMethodCall(
                Session.MethodCall.SendTransaction(
                    id = txRequest,
                    from = from,
                    to = from,
                    nonce = "0x0114",
                    gasPrice = gasPrice,
                    gasLimit = gasLimit,
                    value = "0x00",
                    data = "0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675"
                ),
                ::handleResponse
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("wc:")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }catch (e:Exception){
            println("onStart: screenMainTxButton:${e.message}")
        }
    }
    private fun handleResponse(resp: Session.MethodCall.Response) {
       println(resp.result as? String)
    }
}
class argsModel {
    var method: String? = null
    var params: List<*>? = null

    override fun toString(): String {
        return "argsModel(method=$method, params=$params)"
    }
}
