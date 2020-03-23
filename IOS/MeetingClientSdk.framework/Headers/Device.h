//
//  device.h
//  meeting client sdk
//  Created by linq on 2019/6/23.
//  twomsoft@outlook.com
//  Copyright © 2019 workvideo. All rights reserved.
//
#pragma once

#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

// 声波指示 (用于界面上显示波形指示)
typedef struct SoundWave
{
    uint8_t wave[12u];
} SoundWave;

/////////////////////////////////////////////////////////////////

// 图像尺寸
typedef struct Resolution
{
    uint32_t width, height;
} Resolution;

// 摄像头输出回调
@protocol CameraCallback<NSObject>
@required
-(void) OnCameraOutput: (CMSampleBufferRef)sample;
@end

// 摄像头对象 (单例)
@interface Camera :
NSObject<AVCaptureVideoDataOutputSampleBufferDelegate>

+(instancetype) instance;

-(BOOL) open: (id<CameraCallback>) callback;
-(void) close;

-(AVCaptureVideoPreviewLayer*) previewLayer;
-(AVCaptureConnection*) connection;
-(Resolution) getResolution; //返回分辨率

@property(nonatomic) AVCaptureDevicePosition facing;

-(BOOL) isRunning;
-(BOOL) run: (Resolution) resolution;
-(void) stop;
-(BOOL) restart;
-(void) updateOrientation; //刷新方向
@end

/////////////////////////////////////////////////////////////////

// 源id列表
typedef struct SourceList {
    uint32_t count;
    uint64_t list[10]; // source ID list
} SourceList;

// 视频播放对象
@interface VideoPlayer: UIView

// 载入流
// 例:
// load(@"fvideo://www.fff.cc:10005/13509391992");
// load(null) 卸载
-(BOOL) load: (nullable NSString*)uri;

// 返回信号源 (可能有多个，例如混合后的语音)
-(BOOL) getSource: (SourceList*) src;

// 播放/停止
// mask: 层掩码
// 例:
//  playMask(LayerBitAudioHD | LayerBitVideoMedium) 播放声音和视频
//  playMask(LayerBitVideoLowest) 仅播放低清视频
//  playMask(LayerBitAudioHD) 仅播放语音
//  playMask(0) 停止播放
@property(assign,nonatomic) int playMask;
#define LayerBitAudioHD (1u<<8)     //高清语音
#define LayerBitVideoLowest (1u)    //最低清视频
#define LayerBitVideoLow (1u<<1)    //低清视频
#define LayerBitVideoMedium (1u<<2) //中等视频
#define LayerBitVideoHigh (1u<<3)
#define LayerBitVideoHighest (1u<<4)

// 画面分辨率
@property(readonly) Resolution resolution;

// 播放音量
@property(assign,nonatomic) float volume; // [0 - 1.0]

// 声波指示
-(void) getWave: (SoundWave*) wave;

// 设置空白画面 (无信号)
+(BOOL) setBlank: (nullable NSString*)imagePath;
@end

/////////////////////////////////////////////////////////////////

// 声音会话 (单例)
@interface AppAudioSession : NSObject
+(instancetype) instance;

@property(assign,nonatomic) BOOL microphoneOn;   //麦克风开 ?
@property(assign,nonatomic) BOOL speakerOn;      //扬声器开 ?
@property(assign,nonatomic) float recordVolume;  //录音音量 [0 - 1.0]

-(void) getRecordWave: (SoundWave*)wave; //返回录音声波指示

-(BOOL) isPlaying; //是否在播放声音
-(BOOL) isHeadsetPluggedIn; //是否插着耳机
@end

NS_ASSUME_NONNULL_END

/////////////////////////////////////////////////////////////////
