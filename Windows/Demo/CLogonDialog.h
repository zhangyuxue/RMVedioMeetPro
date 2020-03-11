#pragma once


// CLogonDialog 对话框

class CLogonDialog : public CDialogEx
{
	DECLARE_DYNAMIC(CLogonDialog)

	void printTip(const wchar_t* f, ...);

public:
	CLogonDialog(CWnd* pParent = nullptr);   // 标准构造函数
	virtual ~CLogonDialog();

	void onError(uint32_t errorcode);

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_LOGON_DIALOG };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedOk();
	virtual BOOL OnInitDialog();
};
