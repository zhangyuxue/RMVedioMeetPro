//
//  ViewController.h
//  FMeeting
//
//  Created by blue on 2020/3/12.
//  Copyright © 2020 twomsoft. All rights reserved.
//

#import <UIKit/UIKit.h>

#import <MeetingClientSdk/MeetingClientSdk.h>

@interface ViewController : UIViewController<CameraCallback>

// 定时任务: 绘制声波指示，视频窗口调整(和内容等比例)
-(void) onTimer;

@end

