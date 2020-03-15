
//  video meeting sdk
//  copyright © 2019 workvideo. All rights reserved.
//
//  author: LinQing
//  phone: 13509391992
//  email: twomsoft@outlook.com

package workvideo.meetingSdk;

public class Error {

    public static final int
        startup_failed =(1001),
        wrong_parameter =(1002),
        out_of_memory =(1003),
        wrong_address =(1004),
        timeout =(1005),
        busy =(1006),
        un_expect =(1007),
        not_connect =(1008),
        not_online =(1009),
        connect_failed =(1010),
        password_required =(1011),
        authorize_failed =(1012),
        not_allowed =(1013),
        not_opened =(1014),
        conflict =(1015)
            ;

    public static String parse(int code)
    {
        switch(code) {
            case (0):
                return "no error";
            case (startup_failed):
                return "startup failed";
            case (wrong_parameter):
                return "wrong parameter";
            case (out_of_memory):
                return "out of memory";
            case (wrong_address):
                return "wrong address";
            case (timeout):
                return "timeout";
            case (busy):
                return "busy";
            case (un_expect):
                return "unExpect";
            case (not_connect):
                return "not connect";
            case (not_online):
                return "not online";
            case (connect_failed):
                return "connect failed";
            case (password_required):
                return "password required";
            case (authorize_failed):
                return "authorize failed";
            case (not_allowed):
                return "not allowed";
            case (not_opened):
                return "room not opened";
            case (conflict):
                return "conflict";
        }
        return "unknown error("+code+")";
    }
}
