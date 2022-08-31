//
//  myWalletConnect.swift
//  Runner
//
//  Created by 雨川 on 2022/8/31.
//

import Foundation
import WalletConnectSwift


class MyWalletConnect {
    var walletConnect:WalletConnect!
    var client: Client!
    var session: Session!
    var cr:String!
    ///初始化
    func initWalleConnect(){
        walletConnect = WalletConnect(delegate: self)
        walletConnect.reconnectIfNeeded()
        
        NSLog("开始初始化")
        cr = walletConnect.connect()
        client = walletConnect.client
        session = walletConnect.session
    }
    
    ///链接钱包
    func walleConnectFun(){
        
        let deepLinkUrl = "imtokenv2://wc?uri=\(cr!)"
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            if let url = URL(string: deepLinkUrl), UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url, options: [:], completionHandler: nil)
            } else {
                NSLog("开始初始化检测不支持")
            }
        }
    }
    
    func customRequests(jsonStr:String){
        let jsonData:Data = jsonStr.data(using: .utf8)!
        
        let dict =  try? JSONSerialization.jsonObject(with: jsonData, options: .mutableContainers) as? Dictionary<String, Any>

        try? client.send(.eth_custom(url: walletConnect.session.url,methods:dict?["method"] as! String,param:dict?["params"] as! Array<String>)) { [weak self] response in
            self?.handleReponse(response, expecting: "Gas Price")
        }

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
extension Request {
    static func eth_getTransactionCount(url: WCURL, account: String) -> Request {
        return try! Request(url: url, method: "eth_getTransactionCount", params: [account, "latest"])
    }

    static func eth_gasPrice(url: WCURL) -> Request {
        return Request(url: url, method: "eth_gasPrice")
    }
    
    static func eth_custom(url:WCURL,methods:String,param:Array<String>) -> Request{
        return try! Request(url: url, method: methods, params: param)
    }
}


extension MyWalletConnect: WalletConnectDelegate {
    func failedToConnect() {
        onMainThread { [unowned self] in
            NSLog("didConnect")        }
    }

    func didConnect() {
        onMainThread { [unowned self] in
            print("session+++++==============\(walletConnect.session.url)")
            NSLog("didConnect")
        }
    }

    func didDisconnect() {
        onMainThread { [unowned self] in
            NSLog("didConnect")
        }
    }
}
