package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net"
	"strconv"
	"strings"
	"sync"

	"github.com/tidwall/gjson"
)

// type MsgStruct struct {
// 	Type 			string 'json:"msg_type"'
// 	PushID			string 'json:"msg_userpushid"'
// 	JionMeetID		string 'json:"msg_JionMeetID"'
// 	CreateMeetID	string 'json:"msg_CreateMeetID"'
// 	Uid				string 'json:"msg_useruid"'
// 	Name			string 'json:"msg_userName"'
// }

type MsgStruct struct {
	Msg_type         string
	Msg_userpushid   string
	Msg_JionMeetID   string
	Msg_CreateMeetID string
	Msg_useruid      string
	Msg_userName     string
	Msg_userlevel    string
	Msg_meetName     string
}

type MsgMeetListStruct struct {
	Msg_type     string
	Msg_meetlist []MsgStruct
}

type MsgUserListStruct struct {
	Msg_type     string
	Msg_userlist []MsgStruct
}

type MsgInvitStruct struct {
	Msg_type       string
	Msg_JionMeetID string
	Msg_useruid    string
	Msg_userName   string
	Msg_meetName   string
}

var gPushID int
var rw sync.RWMutex
var gMeetingList []MsgStruct
var connections []net.Conn
var uid_conn map[string]net.Conn

func DeleteSliceCon(a []net.Conn, b net.Conn) []net.Conn {
	j := 0
	for _, val := range a {
		if val == b {
			a[j] = val
			j++
		}
	}
	return a[:j]
}

var gUserList []MsgStruct

func DeleteSliceMsgStruct(a []MsgStruct, b *MsgStruct) []MsgStruct {
	j := 0
	for _, val := range a {
		if val.Msg_useruid == b.Msg_useruid {
			a[j] = val
			j++
		}
	}
	return a[:j]
}

func main() {
	log.Println("河北安监业务服务器\r\n")
	gPushID = 1
	uid_conn = make(map[string]net.Conn)

	var test MsgStruct
	test.Msg_CreateMeetID = "111111"
	test.Msg_meetName = "sfsd"
	gMeetingList = append(gMeetingList, test)
	test.Msg_meetName = "aaa"
	gMeetingList = append(gMeetingList, test)
	test.Msg_meetName = "bbbb"
	gMeetingList = append(gMeetingList, test)
	test.Msg_meetName = "cccc"
	gMeetingList = append(gMeetingList, test)
	test.Msg_meetName = "dddd"
	gMeetingList = append(gMeetingList, test)

	ln, err := net.Listen("tcp", ":10000")
	if err != nil {
		panic(err)
	}
	// go func() {
	// 	if err := http.ListenAndServe(":6060", nil); err != nil {
	// 		log.Printf("pprof failed: %v", err)
	// 	}
	// }()

	defer func() {
		for _, conn := range connections {
			conn.Close()
			fmt.Println("One socket Disconnect\r\n")
		}
	}()
	for {
		conn, e := ln.Accept()
		if e != nil {
			if ne, ok := e.(net.Error); ok && ne.Temporary() {
				log.Printf("accept temp err: %v", ne)
				continue
			}
			log.Printf("accept err: %v", e)
			return
		}
		go handleConn(conn)
		connections = append(connections, conn)

		if len(connections)%100 == 0 {
			log.Printf("total number of connections: %v", len(connections))
		}
	}
}
func handleConn(conn net.Conn) {
	log.Println("One newer Connect Get!\r\n")
	//io.Copy(ioutil.Discard, conn)
	defer conn.Close()

	// 从连接读取直到遇到EOF. 期望下一次输入是命令名。调用注册的用于该命令的处理器。
	data := make([]byte, 65536)
	var loopstr string
	backmsg := &MsgStruct{}
	for {
		bytesize, err := conn.Read(data)
		log.Printf("Receive command bytes %d:", bytesize)
		switch {
		case err == io.EOF:
			{
				rw.RLock()
				removeIndex := -1
				for i, socketHandle := range connections {
					if socketHandle != conn {
						backmsg.Msg_type = "LeavMeeting"
						b, jsonerr := json.Marshal(backmsg)
						if jsonerr != nil {
							fmt.Printf("json.marshal failed, err:", jsonerr)
							return
						}
						fmt.Println(string(b))
						b = append(b, '\n')
						log.Printf("Live Data:%s\r\n", b)
						socketHandle.Write(b)
					} else {
						removeIndex = i
					}

				}
				if removeIndex != -1 {
					connections = DeleteSliceCon(connections, conn)
				}
				removeIndex = -1
				for i, usr := range gUserList {
					if usr.Msg_useruid == backmsg.Msg_useruid {
						removeIndex = i
					}
				}
				if removeIndex != -1 {
					gUserList = DeleteSliceMsgStruct(gUserList, backmsg)
				}
				rw.RUnlock()
				log.Println("Reached EOF - close this connection.\n  ---")
				return
			}

		case err != nil:
			log.Println("\nError reading command. Got: ", err)
		}
		var cmds []string
		loopstr += string(data[:bytesize])
		log.Println("Get Data:" + loopstr + "\r\n")
		// 修剪请求字符串中的多余回车和空格- ReadString不会去掉任何换行。
		var lastendpos = 0
		for i := 0; i < len(loopstr); i++ {
			content := loopstr[i]
			if content == '\n' {
				tempcmd := string(loopstr[lastendpos:i])
				tempcmd = strings.Trim(tempcmd, "\n")
				cmds = append(cmds, tempcmd)
				lastendpos += i
			}
		}
		if lastendpos < (bytesize - 1) {
			loopstr += string(data[lastendpos:bytesize])
			log.Printf("last data not useed:%s", loopstr)
		} else {
			loopstr = ""
		}

		for _, v := range cmds {
			log.Println(v)
			msgtype := gjson.Get(v, "Msg_type")
			if msgtype.String() == "InvitUsers" {
				rw.RLock()

				invitvalue := gjson.Get(v, "Msg_invitlist")
				invitArray := invitvalue.Array()

				var invitmsg MsgInvitStruct
				invitmsg.Msg_type = "InvitUsers"
				invitmsg.Msg_JionMeetID = gjson.Get(v, "Msg_JionMeetID").String()
				invitmsg.Msg_meetName = gjson.Get(v, "Msg_meetName").String()
				invitmsg.Msg_userName = gjson.Get(v, "Msg_userName").String()
				invitmsg.Msg_useruid = gjson.Get(v, "Msg_useruid").String()

				for _, vUser := range invitArray {
					localconn := uid_conn[vUser.String()]
					if localconn != nil {
						b, jsonerr := json.Marshal(invitmsg)
						if jsonerr != nil {
							fmt.Printf("json.marshal failed, err:", jsonerr)
							return
						}
						fmt.Println(string(b))
						b = append(b, '\n')
						log.Printf("Invit Back Data:%s\r\n", b)
						localconn.Write(b)
					}
				}

				rw.RUnlock()
			} else if msgtype.String() == "GetUserList" {
				rw.RLock()

				var meetlist MsgUserListStruct
				for _, vMet := range gUserList {
					meetlist.Msg_userlist = append(meetlist.Msg_userlist, vMet)
				}
				meetlist.Msg_type = "GetUserList"
				b, jsonerr := json.Marshal(meetlist)
				if jsonerr != nil {
					fmt.Printf("json.marshal failed, err:", jsonerr)
					return
				}
				fmt.Println(string(b))
				b = append(b, '\n')
				log.Printf("Back Data:%s\r\n", b)
				conn.Write(b)
				rw.RUnlock()
			} else if msgtype.String() == "GetMeetingList" {
				rw.RLock()

				var meetlist MsgMeetListStruct
				for _, vMet := range gMeetingList {
					meetlist.Msg_meetlist = append(meetlist.Msg_meetlist, vMet)
				}
				meetlist.Msg_type = "GetMeetingList"
				b, jsonerr := json.Marshal(meetlist)
				if jsonerr != nil {
					fmt.Printf("json.marshal failed, err:", jsonerr)
					return
				}
				fmt.Println(string(b))
				b = append(b, '\n')
				log.Printf("Back Data:%s\r\n", b)
				conn.Write(b)
				rw.RUnlock()
			} else if msgtype.String() == "login" {
				backmsg.Msg_type = msgtype.String()
				rw.RLock()
				backmsg.Msg_userpushid = strconv.Itoa(gPushID)
				gPushID += 1
				rw.RUnlock()

				userlevel := gjson.Get(v, "Msg_userlevel")
				meetName := gjson.Get(v, "Msg_meetName")

				jionmeetid := gjson.Get(v, "Msg_JionMeetID")
				useruid := gjson.Get(v, "Msg_useruid")
				username := gjson.Get(v, "Msg_userName")

				rw.RLock()
				uid_conn[useruid.String()] = conn
				rw.RUnlock()
				backmsg.Msg_JionMeetID = jionmeetid.String()
				backmsg.Msg_useruid = useruid.String()
				backmsg.Msg_userName = username.String()
				backmsg.Msg_userlevel = userlevel.String()
				backmsg.Msg_meetName = meetName.String()

				b, jsonerr := json.Marshal(backmsg)
				if jsonerr != nil {
					fmt.Printf("json.marshal failed, err:", jsonerr)
					return
				}
				fmt.Println(string(b))
				b = append(b, '\n')
				log.Printf("Back Data:%s\r\n", b)
				conn.Write(b)
			} else if msgtype.String() == "CreateMeet" {
				metid := gjson.Get(v, "Msg_CreateMeetID")
				metidstr := metid.String()
				backmsg.Msg_type = msgtype.String()
				for _, vMet := range gMeetingList {
					if vMet.Msg_CreateMeetID == metidstr {
						backmsg.Msg_CreateMeetID = strconv.Itoa(0)
						b, jsonerr := json.Marshal(backmsg)
						if jsonerr != nil {
							fmt.Printf("json.marshal failed, err:", jsonerr)
							return
						}
						fmt.Println(string(b))
						b = append(b, '\n')
						log.Printf("Back Data:%s\r\n", b)
						conn.Write(b)
						return
					}
				}
				rw.RLock()
				connections = append(connections, conn)
				rw.RUnlock()
				userlevel := gjson.Get(v, "Msg_userlevel")
				meetName := gjson.Get(v, "Msg_meetName")
				userpushid := gjson.Get(v, "Msg_userpushid")
				jionmeetid := gjson.Get(v, "Msg_JionMeetID")
				useruid := gjson.Get(v, "Msg_useruid")
				username := gjson.Get(v, "Msg_userName")

				backmsg.Msg_CreateMeetID = metid.String()
				backmsg.Msg_userpushid = userpushid.String()
				backmsg.Msg_JionMeetID = jionmeetid.String()
				backmsg.Msg_useruid = useruid.String()
				backmsg.Msg_userName = username.String()
				backmsg.Msg_userlevel = userlevel.String()
				backmsg.Msg_meetName = meetName.String()

				b, jsonerr := json.Marshal(backmsg)
				if jsonerr != nil {
					fmt.Printf("json.marshal failed, err:", jsonerr)
					return
				}
				fmt.Println(string(b))
				b = append(b, '\n')
				log.Printf("Back Data:%s\r\n", b)
				conn.Write(b)
			} else if msgtype.String() == "JionMeet" {
				metid := gjson.Get(v, "Msg_JionMeetID")
				metidstr := metid.String()

				backmsg.Msg_type = msgtype.String()
				userlevel := gjson.Get(v, "Msg_userlevel")
				meetName := gjson.Get(v, "Msg_meetName")
				userpushid := gjson.Get(v, "Msg_userpushid")
				jionmeetid := gjson.Get(v, "Msg_JionMeetID")
				useruid := gjson.Get(v, "Msg_useruid")
				username := gjson.Get(v, "Msg_userName")

				backmsg.Msg_CreateMeetID = metid.String()
				backmsg.Msg_userpushid = userpushid.String()
				backmsg.Msg_JionMeetID = jionmeetid.String()
				backmsg.Msg_useruid = useruid.String()
				backmsg.Msg_userName = username.String()
				backmsg.Msg_userlevel = userlevel.String()
				backmsg.Msg_meetName = meetName.String()
				var res int
				res = 0
				for _, vMet := range gMeetingList {
					if vMet.Msg_CreateMeetID == metidstr {
						//有这个ID
						res = 0
						break
					}
				}
				if res == 0 {
					b, jsonerr := json.Marshal(backmsg)
					if jsonerr != nil {
						fmt.Printf("json.marshal failed, err:", jsonerr)
						return
					}
					fmt.Println(string(b))
					b = append(b, '\n')

					rw.RLock()
					for _, socketHandle := range connections {
						log.Printf("Back Data:%s\r\n", b)
						socketHandle.Write(b)
					}
					rw.RUnlock()
				}
			}

		}

		log.Println("Data Flush\r\n")
		// cmd = strings.Trim(cmd, "\n")
		// log.Println(cmd + "'")
		rw.RLock()
		adduser := 1
		for _, usr := range gUserList {
			if usr.Msg_useruid == backmsg.Msg_useruid {
				adduser = 0
				usr.Msg_userpushid = backmsg.Msg_userpushid
				usr.Msg_JionMeetID = backmsg.Msg_JionMeetID
				usr.Msg_CreateMeetID = backmsg.Msg_CreateMeetID
				usr.Msg_useruid = backmsg.Msg_useruid
				usr.Msg_userName = backmsg.Msg_userName
				usr.Msg_userlevel = backmsg.Msg_userlevel
				usr.Msg_meetName = backmsg.Msg_meetName
				break
			}
		}
		if adduser == 1 {
			var usr MsgStruct
			usr.Msg_userpushid = backmsg.Msg_userpushid
			usr.Msg_JionMeetID = backmsg.Msg_JionMeetID
			usr.Msg_CreateMeetID = backmsg.Msg_CreateMeetID
			usr.Msg_useruid = backmsg.Msg_useruid
			usr.Msg_userName = backmsg.Msg_userName
			usr.Msg_userlevel = backmsg.Msg_userlevel
			usr.Msg_meetName = backmsg.Msg_meetName

			gMeetingList = append(gMeetingList, usr)
			log.Printf("Add Newer User :%s\r\n", backmsg.Msg_useruid)
		}
		rw.RUnlock()
	}
}
