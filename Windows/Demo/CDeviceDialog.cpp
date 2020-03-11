// CDeviceDialog.cpp: 实现文件
//

#include "stdafx.h"
#include "MeetingDemo.h"
#include "CDeviceDialog.h"
#include "afxdialogex.h"


// CDeviceDialog 对话框

IMPLEMENT_DYNAMIC(CDeviceDialog, CDialogEx)

CDeviceDialog::CDeviceDialog(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_DEVICE_DIALOG, pParent)
{

}

CDeviceDialog::~CDeviceDialog()
{
}

void CDeviceDialog::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO1, m_camera_box);
	DDX_Control(pDX, IDC_COMBO4, m_res_box);
	DDX_Control(pDX, IDC_COMBO2, m_mic_box);
	DDX_Control(pDX, IDC_COMBO3, m_playback_box);
	DDX_Control(pDX, IDC_SLIDER1, m_mic_vol_slider);
}


BEGIN_MESSAGE_MAP(CDeviceDialog, CDialogEx)
	ON_BN_CLICKED(IDOK, &CDeviceDialog::OnBnClickedOk)
	ON_WM_CLOSE()
	ON_WM_HSCROLL()
	ON_CBN_SELCHANGE(IDC_COMBO1, &CDeviceDialog::OnCbnSelchangeCombo1)
	ON_CBN_SELCHANGE(IDC_COMBO4, &CDeviceDialog::OnCbnSelchangeCombo4)
	ON_CBN_SELCHANGE(IDC_COMBO2, &CDeviceDialog::OnCbnSelchangeCombo2)
	ON_CBN_SELCHANGE(IDC_COMBO3, &CDeviceDialog::OnCbnSelchangeCombo3)
END_MESSAGE_MAP()


// CDeviceDialog 消息处理程序


void CDeviceDialog::OnBnClickedOk()
{
	// TODO: 在此添加控件通知处理程序代码
//	CDialogEx::OnOK();
}


void CDeviceDialog::OnClose()
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值

	ShowWindow(SW_HIDE);

//	CDialogEx::OnClose();
}

void CDeviceDialog::show()
{
	// 刷新数据

	rc_device_list list;

	m_camera_box.ResetContent();
	if (m_device->camera_flush() && m_device->camera_list(&list) )
	{
		for (uint i = 0; i < list.total; i++)
		{
			m_camera_box.AddString(list.list[i]);
		}
		m_camera_box.SetCurSel(list.selected);
	}

	m_res_box.SetCurSel(m_capture->get_camera_resolution() );

	m_mic_box.ResetContent();
	if (m_device->sound_record_flush() && m_device->sound_record_list(&list))
	{
		for (uint i = 0; i < list.total; i++)
		{
			m_mic_box.AddString(list.list[i]);
		}
		m_mic_box.SetCurSel(list.selected);
	}

	m_mic_vol_slider.SetPos(m_device->get_mic_volume());

	m_playback_box.ResetContent();
	if (m_device->sound_playback_flush() && m_device->sound_playback_list(&list))
	{
		for (uint i = 0; i < list.total; i++)
		{
			m_playback_box.AddString(list.list[i]);
		}
		m_playback_box.SetCurSel(list.selected);
	}

	ShowWindow(SW_NORMAL);
}


void CDeviceDialog::OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar)
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值

	if (pScrollBar->GetSafeHwnd() == m_mic_vol_slider.GetSafeHwnd())
		if (nSBCode == SB_THUMBPOSITION)
			m_device->set_mic_volume(nPos);

	CDialogEx::OnHScroll(nSBCode, nPos, pScrollBar);
}


BOOL CDeviceDialog::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// TODO:  在此添加额外的初始化

	m_device = (rc_device_manager*)theMeeting->query_api(rc_meeting::iid_device_manager);
	m_capture = (rc_video_capturer*)theMeeting->query_api(rc_meeting::iid_media_capturer);
	m_capture->set_camera_resolution(video_layer_medium); //初始设为标清采集

	m_mic_vol_slider.SetRange(0, 100);

	return TRUE;  // return TRUE unless you set the focus to a control
				  // 异常: OCX 属性页应返回 FALSE
}

// 摄像头
void CDeviceDialog::OnCbnSelchangeCombo1()
{
	int sel = m_camera_box.GetCurSel();
	if (sel >= 0)
		m_device->camera_select(sel);
}

// 清晰度
void CDeviceDialog::OnCbnSelchangeCombo4()
{
	int sel = m_res_box.GetCurSel();
	if (sel >= 0)
		m_capture->set_camera_resolution((rc_layer)sel);
}

// 麦克风
void CDeviceDialog::OnCbnSelchangeCombo2()
{
	int sel = m_mic_box.GetCurSel();
	if (sel >= 0) {
		CString name;
		m_mic_box.GetWindowText(name);
		m_device->sound_record_select(name);
	}
}

// 扬声器
void CDeviceDialog::OnCbnSelchangeCombo3()
{
	if (m_playback_box.GetCurSel() >= 0)
	{
		CString name;
		m_playback_box.GetWindowText(name);
		m_device->sound_playback_select(name);
	}
}
