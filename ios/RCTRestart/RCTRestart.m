
#import "RCTRestart.h"
#import <React/RCTBridge.h>

@interface RCTRestart()

@end

@implementation RCTRestart

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE(RNRestart)

- (void)loadBundle
{
    [_bridge reload];
}

-(NSString*)formatKey:(NSString*)key{
    return [NSString stringWithFormat:@"user.%@", key];
}

RCT_EXPORT_METHOD(Restart) {
    if ([NSThread isMainThread]) {
        [self loadBundle];
    } else {
        dispatch_sync(dispatch_get_main_queue(), ^{
            [self loadBundle];
        });
    }
    return;
}

RCT_EXPORT_METHOD(StoreStatus:(NSString*)status) {
    [[NSUserDefaults standardUserDefaults] setObject:status forKey:[self formatKey:@"appStatus"]];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

RCT_EXPORT_METHOD(GetAppStatus:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    NSString* status = [[NSUserDefaults standardUserDefaults] stringForKey:[self formatKey:@"appStatus"]];
    if (status == nil){
        status = @"normal";
    }
    resolve(status);
}

RCT_EXPORT_METHOD(StoreValue:(NSString*)key value:(NSString*)value) {
    [[NSUserDefaults standardUserDefaults] setObject:value forKey:[self formatKey:key]];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

RCT_EXPORT_METHOD(FetchCache:(NSString*)key resovle:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    NSString* status = [[NSUserDefaults standardUserDefaults] stringForKey:[self formatKey:key]];
    if (status == nil){
        status = @"null";
    }
    resolve(status);
}


RCT_EXPORT_METHOD(RemoveOneKey:(NSString*)key){
    [[NSUserDefaults standardUserDefaults] removeObjectForKey:[self formatKey:key]];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

RCT_EXPORT_METHOD(FetchAllStorageData:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    //NSDictionary* allvalues = [[NSUserDefaults standardUserDefaults] dictionaryRepresentation];
    NSArray *keys = [[[NSUserDefaults standardUserDefaults] dictionaryRepresentation] allKeys];
    if (keys && keys.count > 0){
        NSMutableDictionary* dicts = [NSMutableDictionary dictionaryWithCapacity:keys.count];
        for(NSString* key in keys){
            // your code here
            id value = [[NSUserDefaults standardUserDefaults] valueForKey:key];
            if ([value isKindOfClass:[NSString class]] && [key hasPrefix:@"user."]){
                NSString* newKey = [key substringFromIndex:[@"user." length]];
                [dicts setObject:[[NSUserDefaults standardUserDefaults] valueForKey:key] forKey:newKey];
            }
        }
        NSError* error;
        NSData* jsonData = [NSJSONSerialization dataWithJSONObject:dicts options:NSJSONWritingPrettyPrinted error:&error];
        if (error){
            reject(@"100", @"format json error", error);
        }else {
            NSString* result = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
            resolve(result);
        }
    }else {
        resolve(@"null");
    }

}

RCT_EXPORT_METHOD(ClearAllData){
    
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSDictionary *dic = [userDefaults dictionaryRepresentation];
    for (id  key in dic) {
        if ([key hasPrefix:@"user."]){
            [userDefaults removeObjectForKey:key];
        }
    }
    [userDefaults synchronize];
}
@end