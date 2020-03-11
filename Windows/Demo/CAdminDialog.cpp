// CAdminDialog.cpp: 实现文件
//

#include "stdafx.h"
#include "MeetingDemo.h"
#include "CAdminDialog.h"
#include "afxdialogex.h"
#include "CInputDialog.h"

#include "segment.h"

// 房间属性字段
enum SEG {
	seg_null,
	seg_passwd,
	seg_flag,
	seg_speak_mode,
	seg_invite_list,
};


// CAdminDialog 对话框

IMPLEMENT_DYNAMIC(CAdminDialog, CDialogEx)

CAdminDialog::CAdminDialog(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_ADMIN_DIALOG, pParent)
{
	m_flag = 0;
	m_speak_mode = speak_mode_queuing;
	m_invite_list.count = 0;
}

CAdminDialog::~CAdminDialog()
{
}

void CAdminDialog::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK1, m_checkBox1);
	DDX_Control(pDX, IDC_CHECK2, m_checkBox2);
	DDX_Control(pDX, IDC_LIST1, m_inviteBox);
	DDX_Control(pDX, IDC_COMBO1, m_speakModeBox);
}


BEGIN_MESSAGE_MAP(CAdminDialog, CDialogEx)
	ON_BN_CLICKED(IDOK, &CAdminDialog::OnBnClickedOk)
	ON_WM_CLOSE()
	ON_BN_CLICKED(IDC_BUTTON2, &CAdminDialog::OnBnClickedButton2)
	ON_BN_CLICKED(IDC_BUTTON1, &CAdminDialog::OnBnClickedButton1)
	ON_BN_CLICKED(IDC_BUTTON4, &CAdminDialog::OnBnClickedButton4)
END_MESSAGE_MAP()


// CAdminDialog 消息处理程序


void CAdminDialog::OnBnClickedOk()
{
	// TODO: 在此添加控件通知处理程序代码
//	CDialogEx::OnOK();
}


void CAdminDialog::OnClose()
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值

	ShowWindow(SW_HIDE);

	apply();

	writeRegister();

//	CDialogEx::OnClose();
}

#define MaxPasswdLen (60u)

BOOL CAdminDialog::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// TODO:  在此添加额外的初始化

	// 载入房间属性
	MY_ROOM_DATA& data = theApp.m_my_room;
	if (data.ptr)
	{
		const uint8_t* ptr = data.ptr;
		const uint8_t* end = ptr + data.len;
		segment_t seg;
		while (ptr = next(ptr, end, &seg))
		{
			switch (seg.what) {
			case	seg_passwd:
				if (seg.length <= MaxPasswdLen*2) 
					m_passwd.SetString((LPCTSTR)seg.body, seg.length/2);
				break;

			case	seg_flag:
				if (seg.length == 4u) {
					m_flag = parse_u32(seg.body);
				}
				break;

			case	seg_speak_mode:
				if (seg.length == 2u) {
					m_speak_mode = (rc_speak_mode)parse_u16(seg.body);
				}
				break;

			case 	seg_invite_list:
			{
				uint32_t count = seg.length / sizeof(rc_userid);
				if (count <= MaxInviteCount)
					memcpy(m_invite_list.vector, seg.body, seg.length);
				m_invite_list.count = count;
			}
				break;
			}
		}
	}

	SetDlgItemText(IDC_EDIT1, m_passwd);

	m_checkBox1.SetCheck((m_flag&room_flag_password) ? BST_CHECKED : BST_UNCHECKED);
	m_checkBox2.SetCheck((m_flag&room_flag_invite) ? BST_CHECKED : BST_UNCHECKED);

	CString str;
	for (uint i = 0; i < m_invite_list.count; i++)
	{
		str.Format(L"%llu", m_invite_list.vector[i]);
		m_inviteBox.AddString(str);
	}

	m_speakModeBox.SetCurSel(m_speak_mode);

	return TRUE;  // return TRUE unless you set the focus to a control
				  // 异常: OCX 属性页应返回 FALSE
}

void CAdminDialog::onSpeakerList()
{
	rc_userid* list = theApp.m_speaker;

	const UINT edit_id[MAX_SPEAKERS] = {
		IDC_EDIT2,
		IDC_EDIT6,
		IDC_EDIT7,
		IDC_EDIT8,
		IDC_EDIT9,
	};

	CString str;
	for (uint i = 0; i < MAX_SPEAKERS; i++)
	{
		str.Format(L"%llu", list[i]);
		SetDlgItemText(edit_id[i], str);
	}
}

void CAdminDialog::writeRegister(void)
{
	const size_t MaxSize = sizeof(m_invite_list) + 200u;

	MY_ROOM_DATA& data = theApp.m_my_room;
	if (data.ptr == nullptr) {
		data.ptr = new BYTE[MaxSize];
	}

	// 写入
	BYTE* ptr = data.ptr;

	ptr = write_tag(ptr, seg_flag, 4u);
	ptr = write_u32(ptr, m_flag);

	ptr = write_tag(ptr, seg_speak_mode, 2u);
	ptr = write_u16(ptr, m_speak_mode);

	size_t chars = m_passwd.GetLength();
	ptr = write_tag(ptr, seg_passwd, chars*2);
	if(chars)
	ptr = write_block(ptr, m_passwd, chars*2);

	size_t list_size = m_invite_list.count * sizeof(rc_userid);
	ptr = write_tag(ptr, seg_invite_list, list_size);
	ptr = write_block(ptr, m_invite_list.vector, list_size);

	data.len = ptr - data.ptr;
}

// 增加邀请
void CAdminDialog::OnBnClickedButton2()
{
	CInputDialog dialog(L"请输入手机号");

	if (dialog.DoModal() == IDOK)
	{
		CString text = dialog.mText.Trim();
		rc_userid uid = wcstoull(text,NULL,10);
		if (uid != empty_id &&
			uid != theApp.m_uid &&
			m_invite_list.count < MaxInviteCount)
		{
			for (uint i = m_invite_list.count; i;)
			{
				if (m_invite_list.vector[--i] == uid)
					return; //已经存在
			}
			m_invite_list.vector[m_invite_list.count++] = uid;

			m_inviteBox.AddString(text);
			
			// 发送邀请文字
			Utf8Str utf8(L"快来开会");
			theMeeting->send_text(uid, utf8, utf8.length());
		}
	}
}

// 移除邀请
void CAdminDialog::OnBnClickedButton1()
{
	int sel = m_inviteBox.GetCurSel();
	if (sel < 0)
		return;

	CString str;
	m_inviteBox.GetText(sel, str);

	rc_userid uid = wcstoull(str, nullptr, 10);

	m_inviteBox.DeleteString(sel);

	for (uint i = 0; i < m_invite_list.count; i++)
	{
		if (m_invite_list.vector[i] == uid) {
			m_invite_list.vector[i] = m_invite_list.vector[--m_invite_list.count];
			break;
		}
	}
}

void CAdminDialog::OnBnClickedButton4()
{
	apply();
}

// 提交修改
void CAdminDialog::apply(void)
{
	rc_room_admin* admin = (rc_room_admin*)theMeeting->query_api(rc_meeting::iid_room_admin);

	CString str;
	GetDlgItemText(IDC_EDIT1, str);
	m_passwd = str.Left(MaxPasswdLen);

	uint32_t flag = 0;
	if (m_checkBox1.GetCheck() == BST_CHECKED)
		flag |= room_flag_password;
	if (m_checkBox2.GetCheck() == BST_CHECKED)
		flag |= room_flag_invite;
	m_flag = flag;

	Utf8Str utf8(m_passwd);
	admin->set_room_flag(flag, utf8);

	m_speak_mode = (rc_speak_mode) m_speakModeBox.GetCurSel();
	admin->set_speak_mode(m_speak_mode);

	if (m_speak_mode == speak_mode_hosting)
	{
		// 主持模式下可以指定各个通道的发言者:

		const UINT edit_id[MAX_SPEAKERS] = {
			IDC_EDIT2,
			IDC_EDIT6,
			IDC_EDIT7,
			IDC_EDIT8,
			IDC_EDIT9,
		};
		rc_speaker_list list;
		for (uint i = 0; i < MAX_SPEAKERS; i++)
		{
			GetDlgItemText(edit_id[i], str);
			list[i] = wcstoull(str, nullptr, 10);
		}
		admin->set_speaker(list);
	}

	if (m_flag &room_flag_invite)
	{
		admin->set_invite(m_invite_list.vector, m_invite_list.count);
	}
}
