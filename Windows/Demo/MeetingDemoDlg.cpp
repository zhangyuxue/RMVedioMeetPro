
// MeetingDemoDlg.cpp: 实现文件
//

#include "stdafx.h"
#include "MeetingDemo.h"
#include "MeetingDemoDlg.h"
#include "afxdialogex.h"

#include "CLogonDialog.h"
#include "CInputDialog.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

CLogonDialog* pLogonDlg = nullptr;

const UINT WM_MEETING_EVENT = (WM_USER + 101);

// CMeetingDemoDlg 对话框

CMeetingDemoDlg::CMeetingDemoDlg(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_MEETINGDEMO_DIALOG, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CMeetingDemoDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CMeetingDemoDlg, CDialogEx)
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDOK, &CMeetingDemoDlg::OnBnClickedOk)
	ON_WM_TIMER()
	ON_WM_DESTROY()
	ON_BN_CLICKED(IDC_BUTTON1, &CMeetingDemoDlg::OnBnClickedButton1)
	ON_BN_CLICKED(IDC_BUTTON2, &CMeetingDemoDlg::OnBnClickedButton2)
	ON_MESSAGE(WM_MEETING_EVENT, &CMeetingDemoDlg::OnMeetingEvent)
	ON_WM_TIMER()
END_MESSAGE_MAP()

static UINT WINAPI Proc(void* c)
{
	((CMeetingDemoDlg*)c)->EventMonitor();
	return 0;
}

// CMeetingDemoDlg 消息处理程序

BOOL CMeetingDemoDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// 设置此对话框的图标。  当应用程序主窗口不是对话框时，框架将自动
	//  执行此操作
	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	// TODO: 在此添加额外的初始化代码

	// 创建子窗体
	m_messageWnd.Create(IDD_MESSAGE_DIALOG);
	m_roomWnd.Create(IDD_ROOM_DIALOG);
	m_adminWnd.Create(IDD_ADMIN_DIALOG);

	// 启动事件接收线程
	m_running = true;
	m_thread = (HANDLE)_beginthreadex(NULL, 0, Proc, this, 0, NULL);

	// 登录对话
	if (!OnLogin())
	{
		EndDialog(IDCANCEL);
		return FALSE;
	}

	// 探测往返时长
	rc_echo echo;
	echo.clock = getMsecond();
	echo.seq = 0;
	theMeeting->send_echo(&echo);

	// 定时器
	SetTimer(1, 100, NULL);

	return TRUE;  // 除非将焦点设置到控件，否则返回 TRUE
}

BOOL CMeetingDemoDlg::OnLogin()
{
	CLogonDialog logon(this);
	pLogonDlg = &logon;
	BOOL ok = (logon.DoModal() == IDOK);
	pLogonDlg = nullptr;
	if (ok) {
		SetWindowText(theApp.m_phone);
	}
	return ok;
}


// 如果向对话框添加最小化按钮，则需要下面的代码
//  来绘制该图标。  对于使用文档/视图模型的 MFC 应用程序，
//  这将由框架自动完成。

void CMeetingDemoDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // 用于绘制的设备上下文

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// 使图标在工作区矩形中居中
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// 绘制图标
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialogEx::OnPaint();
	}
}

//当用户拖动最小化窗口时系统调用此函数取得光标
//显示。
HCURSOR CMeetingDemoDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

void CMeetingDemoDlg::OnBnClickedOk()
{
	// TODO: 在此添加控件通知处理程序代码
//	CDialogEx::OnOK();
}

void CMeetingDemoDlg::printInfo(const wchar_t* f, ...)
{
	wchar_t buf[1000u];
	va_list ap;
	va_start(ap, f);
	int ret = vswprintf(buf, sizeof(buf), f, ap);
	va_end(ap);
	SetDlgItemText(IDC_STATIC_INFO, buf);
}

void CMeetingDemoDlg::ProcessMeetingEvent(uint32_t what, uint8_t body[], uint32_t size)
{
	switch (what)
	{
	case rc_event_login_success :
		eventReader->read_userid(&theApp.m_uid);
		if (pLogonDlg)
			pLogonDlg->EndDialog(IDOK);
		return;

	case rc_event_login_failed :
		if(pLogonDlg)
		{
			pLogonDlg->onError(eventReader->get_error());
		}
		return;

	case rc_event_offline :
		// 离线了，返回登录界面重新登录
		theApp.m_uid = empty_id;

		m_adminWnd.ShowWindow(SW_HIDE);
		m_messageWnd.ShowWindow(SW_HIDE);
		m_roomWnd.ShowWindow(SW_HIDE);
		ShowWindow(SW_HIDE);

		if (OnLogin()) {
			ShowWindow(SW_SHOW);
		}
		else {
			EndDialog(IDCANCEL);
		}
		return;

	case rc_event_text :
		{
			rc_text text;
			if (eventReader->read_text(&text))
				m_messageWnd.addMessage(&text);
		}
		return;

	case rc_event_echo :
		{
		// 开启了 NEGLE，因此至少2毫秒延迟
			static uint32_t minDelay = -1;

			rc_echo echo;
			if (eventReader->read_echo(&echo)) 
			{
				uint32_t now = getMsecond();
				uint32_t delay = (now - echo.clock)/2; 

				if (delay < minDelay)
					minDelay = delay;

				if(minDelay > 1u && echo.seq < 5u) {
					echo.seq++;
					echo.clock = now;
					theMeeting->send_echo(&echo);
				} 
				else {
					printInfo(L"传输延迟: %u(ms)", minDelay);
				}
			}
		}
		return;

	case rc_event_enter_room :
		{
			//进入房间或返回大厅
			eventReader->read_roomid(&theApp.m_roomid);
			m_roomWnd.onEnter();

			if (theApp.m_roomid == theApp.m_uid && 
				theApp.m_roomid!=empty_id) 
			{
				// 进入自己的房间，显示管理界面
				RECT rect;
				GetWindowRect(&rect);
				POINT pt = { rect.left,rect.top };

				m_adminWnd.GetWindowRect(&rect);
				SIZE size = { rect.right - rect.left, rect.bottom - rect.top };

				rect.left = pt.x + 50;
				rect.right = rect.left + size.cx;
				rect.top = pt.y + 50;
				rect.bottom = rect.top + size.cy;
				m_adminWnd.MoveWindow(&rect);

				m_adminWnd.ShowWindow(SW_NORMAL);
			}
			else {
				m_adminWnd.ShowWindow(SW_HIDE);
			}
		}
		return;

	case rc_event_enter_room_failed :
		{
			//进入房间失败
			theApp.m_roomid = empty_id;

			rc_roomid roomid;

			m_roomWnd.ShowWindow(SW_HIDE);
			m_adminWnd.ShowWindow(SW_HIDE);
			m_messageWnd.ShowWindow(SW_HIDE);

			uint32_t error = eventReader->get_error();
			switch (error)
			{
			case error_code_not_opened:
				printInfo(L"房间未打开");
				break;
			case error_code_not_allowed:
				printInfo(L"需要主人邀请");
				break;
			case error_code_authorize_failed:
				printInfo(L"密码错误");
				break;
			case error_code_password_required:
				printInfo(L"需要密码");
				if (eventReader->read_roomid(&roomid))
				{
					CInputDialog dialog(L"请输入密码");
					if (dialog.DoModal() == IDOK)
					{
						Utf8Str utf8(dialog.mText);
						if(utf8.length())
						theMeeting->enter_room(roomid, utf8, roomid==theApp.m_uid);
					}
				}
				break;
			default:
				printInfo(L"其他错误: %u", error);
				break;
			}
		}
		return;

	case rc_event_user_list :
		// 更新用户列表
		if(true)
		{
			theApp.m_user_list.RemoveAll();

			rc_user u;
			const uint8_t* cur = nullptr;
			while (eventReader->read_user_list(&cur, &u))
			{
				USER_RECORD r(u.uid, u.uri);
				theApp.m_user_list.AddTail(r);
			}
			m_roomWnd.onSpeakerChanged();
		}
		return;

	case rc_event_user_record :
		if(true)
		{
			rc_user_op op;
			if (eventReader->read_user_op(&op))
			{
				// 记录增删改
				if (op.op == record_op_append) {
					USER_RECORD r(op.user.uid, op.user.uri);
					theApp.m_user_list.AddTail(r);
				}
				else {
					POSITION pos = theApp.find_user(op.user.uid);
					if(pos)
					{
						if (op.op == record_op_delete)
							theApp.m_user_list.RemoveAt(pos);
						else
							theApp.m_user_list.GetAt(pos).uri = Utf8Str(op.user.uri);
					}
				}
				m_roomWnd.onSpeakerChanged();
			}
		}
		return;

	case rc_event_channel_list :
		// 发言者改变了
		eventReader->read_speaker_list(theApp.m_speaker);

		m_roomWnd.onSpeakerChanged();

		if (theApp.m_uid == theApp.m_roomid)
			m_adminWnd.onSpeakerList();
		return;
	}
}

void CMeetingDemoDlg::EventMonitor(void)
{
	while (m_running)
	{
		rc_event* ev = theMeeting->event_poll();
		if (ev)
		{
			PostMessage(WM_MEETING_EVENT, 0, (LPARAM)ev);
		}
		else {
			theMeeting->event_wait(100u);
		}
	}
}

LRESULT CMeetingDemoDlg::OnMeetingEvent(WPARAM Wp, LPARAM Lp)
{
	rc_event* ev = (rc_event*)Lp;
	eventReader->load(ev);
	ProcessMeetingEvent(ev->what, ev->body, ev->payload);
	eventReader->load(nullptr);
	ev->release(ev);
	return 0L;
}

void CMeetingDemoDlg::OnDestroy()
{
	CDialogEx::OnDestroy();

	// TODO: 在此处添加消息处理程序代码

	m_running = false;
	WaitForSingleObject(m_thread, INFINITE);
	CloseHandle(m_thread);

	m_messageWnd.DestroyWindow();
	m_roomWnd.DestroyWindow();
	m_adminWnd.DestroyWindow();

	// 登出
	if (theApp.m_uid != empty_id) 
	{
		theMeeting->login(nullptr, empty_id, nullptr);
		Sleep(500u); //等待一会，让底层有时间把登出消息发送出去
	}
}

// 加入会议
void CMeetingDemoDlg::OnBnClickedButton1()
{
	CString str;
	if (GetDlgItemText(IDC_EDIT1, str) < 4)
	{
		printInfo(L"请输入房间ID");
		return;
	}

	rc_roomid roomid = wcstoull(str, NULL, 10);
	if (roomid == empty_id)
	{
		printInfo(L"请输入房间ID");
		return;
	}

	if (!theMeeting->enter_room(roomid, nullptr, roomid == theApp.m_uid))
	{
		printInfo(L"错误");
		return;
	}
}

// 发送文字
void CMeetingDemoDlg::OnBnClickedButton2()
{
	CString to, text;

	if (GetDlgItemText(IDC_EDIT2, to) < 4)
	{
		printInfo(L"手机号错误");
		return;
	}

	rc_userid to_uid = wcstoull(to, NULL, 10);
	if (to_uid == empty_id)
	{
		printInfo(L"手机号错误");
		return;
	}

	CEdit* edit = (CEdit*)GetDlgItem(IDC_EDIT3);
	edit->GetWindowText(text);
	if(text.IsEmpty())
	{
		return;
	}

	Utf8Str str(text);
	if (!theMeeting->send_text(to_uid, str, str.length()))
	{
		printInfo(L"错误");
		return;
	}
	printInfo(L"消息已发出");

	edit->SetWindowText(L"");
	edit->SetFocus();
}

void CMeetingDemoDlg::OnTimer(UINT_PTR nIDEvent)
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值

	m_roomWnd.onTimer();

	CDialogEx::OnTimer(nIDEvent);
}

