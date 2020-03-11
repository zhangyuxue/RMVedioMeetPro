
// MeetingDemo.h: PROJECT_NAME 应用程序的主头文件
//

#pragma once

#ifndef __AFXWIN_H__
	#error "在包含此文件之前包含“stdafx.h”以生成 PCH 文件"
#endif

#include "resource.h"		// 主符号


// CMeetingDemoApp:
// 有关此类的实现，请参阅 MeetingDemo.cpp
//

inline int UnicodeToUtf8(const wchar_t src[], const uint32_t srcLen, char dst[], const uint32_t dstSize)
{
	int chars = ::WideCharToMultiByte(CP_UTF8, 0, src, srcLen, dst, dstSize - 1, NULL, NULL);
	if (chars > 0) {
		dst[chars] = 0;
	}
	else {
		dst[0] = 0;
	}
	return chars;
}

inline int Utf8ToUnicode(const char src[], const uint32_t srcL, wchar_t dst[], const uint32_t dstSize)
{
	int chars = ::MultiByteToWideChar(CP_UTF8, 0, src, srcL, dst, dstSize - 1);
	if (chars > 0) {
		dst[chars] = 0;
	}
	return chars;
}

inline int Utf8ToCStr(const char utf8[], const uint32_t len, CString& out)
{
	size_t require = (len + 2) * sizeof(wchar_t);
	wchar_t *wstr = (wchar_t*)malloc(require);
	if (!wstr)
		return -1;

	int ret = Utf8ToUnicode(utf8, len, wstr, (uint32_t) require);
	if (ret > 0) {
		out.SetString(wstr, ret);
	}
	free(wstr);
	return ret;
}

class Utf8Str
{
	char* m_buf;
	uint32_t m_len, m_cap;
public:
	Utf8Str(): m_len(0),m_cap(1000u)
	{
		m_buf = (char*)malloc(m_cap);
		m_buf[0] = 0;
	}
	Utf8Str(const wchar_t* s, const size_t L): 
		m_buf(nullptr)
	{
		assign(s, L);
	}
	Utf8Str(const wchar_t* s):
		m_buf(nullptr)
	{
		assign(s, wcslen(s));
	}
	Utf8Str(const CString& s):
		m_buf(nullptr)
	{
		assign(s, s.GetLength());
	}
	Utf8Str(const Utf8Str& s): m_cap(0),m_len(0),m_buf(nullptr)
	{
		operator=(s);
	}
	Utf8Str& operator=(const Utf8Str& s)
	{
		if (m_buf)
			free(m_buf);
		m_cap = (s.m_len+1000u) & (~511u);
		m_len = s.m_len;
		m_buf = (char*)malloc(m_cap);
		memcpy(m_buf, s.m_buf, m_len);
		m_buf[m_len] = 0;
		return *this;
	}
	Utf8Str(const char* sz)
	{
		m_len = (sz)? strlen(sz) : 0;
		m_cap = (m_len + 1000u) & (~511u);
		m_buf = (char*)malloc(m_cap);
		memcpy(m_buf, sz, m_len);
		m_buf[m_len] = 0;
	}
	~Utf8Str()
	{
		free(m_buf);
	}
	BOOL assign(const wchar_t* s, const size_t L)
	{
		size_t require = (s) ? (L * 3u + 1u) : 1u;

		if (m_buf)
		{
			if (m_cap < require) {
				free(m_buf);
				m_buf = nullptr;
			}
		}
		if (!m_buf)
		{
			m_cap = (require + 1023u)&(~1023u);
			m_buf = (char*)malloc(m_cap);
			if (!m_buf)
				return FALSE;
		}

		if (!s)
		{
			m_buf[0] = 0;
			m_len = 0;
			return TRUE;
		}

		int ret = UnicodeToUtf8(s, (uint32_t)L, m_buf, m_cap);
		if (ret > 0) {
			m_len = ret;
			return TRUE;
		}
		else {
			m_buf[0] = 0;
			m_len = 0;
			return FALSE;
		}
	}
	const uint32_t length() const
	{
		return m_len;
	}
	operator const char*() const
	{
		return m_buf;
	}
};

void debug_out(const char* fmt, ...);

typedef uint32_t uint;

struct MY_ROOM_DATA
{
	BYTE* ptr;
	UINT len;
};

struct USER_RECORD
{
	rc_userid uid;
	Utf8Str uri; //推流地址

	USER_RECORD& operator=(const USER_RECORD& u)
	{
		uid = u.uid;
		uri = u.uri;
		return *this;
	}
	USER_RECORD(const USER_RECORD& u)
	{
		operator=(u);
	}
	USER_RECORD() { uid = empty_id; }
	USER_RECORD(rc_userid _uid, const char* _uri) : uid(_uid), uri(_uri) {}
};

class CMeetingDemoApp : public CWinApp
{
public:
	CMeetingDemoApp();

	CString m_phone, m_passwd, m_server;
	MY_ROOM_DATA m_my_room;

	// 当前登录账号
	rc_userid m_uid;

	// 当前房间号
	rc_roomid m_roomid;

	// 当前房间的发言通道
	rc_speaker_list m_speaker;

	// 当前房间的用户列表
	CList<USER_RECORD> m_user_list;

	POSITION find_user(rc_userid uid)
	{
		POSITION pos = m_user_list.GetHeadPosition();
		while (pos)
		{
			POSITION cur = pos;
			USER_RECORD& r = m_user_list.GetNext(pos);
			if (r.uid == uid)
				return cur;
		}
		return nullptr;
	}

// 重写
public:
	virtual BOOL InitInstance();

// 实现

	DECLARE_MESSAGE_MAP()
};

extern CMeetingDemoApp theApp;
