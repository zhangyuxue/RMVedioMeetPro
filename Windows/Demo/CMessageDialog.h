#pragma once

// CMessageDialog 对话框

class CMessageDialog : public CDialogEx
{
	DECLARE_DYNAMIC(CMessageDialog)

	CListBox* mBox;

public:
	CMessageDialog(CWnd* pParent = nullptr);   // 标准构造函数
	virtual ~CMessageDialog();

// 对话框数据
	enum { IDD = IDD_MESSAGE_DIALOG };

	void addMessage(rc_text*);

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedOk();
	virtual BOOL OnInitDialog();
	afx_msg void OnLbnSelchangeList1();
	afx_msg void OnClose();
};
