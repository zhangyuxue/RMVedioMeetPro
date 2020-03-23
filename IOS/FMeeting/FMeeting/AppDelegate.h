//
//  AppDelegate.h
//  FMeeting
//
//  Created by blue on 2020/3/12.
//  Copyright © 2020 twomsoft. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ViewController.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate, MeetingCallback>

+(instancetype) instance;

@property (strong, nonatomic) UIWindow *window;

@property (nonatomic) ViewController* view_c;

@end

