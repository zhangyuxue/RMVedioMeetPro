
// stdafx.h : 标准系统包含文件的包含文件，
// 或是经常使用但不常更改的
// 特定于项目的包含文件

#pragma once

#ifndef VC_EXTRALEAN
#define VC_EXTRALEAN            // 从 Windows 头中排除极少使用的资料
#endif

#include "targetver.h"

#define _ATL_CSTRING_EXPLICIT_CONSTRUCTORS      // 某些 CString 构造函数将是显式的

// 关闭 MFC 对某些常见但经常可放心忽略的警告消息的隐藏
#define _AFX_ALL_WARNINGS

#include <afxwin.h>         // MFC 核心组件和标准组件
#include <afxext.h>         // MFC 扩展


#include <afxdisp.h>        // MFC 自动化类



#ifndef _AFX_NO_OLE_SUPPORT
#include <afxdtctl.h>           // MFC 对 Internet Explorer 4 公共控件的支持
#endif
#ifndef _AFX_NO_AFXCMN_SUPPORT
#include <afxcmn.h>             // MFC 对 Windows 公共控件的支持
#endif // _AFX_NO_AFXCMN_SUPPORT

#include <afxcontrolbars.h>     // 功能区和控件条的 MFC 支持

#include <inttypes.h>

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

	int ret = Utf8ToUnicode(utf8, len, wstr, (uint32_t)require);
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
	Utf8Str() : m_len(0), m_cap(1000u)
	{
		m_buf = (char*)malloc(m_cap);
		m_buf[0] = 0;
	}
	Utf8Str(const wchar_t* s, const size_t L) :
		m_buf(nullptr)
	{
		assign(s, L);
	}
	Utf8Str(const wchar_t* s) :
		m_buf(nullptr)
	{
		assign(s, wcslen(s));
	}
	Utf8Str(const CString& s) :
		m_buf(nullptr)
	{
		assign(s, s.GetLength());
	}
	Utf8Str(const Utf8Str& s) : m_cap(0), m_len(0), m_buf(nullptr)
	{
		operator=(s);
	}
	Utf8Str& operator=(const Utf8Str& s)
	{
		if (m_buf)
			free(m_buf);
		m_cap = (s.m_len + 1000u) & (~511u);
		m_len = s.m_len;
		m_buf = (char*)malloc(m_cap);
		memcpy(m_buf, s.m_buf, m_len);
		m_buf[m_len] = 0;
		return *this;
	}
	Utf8Str(const char* sz)
	{
		m_len = (sz) ? strlen(sz) : 0;
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








