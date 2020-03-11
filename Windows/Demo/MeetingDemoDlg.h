
// MeetingDemoDlg.h: 头文件
//

#pragma once

#include "CMessageDialog.h"
#include "CRoomDialog.h"
#include "CAdminDialog.h"

// CMeetingDemoDlg 对话框
class CMeetingDemoDlg : public CDialogEx
{
	void printInfo(const wchar_t* f, ...);

	volatile BOOL m_running;
	HANDLE m_thread;

	CMessageDialog m_messageWnd;
	CRoomDialog m_roomWnd;
	CAdminDialog m_adminWnd;

	BOOL OnLogin();

// 构造
public:
	CMeetingDemoDlg(CWnd* pParent = nullptr);	// 标准构造函数

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_MEETINGDEMO_DIALOG };
#endif

	void EventMonitor(void);
	void ProcessMeetingEvent(uint32_t what, uint8_t body[], uint32_t size);

protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV 支持

// 实现
protected:
	HICON m_hIcon;

	// 生成的消息映射函数
	virtual BOOL OnInitDialog();
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()

	LRESULT OnMeetingEvent(WPARAM, LPARAM);

public:
	afx_msg void OnBnClickedOk();
	afx_msg void OnDestroy();
	afx_msg void OnBnClickedButton1();
	afx_msg void OnBnClickedButton2();
	afx_msg void OnTimer(UINT_PTR nIDEvent);
};
