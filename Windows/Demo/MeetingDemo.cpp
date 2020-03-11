
#include "stdafx.h"
#include "MeetingDemo.h"
#include "MeetingDemoDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// SDK主接口
rc_meeting* theMeeting = nullptr;

// 事件读取接口
rc_event_reader* eventReader = nullptr;

// CMeetingDemoApp

BEGIN_MESSAGE_MAP(CMeetingDemoApp, CWinApp)
	ON_COMMAND(ID_HELP, &CWinApp::OnHelp)
END_MESSAGE_MAP()

// CMeetingDemoApp 构造

CMeetingDemoApp::CMeetingDemoApp()
{
}


// 唯一的 CMeetingDemoApp 对象

CMeetingDemoApp theApp;


// CMeetingDemoApp 初始化

BOOL CMeetingDemoApp::InitInstance()
{
	CWinApp::InitInstance();

	SetRegistryKey(_T("WORKVIDEO"));

	// 从注册表读入参数
	LPCTSTR section = _T("Meeting");
	m_phone = theApp.GetProfileString(section, L"phone", L"");
	m_passwd = theApp.GetProfileString(section, L"passwd", L"");
	m_server = theApp.GetProfileString(section, L"server", L"localhost:10003");

	if (!theApp.GetProfileBinary(section, L"myRoom", &m_my_room.ptr, &m_my_room.len))
	{
		m_my_room.len = 0;
		m_my_room.ptr = nullptr;
	}

	m_uid = empty_id;
	m_roomid = empty_id;
	for (uint i = 0; i < MAX_SPEAKERS; i++)
	{
		m_speaker[i] = empty_id;
	}

	// 初始化SDK
	theMeeting = getMeeting();
	theMeeting->startup();
	eventReader = (rc_event_reader*)theMeeting->query_api(rc_meeting::iid_event_reader);

	// 显示主窗口
	CMeetingDemoDlg dlg;
	m_pMainWnd = &dlg;
	INT_PTR nResponse = dlg.DoModal();
	if (nResponse == IDOK)
	{
		// TODO: 在此放置处理何时用
		//  “确定”来关闭对话框的代码
	}
	else if (nResponse == IDCANCEL)
	{
		// TODO: 在此放置处理何时用
		//  “取消”来关闭对话框的代码
	}
	else if (nResponse == -1)
	{
		TRACE(traceAppMsg, 0, "警告: 对话框创建失败，应用程序将意外终止。\n");
		TRACE(traceAppMsg, 0, "警告: 如果您在对话框上使用 MFC 控件，则无法 #define _AFX_NO_MFC_CONTROLS_IN_DIALOGS。\n");
	}

	// 保存参数到注册表
	theApp.WriteProfileString(section, L"phone", m_phone);
	theApp.WriteProfileString(section, L"passwd", m_passwd);
	theApp.WriteProfileString(section, L"server", m_server);
	theApp.WriteProfileBinary(section, L"myRoom", m_my_room.ptr, m_my_room.len);

	if (m_my_room.ptr)
		delete[] m_my_room.ptr;

	// 退出SDK
	theMeeting->cleanup();

#if !defined(_AFXDLL) && !defined(_AFX_NO_MFC_CONTROLS_IN_DIALOGS)
	ControlBarCleanUp();
#endif
	return FALSE;
}

void debug_out(const char* fmt, ...)
{
	char buf[4000u];
	va_list ap;
	va_start(ap, fmt);
	int ret = vsnprintf(buf, sizeof(buf), fmt, ap);
	va_end(ap);
	if (ret >= sizeof(buf)) {
		buf[sizeof(buf) - 1] = 0;
	}
	OutputDebugStringA(buf);
}
