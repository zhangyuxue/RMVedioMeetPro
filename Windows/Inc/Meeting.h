#pragma once
#include <inttypes.h>

typedef uint64_t rc_userid;
typedef uint64_t rc_roomid;

typedef int rc_bool;

#define EQUAL(a,b) ((a)==(b))

#define empty_id (0u)

// 最多同时发言人数
#define MAX_SPEAKERS (5u)

// 记录操作类型 (用户记录事件)
enum record_op
{
	record_op_append,
	record_op_delete,
	record_op_modify,
};

// 发言模式
enum rc_speak_mode
{
	speak_mode_queuing,	//简单排队模式
	speak_mode_hosting, //主持模式，由主持人决定谁发言
	speak_mode_activity,//语言活跃模式，说话大声者优先
};

// 开关状态
enum rc_onoff
{
	state_off,
	state_on,
};

// 房间属性字
enum rc_room_flag
{
	room_flag_password = (1),	//需要密码
	room_flag_invite = (1 << 1),	//邀请模式 (未被邀请的人不能进入)
};

// 发言者列表
typedef rc_userid rc_speaker_list[MAX_SPEAKERS];

#include "Errorcode.h"
