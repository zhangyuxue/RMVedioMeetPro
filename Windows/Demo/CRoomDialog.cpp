// CRoomDialog.cpp: 实现文件
//

#include "stdafx.h"
#include "MeetingDemo.h"
#include "CRoomDialog.h"
#include "afxdialogex.h"

static const UINT VIDEO_WND_ID[5] = {
	IDC_STATIC_V1,
	IDC_STATIC_V2,
	IDC_STATIC_V3,
	IDC_STATIC_V4,
	IDC_STATIC_V5,
};

static const UINT NAME_WND_ID[5] = {
	IDC_STATIC_NAME_1,
	IDC_STATIC_NAME_2,
	IDC_STATIC_NAME_3,
	IDC_STATIC_NAME_4,
	IDC_STATIC_NAME_5,
};

static const UINT SLIDER_WND_ID[5] = {
	IDC_SLIDER1,
	IDC_SLIDER2,
	IDC_SLIDER3,
	IDC_SLIDER4,
	IDC_SLIDER5,
};

static const UINT AUDIO_CHECK_ID[5] = {
	IDC_CHECK_A1,
	IDC_CHECK_A2,
	IDC_CHECK_A3,
	IDC_CHECK_A4,
	IDC_CHECK_A5,
};

static const UINT VIDEO_CHECK_ID[5] = {
	IDC_CHECK_V1,
	IDC_CHECK_V2,
	IDC_CHECK_V3,
	IDC_CHECK_V4,
	IDC_CHECK_V5,
};

#define NAME_HEIGHT (30)
#define SLIDER_VOFFS (8)
#define SLIDER_HEIGHT (26)
#define SLIDER_WIDTH (180)
#define CHECK_WIDTH (62)
#define CHECK_HEIGHT (20)

static DWORD BG_COLOR = RGB(55, 55, 55);

static CScrollBar* sliderBar[MAX_SPEAKERS];

// CRoomDialog 对话框

IMPLEMENT_DYNAMIC(CRoomDialog, CDialogEx)

CRoomDialog::CRoomDialog(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_ROOM_DIALOG, pParent)
{
	m_layout = 0; //显示5个同样大小的画面
	m_main_video = 0; //[0-4] 表示当前主画面
	m_send_ch = -1;
}

CRoomDialog::~CRoomDialog()
{
}

// 发言者改变了
void CRoomDialog::onSpeakerChanged()
{
	int send_ch = -1;

	// 更新视频标题
	CString str;
	rc_speaker_list &list = theApp.m_speaker;
	for (uint i = 0; i < MAX_SPEAKERS; i++)
	{
		if (list[i] == empty_id)
			str = L"";
		else
			str.Format(L"%llu", list[i]);
		SetDlgItemText(NAME_WND_ID[i], str);

		if (list[i] != empty_id) {
			if (list[i] == theApp.m_uid)
				send_ch = i;
			else
			{
				// 载入URI
				POSITION pos = theApp.find_user(list[i]);
				if (pos)
				{
					USER_RECORD& u = theApp.m_user_list.GetAt(pos);
					if (m_player[i]->load(u.uri))
						debug_out("player[%u] load(%s) success\n", i, (const char*)u.uri);
				}
			}
		}
	}

	// 发送通道改变了
	if (send_ch != m_send_ch)
	{
		// 重置音量条
		if (m_send_ch != -1) {
			m_player[m_send_ch]->set_volume(100);
			((CSliderCtrl*)sliderBar[m_send_ch])->SetPos(100);

			// 恢复为解码信号
			m_player[m_send_ch]->set_video_signal(nullptr);
		}
		if (send_ch != -1) {
			int vol = m_device->get_record_volume();
			((CSliderCtrl*)sliderBar[send_ch])->SetPos(vol);
		}
		m_send_ch = send_ch;

		if (m_send_ch != -1)
		{
			// 连接 摄像头预览信号
			m_player[m_send_ch]->set_video_signal(m_capture->get_camera_signal());
		}
	}
}

// 当进入房间/大厅
void CRoomDialog::onEnter()
{
	if (theApp.m_roomid == empty_id)
	{
		// 退出房间，停止播放器
		for (uint i = 0; i < MAX_SPEAKERS; i++)
		{
			m_player[i]->load(nullptr); //卸载信号
			m_player[i]->play(0); //停止
		}

		// 关闭采集
		closeCapture();

		ShowWindow(SW_HIDE);
		return;
	}

	// 进入房间
	CString str;
	str.Format(L"%llu", theApp.m_roomid);
	SetWindowText(str);

	// 初始化推流地址
	// 地址需要改成实际服务器地址
	str.Format(L"fvideo://localhost:10005/%llu", theApp.m_uid);
	SetDlgItemText(IDC_EDIT2, str);

	ShowWindow(SW_NORMAL);

	// 开启播放器
	for (uint i = 0; i < MAX_SPEAKERS; i++)
		m_player[i]->play(layer_bit_audio | layer_bit_video_medium);
	//可以选择4种接收模式
}


void CRoomDialog::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO1, m_videoSrcBox);
}


BEGIN_MESSAGE_MAP(CRoomDialog, CDialogEx)
	ON_BN_CLICKED(IDOK, &CRoomDialog::OnBnClickedOk)
	ON_WM_CLOSE()
	ON_WM_SIZE()
	ON_WM_GETMINMAXINFO()
	ON_BN_CLICKED(IDC_BUTTON3, &CRoomDialog::OnBnClickedButton3)
	ON_WM_DESTROY()
	ON_BN_CLICKED(IDC_BUTTON1, &CRoomDialog::OnBnClickedButton1)
	ON_BN_CLICKED(IDC_BUTTON2, &CRoomDialog::OnBnClickedButton2)
	ON_CBN_SELCHANGE(IDC_COMBO1, &CRoomDialog::OnCbnSelchangeCombo1)
	ON_WM_LBUTTONDBLCLK()
	ON_WM_CTLCOLOR()
	ON_WM_PAINT()
	ON_WM_HSCROLL()
	ON_COMMAND_RANGE(IDC_CHECK_A1, IDC_CHECK_V5, OnCheckClicked)
END_MESSAGE_MAP()


// CRoomDialog 消息处理程序

void CRoomDialog::OnBnClickedOk()
{
	// TODO: 在此添加控件通知处理程序代码
//	CDialogEx::OnOK();
}

// 退出房间
void CRoomDialog::OnClose()
{
	theMeeting->enter_room(empty_id, nullptr, false);

//	CDialogEx::OnClose();
}

// 调整布局
void CRoomDialog::onLayout(int cx, int cy)
{
	if (cx < 400 || cy < 300)
		return;

	CRect rect;
	GetDlgItem(IDC_STATIC_PANEL)->GetWindowRect(&rect);

	SIZE panel_size = { rect.Width(),rect.Height() };
	if (cx < panel_size.cx || cy < panel_size.cy)
		return; // 异常

	const UINT panel_items = 6u;

	const UINT button_id[panel_items] = {
		IDC_BUTTON1,
		IDC_BUTTON2,
		IDC_BUTTON3,
		IDC_COMBO1,
		IDC_STATIC_PUSH,
		IDC_EDIT2
	};
	POINT button_offs[panel_items];
	SIZE button_size[panel_items];
	CWnd* button_pwnd[panel_items];
	for (uint i = 0; i < panel_items; i++) {
		button_pwnd[i] = GetDlgItem(button_id[i]);
		CRect item_rect;
		button_pwnd[i]->GetWindowRect(&item_rect);
		button_offs[i].x = item_rect.left - rect.left;
		button_offs[i].y = item_rect.top - rect.top;
		button_size[i].cx = item_rect.Width();
		button_size[i].cy = item_rect.Height();
	}

	const int margin = 10;

	// 移动面板到窗口底部
	rect.left = (cx - panel_size.cx) / 2;
	rect.bottom = (cy - margin);
	rect.right = rect.left + panel_size.cx;
	rect.top = rect.bottom - panel_size.cy;
	GetDlgItem(IDC_STATIC_PANEL)->MoveWindow(&rect, FALSE);

	for (uint i = 0; i < panel_items; i++)
	{
		RECT item_rect;
		item_rect.left = rect.left + button_offs[i].x;
		item_rect.top = rect.top + button_offs[i].y;
		item_rect.right = item_rect.left + button_size[i].cx;
		item_rect.bottom = item_rect.top + button_size[i].cy;
		button_pwnd[i]->MoveWindow(&item_rect, FALSE);
	}

	// 移动视频窗口
	cy = rect.top;

	if (m_layout == 0) {
		// 5个同等大小的画面

		// 计算每个画面的大小
		int video_cx = (cx - margin * 4) / 3;
		video_cx &= (~7u);
		int video_cy = video_cx * 3 / 4;

		int gap = cy - (NAME_HEIGHT + SLIDER_HEIGHT + SLIDER_VOFFS+ video_cy) * 2;

		if (gap < margin) {
			// 重新计算
			video_cy = (cy - (NAME_HEIGHT+SLIDER_HEIGHT+SLIDER_VOFFS)*2 -margin) / 2;
			video_cx = video_cy * 4 / 3;
			video_cx &= (~7u);
		}

		int x_gap = (cx - margin * 2 - video_cx * 3) / 2;

		RECT video_rect;
		video_rect.top = NAME_HEIGHT;
		video_rect.left = margin;
		for (int i = 0; i < 5; i++)
		{
			video_rect.right = video_rect.left + video_cx;
			video_rect.bottom = video_rect.top + video_cy;
			GetDlgItem(VIDEO_WND_ID[i])->MoveWindow(&video_rect, FALSE);
			m_video_rect[i] = video_rect;

			// 标题
			RECT name_rect = video_rect;
			name_rect.bottom = name_rect.top;
			name_rect.top -= (NAME_HEIGHT-5);
			name_rect.left++;
			CWnd* name_wnd = GetDlgItem(NAME_WND_ID[i]);
			name_wnd->MoveWindow(&name_rect, FALSE);
			name_wnd->ShowWindow(SW_NORMAL);

			// 音量控制
			RECT slider_rect;
			slider_rect.top = video_rect.bottom + SLIDER_VOFFS;
			slider_rect.bottom = slider_rect.top + SLIDER_HEIGHT;
			slider_rect.left = video_rect.left;
			slider_rect.right = slider_rect.left + SLIDER_WIDTH;
			if (slider_rect.right > video_rect.right)
				slider_rect.right = video_rect.right;
			sliderBar[i]->MoveWindow(&slider_rect);
			sliderBar[i]->ShowWindow(SW_NORMAL);

			// 接收开关
			CWnd* audioCheck = GetDlgItem(AUDIO_CHECK_ID[i]);
			CWnd* videoCheck = GetDlgItem(VIDEO_CHECK_ID[i]);
			RECT checkRect;
			checkRect.left = slider_rect.right + 2;
			if (checkRect.left + CHECK_WIDTH + CHECK_WIDTH <= video_rect.right+10)
			{
				checkRect.top = slider_rect.top;
				checkRect.bottom = checkRect.top + CHECK_HEIGHT;
				checkRect.right = checkRect.left + CHECK_WIDTH;
				audioCheck->MoveWindow(&checkRect, FALSE);

				checkRect.left = checkRect.right ;
				checkRect.right = checkRect.left + CHECK_WIDTH;
				videoCheck->MoveWindow(&checkRect, FALSE);

				audioCheck->ShowWindow(SW_NORMAL);
				videoCheck->ShowWindow(SW_NORMAL);
			}
			else {
				// 空间不够显示
				audioCheck->ShowWindow(SW_HIDE);
				videoCheck->ShowWindow(SW_HIDE);
			}

			// 更新视频区域
			if (i == 2) {
				video_rect.left = margin;
				video_rect.top = slider_rect.bottom + NAME_HEIGHT;
			}
			else {
				video_rect.left = video_rect.right + x_gap;
			}
		}
	}
	else {
		// 1个大画面，4个小画面

		// 计算尺寸
		int main_cy = cy * 0.8f;
		int main_cx = (cx - margin * 2)&(~7u);
		int left = (cx - main_cx) / 2;

		int sub_cx = ((main_cx - margin * 3) / 4) & (~7u);
		int sub_cy = (cy - main_cy - margin * 3);

		RECT sub_rect;
		sub_rect.left = left;
		sub_rect.top = margin*2 + main_cy;
		sub_rect.bottom = sub_rect.top + sub_cy;
		int sub_gap = (main_cx - sub_cx * 4) / 3;
		for (int i = 0; i < 5; i++)
		{
			if (i == m_main_video) {
				RECT main_rect = { left,margin,left + main_cx,margin + main_cy };
				GetDlgItem(VIDEO_WND_ID[i])->MoveWindow(&main_rect, FALSE);
				m_video_rect[i] = main_rect;
			}
			else {
				sub_rect.right = sub_rect.left + sub_cx;
				GetDlgItem(VIDEO_WND_ID[i])->MoveWindow(&sub_rect, FALSE);
				m_video_rect[i] = sub_rect;
				sub_rect.left = sub_rect.right + sub_gap;
			}

			// 不显示 音量控制 / 标题 / 接收开关
			GetDlgItem(NAME_WND_ID[i])->ShowWindow(SW_HIDE);
			sliderBar[i]->ShowWindow(SW_HIDE);
			GetDlgItem(AUDIO_CHECK_ID[i])->ShowWindow(SW_HIDE);
			GetDlgItem(VIDEO_CHECK_ID[i])->ShowWindow(SW_HIDE);
		}
	}
	Invalidate();
}

void CRoomDialog::OnSize(UINT nType, int cx, int cy)
{
	CDialogEx::OnSize(nType, cx, cy);

	onLayout(cx, cy);
}


void CRoomDialog::OnGetMinMaxInfo(MINMAXINFO* lpMMI)
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值

	CDialogEx::OnGetMinMaxInfo(lpMMI);

	lpMMI->ptMinTrackSize = POINT{ 800,600 };
}


// 切换布局
void CRoomDialog::OnBnClickedButton3()
{
	m_layout = (m_layout == 0) ? 1 : 0;

	CRect rect;
	GetClientRect(&rect);
	onLayout(rect.Width(),rect.Height());
}

// 设置无信号画面
static void SetBlankVideo()
{
	FILE* fp = fopen("blank", "rb");
	if (!fp)
	{
		setBlankVideo(NULL, 0); //使用随机画面
		return;
	}
	fseek(fp, 0, SEEK_END);
	size_t size = ftell(fp);
	fseek(fp, 0, SEEK_SET);

	uint8_t* buf = (uint8_t*)malloc(size);
	fread(buf, 1, size, fp);
	fclose(fp);

	setBlankVideo(buf, size);
	free(buf);
}


BOOL CRoomDialog::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// 设置空白画面
	SetBlankVideo();

	// 用于绘制声波
	m_dc = this->GetDC();
	m_wave_brush.CreateSolidBrush(RGB(66, 150, 66));
	m_back_brush.CreateSolidBrush(BG_COLOR);

	// 创建播放器
	for (uint i = 0; i < MAX_SPEAKERS; i++) 
	{
		CWnd* wnd = GetDlgItem(VIDEO_WND_ID[i]);
		m_player[i] = createPlayer(wnd->GetSafeHwnd());
	}

	// 获得采集接口
	m_capture = (rc_video_capturer*)theMeeting->query_api(rc_meeting::iid_media_capturer);

	// 获取设备管理接口
	m_device = (rc_device_manager*)theMeeting->query_api(rc_meeting::iid_device_manager);

	// 视频源选择框
	m_videoSrcBox.SetCurSel(video_src_camera); // 默认输出摄像头视频

	// 音量控制条
	for (uint i = 0; i < MAX_SPEAKERS; i++)
	{
		CScrollBar* bar = (CScrollBar*)GetDlgItem(SLIDER_WND_ID[i]);
		sliderBar[i] = bar;

		bar->SetScrollRange(0, 100);
		((CSliderCtrl*)bar)->SetPos(100);

		m_player[i]->set_volume(100);
	}

	// 接收开关
	for (uint i = 0; i < MAX_SPEAKERS; i++)
	{
		CButton* checkBox = (CButton*)GetDlgItem(AUDIO_CHECK_ID[i]);
		checkBox->SetCheck(BST_CHECKED);

		checkBox = (CButton*)GetDlgItem(VIDEO_CHECK_ID[i]);
		checkBox->SetCheck(BST_CHECKED);
	}

	// 创建设备管理页
	m_device_dlg.Create(IDD_DEVICE_DIALOG);
	return TRUE;  // return TRUE unless you set the focus to a control
				  // 异常: OCX 属性页应返回 FALSE
}

// 设置接收
void CRoomDialog::OnCheckClicked(UINT nID)
{
	int index = -1;

	switch (nID)
	{
	case	IDC_CHECK_A1:
	case	IDC_CHECK_V1:
		index = 0;
		break;

	case	IDC_CHECK_A2:
	case	IDC_CHECK_V2:
		index = 1;
		break;

	case	IDC_CHECK_A3:
	case	IDC_CHECK_V3:
		index = 2;
		break;

	case	IDC_CHECK_A4:
	case	IDC_CHECK_V4:
		index = 3;
		break;

	case	IDC_CHECK_A5:
	case	IDC_CHECK_V5:
		index = 4;
		break;
	}
	if (index == -1)
		return;

	uint32_t mask = 0;
	CButton* audioCheck = (CButton*)GetDlgItem(AUDIO_CHECK_ID[index]);
	CButton* videoCheck = (CButton*)GetDlgItem(VIDEO_CHECK_ID[index]);
	if (audioCheck->GetCheck() == BST_CHECKED)
		mask |= layer_bit_audio;
	if (videoCheck->GetCheck() == BST_CHECKED)
		mask |= layer_bit_video_low;
	m_player[index]->play(mask);
}

void CRoomDialog::OnDestroy()
{
	CDialogEx::OnDestroy();

	for (uint i = 0; i < MAX_SPEAKERS; i++) 
	{
		m_player[i]->release();
		m_player[i] = nullptr;
	}
	this->ReleaseDC(m_dc);

	m_device_dlg.DestroyWindow();
}

// 绘制声波
void CRoomDialog::drawWave(RECT* rect, rc_sound_wave& wave)
{
	INT cx = rect->right - rect->left;
	INT cy = rect->bottom - rect->top;

	INT bar_cx = cx / 12;
	RECT bar_rc = *rect;

	for (int i = 0; i < 12; i++)
	{
		bar_rc.right = bar_rc.left + bar_cx - 1;
		int bar_cy = (cy*wave.wave[i]) >> 8;
		bar_rc.top = rect->top + ((cy - bar_cy) >> 1);
		bar_rc.bottom = bar_rc.top + bar_cy;

		m_dc->FillRect(&bar_rc, &m_wave_brush);
		bar_rc.left = bar_rc.right + 1;
	}
}

// 绘制声波
void CRoomDialog::drawVoice()
{
	if (theApp.m_roomid == empty_id)
		return;

	if (m_layout == 1)
		return;

#define WAVE_BOX_WIDTH (40)

	for (int i = 0; i < MAX_SPEAKERS; i++)
	{
		RECT rect = m_video_rect[i];
		rect.bottom = rect.top;
		rect.top -= NAME_HEIGHT;
		rect.left = rect.right - WAVE_BOX_WIDTH;

		m_dc->FillRect(&rect, &m_back_brush);

		rc_sound_wave wav;
		if (theApp.m_speaker[i] == theApp.m_uid) {
			// 麦克风波形
			m_capture->get_mic_wave(&wav);
		}
		else {
			// 播放器波形
			m_player[i]->get_sound_wave(&wav);
		}
		drawWave(&rect, wav);
	}
}

// 定时任务
void CRoomDialog::onTimer(void)
{
	drawVoice();
}

// 关闭采集
void CRoomDialog::closeCapture()
{
	m_capture->set_mic_state(state_off);
	m_capture->set_video_source(video_src_null);

	SetDlgItemText(IDC_BUTTON1, L"开始发送");
}

// 发言开/关
void CRoomDialog::OnBnClickedButton1()
{
	if (m_capture->get_mic_state() == state_off) {
		m_capture->set_mic_state(state_on);
		m_capture->set_video_source((rc_video_source)m_videoSrcBox.GetCurSel());

		// 设置推送地址
		CString uri;
		GetDlgItemText(IDC_EDIT2, uri);

		Utf8Str utf(uri);
		if (!m_capture->set_push(utf))
		{
			MessageBox(L"无效的推送地址");
		}

		SetDlgItemText(IDC_BUTTON1, L"停止发送");
	}
	else {
		closeCapture();
	}
}

// 设备管理
void CRoomDialog::OnBnClickedButton2()
{
	m_device_dlg.show();
}

// 视频采集源切换
void CRoomDialog::OnCbnSelchangeCombo1()
{
	if (m_capture->get_mic_state() == state_on)
	{
		m_capture->set_video_source((rc_video_source) m_videoSrcBox.GetCurSel());
	}
}

// 切换主画面
void CRoomDialog::OnLButtonDblClk(UINT nFlags, CPoint point)
{
	for (uint i = 0; i < MAX_SPEAKERS; i++)
	{
		CRect& rect = m_video_rect[i];
		if (rect.PtInRect(point))
		{
			if (m_main_video != i) 
			{
				m_main_video = i;
				if (m_layout == 1) {
					CRect rect;
					GetClientRect(&rect);
					onLayout(rect.Width(), rect.Height());
				}
			}
			break;
		}
	}
	CDialogEx::OnLButtonDblClk(nFlags, point);
}

// 背景颜色改变
HBRUSH CRoomDialog::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
//	HBRUSH hbr = CDialogEx::OnCtlColor(pDC, pWnd, nCtlColor);

	HBRUSH hbr = (HBRUSH)m_back_brush.GetSafeHandle();

	if (nCtlColor == CTLCOLOR_STATIC || 
		nCtlColor== CTLCOLOR_LISTBOX || 
		nCtlColor== CTLCOLOR_EDIT)
	{
		pDC->SetTextColor(RGB(255, 255, 255));
		pDC->SetBkColor(BG_COLOR);
	}

	// TODO:  如果默认的不是所需画笔，则返回另一个画笔
	return hbr;
}


void CRoomDialog::OnPaint()
{
	CPaintDC dc(this); // device context for painting
					   // TODO: 在此处添加消息处理程序代码
					   // 不为绘图消息调用 CDialogEx::OnPaint()

	// 刷新视频窗口
	for (uint i = 0; i < MAX_SPEAKERS; i++)
	{
		m_player[i]->flush_view();
	}
}

// 设置单个通道的音量
void CRoomDialog::OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar)
{
	if (nSBCode == SB_THUMBPOSITION)
	{
		for (uint32_t i = 0; i < MAX_SPEAKERS; i++) 
		{
			if (sliderBar[i] == pScrollBar) 
			{
				if (i== m_send_ch)
				{
					// 这是发送通道，需要调整录音音量
					m_device->set_record_volume(nPos);
				}
				else {
					// 设置播放音量
					m_player[i]->set_volume(nPos);
				}
				break;
			}
		}
	}
	CDialogEx::OnHScroll(nSBCode, nPos, pScrollBar);
}
