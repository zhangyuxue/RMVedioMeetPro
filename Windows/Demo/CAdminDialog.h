#pragma once


// CAdminDialog 对话框

class CAdminDialog : public CDialogEx
{
	DECLARE_DYNAMIC(CAdminDialog)

#define MaxInviteCount (500u)

	CString m_passwd;
	
	uint32_t m_flag;
	rc_speak_mode m_speak_mode;

	// 邀请列表
	struct INVITE_LIST {
		uint32_t count;
		rc_userid vector[MaxInviteCount];
	} m_invite_list;

	void writeRegister(void);
	void apply(void);

public:
	CAdminDialog(CWnd* pParent = nullptr);   // 标准构造函数
	virtual ~CAdminDialog();

	void onSpeakerList();

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ADMIN_DIALOG };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedOk();
	afx_msg void OnClose();
	virtual BOOL OnInitDialog();
	afx_msg void OnBnClickedButton2();
	afx_msg void OnBnClickedButton1();
	afx_msg void OnBnClickedButton4();
private:
	CButton m_checkBox1;
	CButton m_checkBox2;
	CListBox m_inviteBox;
	CComboBox m_speakModeBox;
};
