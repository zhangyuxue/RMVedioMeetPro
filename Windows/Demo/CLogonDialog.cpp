// CLogonDialog.cpp: 实现文件
//

#include "stdafx.h"
#include "MeetingDemo.h"
#include "CLogonDialog.h"
#include "afxdialogex.h"


// CLogonDialog 对话框

IMPLEMENT_DYNAMIC(CLogonDialog, CDialogEx)

CLogonDialog::CLogonDialog(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_LOGON_DIALOG, pParent)
{

}

CLogonDialog::~CLogonDialog()
{
}

void CLogonDialog::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}


BEGIN_MESSAGE_MAP(CLogonDialog, CDialogEx)
	ON_BN_CLICKED(IDOK, &CLogonDialog::OnBnClickedOk)
END_MESSAGE_MAP()


// CLogonDialog 消息处理程序

void CLogonDialog::printTip(const wchar_t* f, ...)
{
	wchar_t buf[1000u];
	va_list ap;
	va_start(ap, f);
	int ret = vswprintf(buf, sizeof(buf), f, ap);
	va_end(ap);
	SetDlgItemText(IDC_STATIC_TIP, buf);
}

void CLogonDialog::OnBnClickedOk()
{
	// TODO: 在此添加控件通知处理程序代码

	CString phone;
	int ret =GetDlgItemText(IDC_EDIT1, phone);
	if (ret < 4)
	{
		printTip(L"请输入手机号");
		return;
	}

	rc_userid uid =wcstoull(phone, nullptr, 10);
	if (uid == empty_id)
	{
		printTip(L"手机号错误");
		return;
	}

	CString passwd;
	ret = GetDlgItemText(IDC_EDIT2, passwd);
	if (ret < 3)
	{
		printTip(L"请输入密码");
		return;
	}

	CString server;
	ret = GetDlgItemText(IDC_EDIT4, server);
	if (ret < 8)
	{
		printTip(L"请输入服务器");
		return;
	}

	theApp.m_phone = phone;
	theApp.m_passwd = passwd;
	theApp.m_server = server;

	theApp.m_uid = uid;

	Utf8Str _server(server), _passwd(passwd);

	if (theMeeting->login(_server, uid, _passwd))
		printTip(L"正在登陆，请稍后...");
	else
		printTip(L"参数错误");
}


BOOL CLogonDialog::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// TODO:  在此添加额外的初始化

	ModifyStyleEx(0, WS_EX_APPWINDOW);

	SetDlgItemText(IDC_EDIT1, theApp.m_phone);
	SetDlgItemText(IDC_EDIT2, theApp.m_passwd);
	SetDlgItemText(IDC_EDIT4, theApp.m_server);

	return TRUE;  // return TRUE unless you set the focus to a control
				  // 异常: OCX 属性页应返回 FALSE
}

void CLogonDialog::onError(uint32_t errorcode)
{
	switch (errorcode) {
	case error_code_conflict:
		printTip(L"登录冲突, 请等待30秒后再试");
		return;
	case error_code_wrong_address :
		printTip(L"地址错误");
		return;
	case error_code_timeout :
		printTip(L"连接超时");
		return;
	default:
		printTip(L"错误: %u", errorcode);
		return;
	}
}