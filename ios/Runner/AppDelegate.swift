import UIKit
import Flutter
import WalletConnectSwift
@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    var walletConnect:WalletConnect!
    override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
      walletConnect = WalletConnect(delegate: self)
      walletConnect.reconnectIfNeeded()
      NSLog("开始初始化")
      let connectionUrl = walletConnect.connect()
      let controller : FlutterViewController = window?.rootViewController as! FlutterViewController
      let batteryChannel = FlutterMethodChannel(name: "MethodChannelName",
                                                    binaryMessenger: controller.binaryMessenger)
          batteryChannel.setMethodCallHandler({
            (call: FlutterMethodCall, result: @escaping FlutterResult) -> Void in
              guard call.method == "android_version" else {
                  result(FlutterMethodNotImplemented)
                  return
            }
            
            
//              let deepLinkUrl = "wc://wc?uri=\(connectionUrl)"
              let deepLinkUrl = "imtokenv2://wc?uri=\(connectionUrl)"
                      DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                          if let url = URL(string: deepLinkUrl), UIApplication.shared.canOpenURL(url) {
                              UIApplication.shared.open(url, options: [:], completionHandler: nil)
                              NSLog("开始初始化检测支持")
                          } else {
                              NSLog("开始初始化检测不支持")
                          }
                      }
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
