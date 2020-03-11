
// FVideoDlg.cpp: 实现文件
//

#include "stdafx.h"
#include "FVideo.h"
#include "FVideoDlg.h"
#include "afxdialogex.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

// 采集器和播放器
rc_video_capturer* capturer = nullptr;
rc_video_player* player = nullptr;
rc_video_player* preview = nullptr;

BOOL playing = false;

// 用于应用程序“关于”菜单项的 CAboutDlg 对话框

class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ABOUTBOX };
#endif

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

// 实现
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialogEx(IDD_ABOUTBOX)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialogEx)
END_MESSAGE_MAP()


// CFVideoDlg 对话框



CFVideoDlg::CFVideoDlg(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_FVIDEO_DIALOG, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CFVideoDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK1, mAudioCheck);
	DDX_Control(pDX, IDC_CHECK2, mVideoCheck);
}

BEGIN_MESSAGE_MAP(CFVideoDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDOK, &CFVideoDlg::OnBnClickedOk)
	ON_WM_DESTROY()
	ON_BN_CLICKED(IDC_BUTTON3, &CFVideoDlg::OnBnClickedButton3)
	ON_BN_CLICKED(IDC_BUTTON4, &CFVideoDlg::OnBnClickedButton4)
	ON_BN_CLICKED(IDC_CHECK1, &CFVideoDlg::OnBnClickedCheck1)
	ON_BN_CLICKED(IDC_CHECK2, &CFVideoDlg::OnBnClickedCheck2)
END_MESSAGE_MAP()

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

// CFVideoDlg 消息处理程序

BOOL CFVideoDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// 将“关于...”菜单项添加到系统菜单中。

	// IDM_ABOUTBOX 必须在系统命令范围内。
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != nullptr)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// 设置此对话框的图标。  当应用程序主窗口不是对话框时，框架将自动
	//  执行此操作
	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	// TODO: 在此添加额外的初始化代码
	mAudioCheck.SetCheck(BST_CHECKED);
	mVideoCheck.SetCheck(BST_CHECKED);

	// 得到采集器接口
	capturer = (rc_video_capturer*)theMeeting->query_api(rc_meeting::iid_media_capturer);

	// 设置摄像头清晰度: 中等
	// 总共5级分辨率
	capturer->set_camera_resolution(video_layer_medium);

	// 用于摄像头预览的播放器
	CWnd* wnd = GetDlgItem(IDC_STATIC_VIDEO);
	preview = createPlayer(wnd->GetSafeHwnd());
	preview->set_video_signal(capturer->get_camera_signal());

//	rc_device_manager* dev = (rc_device_manager*)theMeeting->query_api(rc_meeting::iid_device_manager);
	
	// 播放远端信号的播放器
	wnd = GetDlgItem(IDC_STATIC_VIDEO2);
	player = createPlayer(wnd->GetSafeHwnd());

	// 设置播放器空白画面
	SetBlankVideo();

	SetDlgItemText(IDC_EDIT1, L"fvideo://127.0.0.1:10005/10000852");
	SetDlgItemText(IDC_EDIT2, L"fvideo://127.0.0.1:10005/10000852");

	return TRUE;  // 除非将焦点设置到控件，否则返回 TRUE
}

void CFVideoDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

// 如果向对话框添加最小化按钮，则需要下面的代码
//  来绘制该图标。  对于使用文档/视图模型的 MFC 应用程序，
//  这将由框架自动完成。

void CFVideoDlg::OnPaint()
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
		// 立即刷新播放器

		preview->flush_view();
		player->flush_view();

		CDialogEx::OnPaint();
	}
}

//当用户拖动最小化窗口时系统调用此函数取得光标
//显示。
HCURSOR CFVideoDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}



void CFVideoDlg::OnBnClickedOk()
{
	// TODO: 在此添加控件通知处理程序代码
//	CDialogEx::OnOK();
}


void CFVideoDlg::OnDestroy()
{
	CDialogEx::OnDestroy();

	// TODO: 在此处添加消息处理程序代码

	player->release();
	preview->release();
}

// 开始/停止 推送
void CFVideoDlg::OnBnClickedButton3()
{
	// TODO: 在此添加控件通知处理程序代码

	if (capturer->get_mic_state() == state_on) {
		// 停止推送

		capturer->set_mic_state(state_off);

		capturer->set_video_source(video_src_null);

		capturer->set_push(nullptr);

		preview->play(0);

		SetDlgItemText(IDC_BUTTON3, L"推送");
	}
	else {
		// 开启麦克风
		capturer->set_mic_state(state_on);

		// 开启摄像头
		capturer->set_video_source(video_src_camera);

		// 开始摄像头预览
		preview->play(layer_bit_video_high);

		// 设置推送地址 (开始推送)
		CString uri;
		GetDlgItemText(IDC_EDIT1, uri);
		Utf8Str uri_sz(uri);
		capturer->set_push(uri_sz);

		SetDlgItemText(IDC_BUTTON3, L"停止");
	}
}

// 开始/停止 播放
void CFVideoDlg::OnBnClickedButton4()
{
	// TODO: 在此添加控件通知处理程序代码

	if (playing) {
		// 停止播放
		player->play(0);

		player->load(nullptr);

		playing = false;
		SetDlgItemText(IDC_BUTTON4, L"播放");
	}
	else {
		// 开始播放
		CString uri;
		GetDlgItemText(IDC_EDIT2, uri);
		Utf8Str uri_sz(uri);

		if (!player->load(uri_sz))
			return;

		uint32_t mask = 0;

		play();
		playing = true;
		SetDlgItemText(IDC_BUTTON4, L"停止");
	}
}

void CFVideoDlg::play()
{
	uint32_t mask = 0;
	if (mAudioCheck.GetCheck() == BST_CHECKED)
		mask |= layer_bit_audio;
	if (mVideoCheck.GetCheck() == BST_CHECKED)
		mask |= layer_bit_video_medium;

	player->play(mask);
}

void CFVideoDlg::OnBnClickedCheck1()
{
	// TODO: 在此添加控件通知处理程序代码
	play();
}


void CFVideoDlg::OnBnClickedCheck2()
{
	// TODO: 在此添加控件通知处理程序代码
	play();
}
