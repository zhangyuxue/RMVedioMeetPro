#pragma once

// 媒体相关接口

#ifdef _WIN32

struct rc_device_list
{
#define MaxDeviceCount (10u)
	wchar_t* list[MaxDeviceCount];
	uint32_t total;
	uint32_t selected; //当前选择的设备
};

// 设备管理
struct rc_device_manager
{
	// 摄像头枚举 / 选择
	virtual uint32_t camera_flush(void) = 0;
	virtual rc_bool camera_list(rc_device_list*) = 0; // 返回列表
	virtual rc_bool camera_select(uint32_t index) = 0; // 改变选择

	// 声卡枚举 / 选择
	virtual uint32_t sound_playback_flush(void) = 0;
	virtual rc_bool sound_playback_list(rc_device_list*) = 0;
	virtual rc_bool sound_playback_select(const wchar_t*) = 0;

	virtual uint32_t sound_record_flush(void) = 0;
	virtual rc_bool sound_record_list(rc_device_list*) = 0;
	virtual rc_bool sound_record_select(const wchar_t*) = 0;

	// 音量 (取值范围: 0-100)
	virtual void set_playback_volume(int vol) = 0; //软件播放音量
	virtual int get_playback_volume() = 0;

	virtual void set_record_volume(int vol) = 0; //软件采集音量
	virtual int get_record_volume() = 0;

	virtual BOOL set_mic_volume(int vol) = 0; //硬件采集音量
	virtual int get_mic_volume() = 0;
};
#endif

// 视频信息
struct rc_video_info
{
	uint32_t width, height; //分辨率

	uint32_t fps; //播放帧率

	char codec[8u]; //编码类型: "h265" or "h264"
};

// 声波指示
struct rc_sound_wave
{
	uint8_t wave[12]; //range: [0-255]
} ;

// 信号层 (一个信号源会输出多个层)
enum rc_layer
{
	video_layer_lowest, //最低清 (144P)
	video_layer_low, //低清 (240P)
	video_layer_medium, //中等 (480P)
	video_layer_high, //高清 (720P)
	video_layer_highest, //最高清 (1080P)

	audio_layer_high =8u,	//高清语音
};

enum rc_layer_bit
{
	layer_bit_video_lowest		= (1u << video_layer_lowest),
	layer_bit_video_low			= (1u << video_layer_low),
	layer_bit_video_medium		= (1u << video_layer_medium),
	layer_bit_video_high		= (1u << video_layer_high),
	layer_bit_video_highest		= (1u << video_layer_highest),

	layer_bit_audio				= (1u << audio_layer_high),
};
typedef void* signal_handle;

// 信号源列表 (可能有多个，例如混合后的语音流)
typedef struct source_list
{
	uint32_t count;
	rc_userid list[10u];
} source_list;

// 播放器
struct rc_video_player
{
	// 删除对象
	virtual void release() = 0;

	// 返回源 (信号是谁 ?)
	virtual rc_bool get_source(source_list* src) = 0;

	// 载入信号
	// fvideo:// ... 
	// rtmp:// ...
	virtual rc_bool load(const char* url) = 0;

	// 播放 / 停止
	// mask: 信号掩码
	//		如果只播放语音 (layer_bit_audio)
	//		如果只播放视频(低清) (layer_bit_video_low)
	//		如果语音和视频(中等) (layer_bit_audio | layer_bit_video_medium)
	// 如果停止 (0)
	// 播放时可以切换画面清晰度
	virtual void play(uint32_t mask) = 0;

	// 可以单独设置视频信号
	virtual void set_video_signal(signal_handle) = 0;

	// 返回视频信号
	// index: 
	//	0 = 原始的, load的信号, 如果未load则返回 null
	//	1 = 当前的, set_video_signal的信号
	virtual signal_handle get_video_signal(int index) = 0;

	// 返回视频信息
	// 返回 FALSE 表示还未收到视频
	virtual BOOL get_video_info(rc_video_info*) = 0;

	// 返回声波指示
	virtual void get_sound_wave(rc_sound_wave*) = 0;

	// 重绘窗口 (当从最小化恢复时需要调用该接口立刻重绘，否则会短暂的空白)
	virtual void flush_view(void) = 0;

	// 设置播放音量 (软音量) [0-100]
	virtual void set_volume(int vol) = 0;
};

// 创建视频播放器 (需要提供一个窗口句柄用于渲染视频)
SDKAPI rc_video_player* createPlayer(HWND hwnd);

// 设置空白视频画面
SDKAPI rc_bool setBlankVideo(const uint8_t data[], const uint32_t size);


// 视频源
enum rc_video_source {
	video_src_null,		//空
	video_src_camera,	//摄像头
	video_src_screen,	//屏幕
};

// 媒体采集
struct rc_video_capturer
{
	// 开启/关闭 推送
	// uri: fvideo://192.168.1.122:10002/13509391992
	// index: 发送器索引 [0-1]，可以同时发送2路流
	virtual rc_bool push(const uint32_t index, const char* uri) = 0;

	// 返回摄像头信号 (用于预览)
	virtual signal_handle get_camera_signal(void) = 0;

	// 开启/关闭 摄像头/屏幕采集
	virtual void set_video_source(rc_video_source) = 0;
	virtual rc_video_source get_video_source(void) const = 0;

	// 设置摄像头清晰度
	virtual void set_camera_resolution(rc_layer) = 0;
	virtual rc_layer get_camera_resolution(void) = 0;

	// 开启/关闭 麦克风
	virtual void set_mic_state(rc_onoff) = 0;
	virtual rc_onoff get_mic_state(void) const = 0;
	virtual void get_mic_wave(rc_sound_wave*) = 0;
};
