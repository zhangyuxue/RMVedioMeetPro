
// FVideoDlg.h: 头文件
//

#pragma once


// CFVideoDlg 对话框
class CFVideoDlg : public CDialogEx
{
	void play();

// 构造
public:
	CFVideoDlg(CWnd* pParent = nullptr);	// 标准构造函数

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_FVIDEO_DIALOG };
#endif

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV 支持


// 实现
protected:
	HICON m_hIcon;

	// 生成的消息映射函数
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedOk();
	afx_msg void OnDestroy();
	afx_msg void OnBnClickedButton3();
	afx_msg void OnBnClickedButton4();
private:
	CButton mAudioCheck;
public:
	afx_msg void OnBnClickedCheck1();
private:
	CButton mVideoCheck;
public:
	afx_msg void OnBnClickedCheck2();
};
