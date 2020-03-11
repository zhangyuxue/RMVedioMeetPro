// CMessageDialog.cpp: 实现文件
//

#include "stdafx.h"
#include "MeetingDemo.h"
#include "CMessageDialog.h"
#include "afxdialogex.h"


// CMessageDialog 对话框

IMPLEMENT_DYNAMIC(CMessageDialog, CDialogEx)

CMessageDialog::CMessageDialog(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_MESSAGE_DIALOG, pParent)
{

}

CMessageDialog::~CMessageDialog()
{
}

void CMessageDialog::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}


BEGIN_MESSAGE_MAP(CMessageDialog, CDialogEx)
	ON_BN_CLICKED(IDOK, &CMessageDialog::OnBnClickedOk)
	ON_WM_CLOSE()
	ON_LBN_SELCHANGE(IDC_LIST1, &CMessageDialog::OnLbnSelchangeList1)END_MESSAGE_MAP()


// CMessageDialog 消息处理程序

void CMessageDialog::addMessage(rc_text* text)
{
	CString str;
	if (Utf8ToCStr(text->str, text->chars, str) <= 0)
		return;

	CString line;
	line.Format(L"%llu: ", text->from);

	line += str;
	mBox->AddString(line);

	if (!IsWindowVisible())
		ShowWindow(SW_NORMAL);
}


void CMessageDialog::OnBnClickedOk()
{
	// TODO: 在此添加控件通知处理程序代码
//	CDialogEx::OnOK();
}


BOOL CMessageDialog::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// TODO:  在此添加额外的初始化

	mBox = (CListBox*)GetDlgItem(IDC_LIST1);

	return TRUE;  // return TRUE unless you set the focus to a control
				  // 异常: OCX 属性页应返回 FALSE
}


void CMessageDialog::OnLbnSelchangeList1()
{
	// TODO: 在此添加控件通知处理程序代码

	int index =mBox->GetCurSel();
	if (index >= 0) {
		CString str;
		mBox->GetText(index, str);
		SetDlgItemText(IDC_EDIT1, str);
	}
}


void CMessageDialog::OnClose()
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值

	// 这里隐藏窗口
	ShowWindow(SW_HIDE);

//	CDialogEx::OnClose();
}
