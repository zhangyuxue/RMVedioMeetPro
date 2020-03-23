//
//  MeetingClientSdk.h
//  MeetingClientSdk
//  Author: Linq
//  Email: twomsoft@outlook.com
//  Created by Linq on 2020/2/2.
//  Copyright © 2020 twomsoft. All rights reserved.
//

#import <UIKit/UIKit.h>

#import <MeetingClientSdk/Device.h>

NS_ASSUME_NONNULL_BEGIN

// 回调接口
@protocol MeetingCallback<NSObject>
-(void) onMeetingTimer; //定时处理，在主线程上，可以在这里绘制声波指示，调整视频窗口大小等任务
@end

// Sdk 接口 (单例)
@interface MeetingClientSdk : NSObject

+(instancetype) instance;

// 启动
-(BOOL) startup: (id<MeetingCallback>) callb;

// 清理
-(void) cleanup;

// 开始/停止 推流
// 例:
// push(@"fvideo://192.168.0.112:10005/13509391992") 推送流到服务器
// push(null) 停止推送
// index: 推送通道, 0-1, 可以同时向2个地址推送
-(BOOL) push: (uint) index
         uri: (nullable NSString*) uri;

// 视频输出
// sample: 样本，可以来自摄像头的输出，或屏幕采集输出，或其他视频源
-(void) sendVideo: (CMSampleBufferRef)sample;

@end

NS_ASSUME_NONNULL_END
