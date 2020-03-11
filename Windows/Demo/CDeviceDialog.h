#pragma once


// CDeviceDialog 对话框

class CDeviceDialog : public CDialogEx
{
	DECLARE_DYNAMIC(CDeviceDialog)

	rc_device_manager* m_device;
	rc_video_capturer* m_capture;

public:
	CDeviceDialog(CWnd* pParent = nullptr);   // 标准构造函数
	virtual ~CDeviceDialog();

	void show();

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_DEVICE_DIALOG };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedOk();
	afx_msg void OnClose();
private:
	CComboBox m_camera_box;
	CComboBox m_res_box;
	CComboBox m_mic_box;
	CComboBox m_playback_box;
	CSliderCtrl m_mic_vol_slider;
public:
	afx_msg void OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar);
	virtual BOOL OnInitDialog();
	afx_msg void OnCbnSelchangeCombo1();
	afx_msg void OnCbnSelchangeCombo4();
	afx_msg void OnCbnSelchangeCombo2();
	afx_msg void OnCbnSelchangeCombo3();
};
