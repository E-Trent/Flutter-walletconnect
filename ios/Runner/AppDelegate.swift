import UIKit
import Flutter
import WalletConnectSwift
@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    var walletConnect:WalletConnect!
    var client: Client!
    var session: Session!
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        walletConnect = WalletConnect(delegate: self)
        walletConnect.reconnectIfNeeded()
        
        NSLog("开始初始化")
        let connectionUrl = walletConnect.connect()
        client = walletConnect.client
        session = walletConnect.session
        let controller : FlutterViewController = window?.rootViewController as! FlutterViewController
        let batteryChannel = FlutterMethodChannel(name: "MethodChannelName",
                                                  binaryMessenger: controller.binaryMessenger)
        batteryChannel.setMethodCallHandler({
            (call: FlutterMethodCall, result: @escaping FlutterResult) -> Void in
            guard call.method == "android_version"
            else {
                result(FlutterMethodNotImplemented)
                return
            }
            if(call.method == "walletConnect"){
                
            }else if(true){}else{}
            //              let deepLinkUrl = "wc://wc?uri=\(connectionUrl)"
            
        })
        GeneratedPluginRegistrant.register(with: self)
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    
    func onMainThread(_ closure: @escaping () -> Void) {
        if Thread.isMainThread {
            closure()
        } else {
            DispatchQueue.main.async {
                closure()
            }
        }
    }
    
    func customRequest(jsonStr:String){
        let jsonData:Data = jsonStr.data(using: .utf8)!
        
        let dict =  try? JSONSerialization.jsonObject(with: jsonData, options: .mutableContainers) as? Dictionary<String, Any>

        try? client.send(.eth_custom(url: session.url,methods:dict?["method"] as! String,param:dict?["params"] as! String)) { [weak self] response in
            self?.handleReponse(response, expecting: "Gas Price")
//            NSLog(response.error, CVarArg)
        }
//        try? client.send(Request(url: session.url, method: "", params: [])){ [weak self] response in
//            self?.handleReponse(response, expecting: "Gas Price")
////            NSLog(response.error, CVarArg)
//        }
//        return map!
    }
    
    private func handleReponse(_ response: Response, expecting: String) {
        DispatchQueue.main.async {
            if let error = response.error {
                self.show(UIAlertController(title: "Error", message: error.localizedDescription, preferredStyle: .alert))
                return
            }
            do {
                let result = try response.result(as: String.self)
                self.show(UIAlertController(title: expecting, message: result, preferredStyle: .alert))
            } catch {
                self.show(UIAlertController(title: "Error",
                                            message: "Unexpected response type error: \(error)",
                                            preferredStyle: .alert))
            }
        }
    }
    private func show(_ alert: UIAlertController) {
        alert.addAction(UIAlertAction(title: "Close", style: .cancel))
//        self.present(alert, animated: true)
    }
}
extension Request {
    static func eth_getTransactionCount(url: WCURL, account: String) -> Request {
        return try! Request(url: url, method: "eth_getTransactionCount", params: [account, "latest"])
    }

    static func eth_gasPrice(url: WCURL) -> Request {
        return Request(url: url, method: "eth_gasPrice")
    }
    
    static func eth_custom(url:WCURL,methods:String,param:String) -> Request{
        return try! Request(url: url, method: methods, params: [param])
    }
}


extension AppDelegate: WalletConnectDelegate {
    func failedToConnect() {
        onMainThread { [unowned self] in
            NSLog("didConnect")        }
    }
    
    func didConnect() {
        onMainThread { [unowned self] in
            
            NSLog("didConnect")
        }
    }
    
    func didDisconnect() {
        onMainThread { [unowned self] in
            NSLog("didConnect")
        }
    }
}
