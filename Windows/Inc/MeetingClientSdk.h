#pragma once

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>

#ifdef MEETINGCLIENTSDK_EXPORTS
#define SDKAPI extern "C" __declspec(dllexport)
#else
#define SDKAPI extern "C" __declspec(dllimport)
#endif

#else
#define SDKAPI extern "C"
#endif

#include "Meeting.h"
#include "Media.h"

// 事件
struct rc_event
{
	uint32_t what;
	uint32_t payload;	//参数长度
	uint8_t* body;		//参数

	typedef void (*free_func)(rc_event*);
	free_func release;
};

// 线路延迟探测
struct rc_echo
{
	uint32_t seq;
	uint32_t clock;
};

// 房间管理接口
struct rc_room_admin
{
	// 设置发言列表 (主持模式下管理员可以指定由谁发言)
	virtual void set_speaker(rc_speaker_list) = 0;

	// 设置邀请名单 (邀请模式时，只有名单内的用户可以进入房间)
	virtual void set_invite(rc_userid list[], uint32_t count) = 0;

	// 设置房间属性
	virtual void set_room_flag(uint32_t flag, const char* passwd) = 0;

	// 设置发言模式
	virtual void set_speak_mode(rc_speak_mode) = 0;
};

// 会议接口
struct rc_meeting
{
	enum iid {
		iid_room_admin =20190105,		//房间管理
		iid_event_reader =20190106,		//事件参数访问
		iid_media_capturer =20190107,	//信号采集
		iid_device_manager =20190108,	//设备管理
	};
	// 获取其他接口
	virtual void* query_api(uint32_t iid) = 0;

	// 启动SDK
	virtual rc_bool startup(void) = 0;

	// 关闭SDK
	virtual void cleanup(void) = 0;

	// 登录
	virtual rc_bool login(const char* server_addr, rc_userid uid, const char* passwd) = 0;

	// 发送文字
	virtual rc_bool send_text(rc_userid to, const char* text, int chars) = 0;

	// 发送回声
	// 服务端收到后会立即返回，客户端收到应答后，可以计算往返延迟(RTT)，也可以用于分隔多次请求
	virtual rc_bool send_echo(rc_echo*) = 0;

	// 读事件
	virtual rc_event* event_poll() = 0;
	virtual rc_bool event_wait(uint32_t timeo_ms) = 0;

	// 进入房间/大厅 (roomid=empty_id 表示大厅)
	virtual rc_bool enter_room(rc_roomid, const char* passwd, rc_bool is_owner) = 0;
};

// 返回主接口
SDKAPI rc_meeting* getMeeting(void);

// 返回毫秒精度的时间
SDKAPI uint32_t getMsecond(void); 

///// 事件ID /////

// 登录成功了/失败了/离线了
#define rc_event_login_success (10)
#define rc_event_login_failed (11) //登录失败了
#define rc_event_offline (12)	//离线了

// 收到文本消息
#define rc_event_text (20)

// 收到心跳应答 (可以检测RTT和分隔多个异步请求)
#define rc_event_echo (21)

// (我) 进入某个房间 (当 roomid=empty_id 时表示返回大厅)
#define rc_event_enter_room (30)
#define rc_event_enter_room_failed (31)

// 用户列表 (房间内的)
#define rc_event_user_list (32)

// 用户列表 (增删改) 事件
#define rc_event_user_record (33)

// 通道列表 (发言者列表)
#define rc_event_channel_list (34)

struct rc_text
{
	char* str;
	uint32_t chars;
	rc_userid from, to;
};

struct rc_user
{
	rc_userid uid;
	const char* uri; // 流地址
};

struct rc_user_op
{
	record_op op;
	rc_user user;
};

// 事件参数访问
struct rc_event_reader
{
	virtual void load(rc_event*) = 0;

	// 错误号
	virtual uint32_t get_error(void) = 0;

	// 返回用户记录操作 (增删改)
	virtual rc_bool read_user_op(rc_user_op*) = 0; // 当 rc_event_user_record

	// 返回消息
	virtual rc_bool read_text(rc_text* msg) = 0;

	// 返回回声信息
	virtual rc_bool read_echo(rc_echo* echo) = 0;

	// 返回发言列表
	virtual rc_bool read_speaker_list(rc_speaker_list) = 0;

	// 遍历用户列表 (rc_event_user_list)
	// *cursor 初始为 null，每次调用后会被改成后一个记录的指针
	virtual rc_bool read_user_list(const uint8_t** cursor, rc_user* out) = 0;

	// 返回房间ID/用户ID
	virtual rc_bool read_roomid(rc_roomid*) = 0; // 当 rc_event_enter_room
	virtual rc_bool read_userid(rc_userid*) = 0; // 当 rc_status_online
};
