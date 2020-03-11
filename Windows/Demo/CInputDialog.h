#pragma once


// CInputDialog 对话框

class CInputDialog : public CDialogEx
{
	DECLARE_DYNAMIC(CInputDialog)

	CString mTitle;

public:
	CInputDialog(LPCTSTR title, CWnd* pParent = nullptr);   // 标准构造函数
	virtual ~CInputDialog();

	CString mText;

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_INPUT_DIALOG };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedOk();
	virtual BOOL OnInitDialog();
};
