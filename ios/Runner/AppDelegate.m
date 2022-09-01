#import "AppDelegate.h"
#import <Flutter/Flutter.h>
#import "Runner-Swift.h"

@implementation AppDelegate
- (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions {
  FlutterViewController* controller = (FlutterViewController*)self.window.rootViewController;
  FlutterMethodChannel* batteryChannel = [FlutterMethodChannel
                                          methodChannelWithName:@"MethodChannelName"
                                          binaryMessenger:controller];
    MyWalletConnect* wc =[MyWalletConnect alloc];
    [wc initWalleConnect];
  [batteryChannel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
      if ([@"walletConnect" isEqualToString:call.method]) {
          NSLog(@"进入了方法");
          [wc walleConnectFun];
          result(@"测试");
          
      }else if([@"sendMessage" isEqualToString:call.method]){
          [wc customRequestsWithJsonStr:@"{\"method\": \"personal_sign\",\"params\": [\"0x49206861766520313030e282ac\",\"0x2eB535d54382eA5CED9183899916A9d39e093877\"]}"];
          [wc walleConnectFun];
      } else {
          result(FlutterMethodNotImplemented);
      }
  }];

  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}
@end
