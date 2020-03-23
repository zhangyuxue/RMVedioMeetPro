//
//  AppDelegate.m
//  FMeeting
//
//  Created by blue on 2020/3/12.
//  Copyright © 2020 twomsoft. All rights reserved.
//

#import "AppDelegate.h"

@interface AppDelegate ()

@end

static AppDelegate* shared =nil;

@implementation AppDelegate
{
    MeetingClientSdk* mSdk;
    
    UIBackgroundTaskIdentifier mBackTask;
}

+(instancetype) instance
{
    return shared;
}

-(void)onMeetingTimer
{
    [_view_c onTimer];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Override point for customization after application launch.
    shared =self;
    
    _view_c =nil;
    
    mBackTask =UIBackgroundTaskInvalid;
    
    // 启动 Sdk
    mSdk =[MeetingClientSdk instance];
    [mSdk startup: self];
    
    // 设置空白视频
    NSBundle* bundle = [NSBundle mainBundle];
    NSString* path =[NSString stringWithFormat:@"%@/image/background", [bundle bundlePath]];
    [VideoPlayer setBlank: path];
    
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
}

- (void) endBackTask: (BOOL) exit
{
    [[UIApplication sharedApplication] endBackgroundTask:mBackTask];
    mBackTask =UIBackgroundTaskInvalid;
    
    if (exit) {
        [mSdk cleanup];
    }
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    
    mBackTask =[[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:
                ^{
                    [self endBackTask: TRUE];
                }];
}


- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    
    [self endBackTask: FALSE];
}


- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    
    [mSdk startup: self];
}


- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    
    [mSdk cleanup];
}


@end
