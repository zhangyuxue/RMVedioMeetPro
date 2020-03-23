//
//  ViewController.m
//  FMeeting
//
//  Created by blue on 2020/3/12.
//  Copyright © 2020 twomsoft. All rights reserved.
//

#import "AppDelegate.h"

// 声波指示图
@interface WaveView :UIView
-(void) update: (SoundWave*) wave;
@end
@implementation WaveView
{
    SoundWave _wave;
}
-(void) layoutSubviews
{
    [super layoutSubviews];
    [self setBackgroundColor: [UIColor whiteColor]];
}
-(void) update:(SoundWave *)wave
{
    if (memcmp(wave->wave, _wave.wave, 12u)) {
        _wave = *wave;
        [self setNeedsDisplay];
    }
}

+(CGColorRef) lineColor
{
    static UIColor* color = NULL;
    if (color==NULL)
        color = [UIColor colorWithRed:0.1 green:0.5 blue:0 alpha:1] ;
    return [color CGColor];
}

-(void) drawRect:(CGRect)rect
{
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetFillColorWithColor(context, [UIColor whiteColor].CGColor);
    CGContextFillRect(context, rect);
    
    CGContextSetLineWidth(context, 2);
    CGContextSetStrokeColorWithColor(context, [self.class lineColor] );
    
    const int H =(int)rect.size.height;
    int x =0;
    for (int i=0; i<12; i++) {
        int level =_wave.wave[i];
        int h = (H*level)>>8;
        int y = (H-h)>>1;
        CGContextMoveToPoint(context, x, y);
        CGContextAddLineToPoint(context, x, y+h);
        x+=3;
    }
    CGContextStrokePath(context);
}
@end

/////

@interface ViewController ()

@end

@implementation ViewController
{
    // 视频播放
    VideoPlayer* _player;
    
    // 摄像头
    Camera* _camera;
    
    // 编辑框: 推送地址
    UITextField* _edit_send_uri;
    
    // 编辑框: 接收地址
    UITextField* _edit_play_uri;
    
    // 按钮: 推送
    UIButton* _button_send;
    
    // 按钮: 播放
    UIButton* _button_play;
    
    // 滑动条: 麦克风音量
    UISlider* _slider_mic_vol;
    
    // 滑动条: 播放器音量
    UISlider* _slider_play_vol;
    
    // 声波指示图: 麦克风
    WaveView* _mic_wave_view;
    
    // 声波指示图: 播放器
    WaveView* _play_wave_view;
}

- (UIButton*) newButton: (NSString*) title
{
    UIButton *button = [UIButton buttonWithType:UIButtonTypeSystem];
    [button setTitle:title forState:UIControlStateNormal];
    [self setButtonBorder: button];
    [self.view addSubview: button];
    return button;
}

-(void) setButtonBorder: (UIButton* )button
{
    [button.layer setMasksToBounds:YES];
    [button.layer setCornerRadius:2.0];
    [button.layer setBorderWidth:1.0];
    button.layer.borderColor=[UIColor grayColor].CGColor;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    AppDelegate.instance.view_c =self;
    
    UIButton* btn;

    // 创建摄像头
    _camera =[Camera instance];
    if (![_camera open: self])
        NSLog(@"camera.open failed!");
    else
    {
        _camera.previewLayer.videoGravity =AVLayerVideoGravityResizeAspectFill;
        _camera.previewLayer.opaque =YES;
        [self.view.layer addSublayer:_camera.previewLayer];
        _camera.previewLayer.frame = CGRectMake(2,50,352,288);

        // 按钮: 切换前后摄像头
        btn =[self newButton: @"切换摄像头"];
        btn.frame =CGRectMake(360, 50, 100, 36);
        [self setButtonBorder: btn];
        [btn addTarget:self action:@selector(onSwitchCamera) forControlEvents:UIControlEventTouchDown];
    }
    
    // 找到ui上的控件并关联事件
    for (UIView* c in self.view.subviews)
    {
        switch(c.tag) {
            case 1:
                btn =(UIButton*) c;
                [self setButtonBorder: btn];
                [btn addTarget: self action:@selector(onPush)  forControlEvents:UIControlEventTouchDown];
                btn.frame =CGRectMake(260, 350, 80, 36);
                _button_send =btn;
                break;
            case 2:
                _edit_send_uri =(UITextField*)c;
                _edit_send_uri.frame =CGRectMake(2, 350, 250, 36);
                break;
            case 3:
                btn =(UIButton*)c;
                [self setButtonBorder: btn];
                [btn addTarget: self action:@selector(onPlay)  forControlEvents:UIControlEventTouchDown];
                btn.frame =CGRectMake(260, 700, 80, 36);
                _button_play =btn;
                break;
            case 4:
                _edit_play_uri =(UITextField*)c;
                _edit_play_uri.frame =CGRectMake(2, 700, 250, 36);
                break;
            case 11:
            case 12:
            {
                UISlider* sl =(UISlider*)c;
                [sl setMinimumValue:0];
                [sl setMaximumValue:1.f];
                [sl addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventValueChanged];
                CGRect frame;
                if (11 == c.tag) {
                    _slider_mic_vol =sl;
                    frame =CGRectMake(380, 100, 200, 50);
                }
                else {
                    _slider_play_vol =sl;
                    frame =CGRectMake(380, 450, 200, 50);
                }
                sl.frame =frame;
                sl.value =1.f;
            }
                break;
        }
    }
    
    // 创建播放器
    _player =[[VideoPlayer alloc] initWithFrame: CGRectMake(2, 400, 352, 288)];
    [self.view addSubview: _player];
    
    // 创建声波指示图
    _mic_wave_view =[[WaveView alloc] initWithFrame:CGRectMake(360, 160, 160, 50)];
    [self.view addSubview: _mic_wave_view];
    
    _play_wave_view =[[WaveView alloc] initWithFrame:CGRectMake(360, 510, 160, 50)];
    [self.view addSubview: _play_wave_view];
}

-(void) sliderValueChanged:(id)sender
{
    if(_slider_mic_vol==sender) {
        AppAudioSession.instance.recordVolume =_slider_mic_vol.value;
    } else {
        _player.volume =_slider_play_vol.value;
    }
}

-(void) onPlay
{
    // 开始/停止 播放
    if (_player.playMask == 0) {
        if (![_player load: _edit_play_uri.text]) //设置地址
            NSLog(@"player.load() failed!");
        else {
            _player.playMask =LayerBitAudioHD | LayerBitVideoMedium; //开始播放
            [_button_play setTitle:@"停止" forState:UIControlStateNormal];
        }
    } else {
        _player.playMask =0; //停止播放
        [_button_play setTitle:@"播放" forState:UIControlStateNormal];

// 如果要彻底释放播放器，则用以下代码:
#if 0
        [_player removeFromSuperview];
        _player =nil;
#endif
    }
}

-(void) onPush
{
    // 开始/停止 推送
    if (AppAudioSession.instance.microphoneOn) {
        // 停止推送
        AppAudioSession.instance.microphoneOn =NO;
        [MeetingClientSdk.instance push:0 uri:nil]; //释放发送流
        [_button_send setTitle:@"推送" forState:UIControlStateNormal];
    }
    else {
        // 开始推送
        if (![MeetingClientSdk.instance push: 0 uri:_edit_send_uri.text])
            NSLog(@"invalid uri");
        else {
            AppAudioSession.instance.microphoneOn =YES;
            [_button_send setTitle:@"停止" forState:UIControlStateNormal];
        }
    }
}

-(void) onSwitchCamera
{
    // 切换 前/后 摄像头
    _camera.facing = AVCaptureDevicePositionFront==_camera.facing ? AVCaptureDevicePositionBack : AVCaptureDevicePositionFront;
    
    [_camera updateOrientation];
}

-(void) viewDidAppear:(BOOL)animated
{
    // 启动摄像头
    Resolution res = {352,288};
    if (![_camera run: res])
        NSLog(@"camera.run failed!");
    [_camera updateOrientation];
}

-(void) viewWillDisappear:(BOOL)animated
{
    [_camera stop];
}

// 摄像头输出回调
-(void) OnCameraOutput: (CMSampleBufferRef)sample
{
    [MeetingClientSdk.instance sendVideo: sample];
}

-(void) onTimer
{
    // TODO: 在这里可以调整播放器位置及尺寸
    
    // 绘制声波图
    SoundWave wave;
    [AppAudioSession.instance getRecordWave: &wave];
    [_mic_wave_view update: &wave];
    
    [_player getWave: &wave];
    [_play_wave_view update: &wave];
}
@end
