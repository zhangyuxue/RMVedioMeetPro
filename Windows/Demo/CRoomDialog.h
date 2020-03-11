#pragma once

#include "CDeviceDialog.h"


// CRoomDialog 对话框

class CRoomDialog : public CDialogEx
{
	DECLARE_DYNAMIC(CRoomDialog)

	int m_layout, m_main_video;

	rc_video_player* m_player[MAX_SPEAKERS]; //播放
	rc_video_capturer* m_capture; //采集接口
	rc_device_manager* m_device; //设备管理接口
	int m_send_ch; //发送通道, -1表示未发送

	CDC *m_dc;
	CBrush m_wave_brush, m_back_brush;

	CRect m_video_rect[MAX_SPEAKERS];

	void onLayout(int cx, int cy);
	void drawWave(RECT* rect, rc_sound_wave& wave);
	void drawVoice();

	CDeviceDialog m_device_dlg;

public:
	CRoomDialog(CWnd* pParent = nullptr);   // 标准构造函数
	virtual ~CRoomDialog();

	void onEnter();
	void onSpeakerChanged();
	void closeCapture();
	void onTimer(void);

// 对话框数据
	enum { IDD = IDD_ROOM_DIALOG };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()

public:
	afx_msg void OnBnClickedOk();
	afx_msg void OnClose();
	afx_msg void OnSize(UINT nType, int cx, int cy);
	afx_msg void OnGetMinMaxInfo(MINMAXINFO* lpMMI);
	afx_msg void OnBnClickedButton3();
	virtual BOOL OnInitDialog();
	afx_msg void OnDestroy();
	afx_msg void OnCheckClicked(UINT nID);
private:
	CComboBox m_videoSrcBox;
public:
	afx_msg void OnBnClickedButton1();
	afx_msg void OnBnClickedButton2();
	afx_msg void OnCbnSelchangeCombo1();
	afx_msg void OnLButtonDblClk(UINT nFlags, CPoint point);
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);
	afx_msg void OnPaint();
	afx_msg void OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar);
};
