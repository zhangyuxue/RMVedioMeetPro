package workvideo.fvideo.net;

public class QSMeet {

    int index;
    String meetid;
    String meetname;
    String state;
    String time;

    public QSMeet(int Index,
            String Meetid,
            String Meetname,
            String State,
            String Time) {
        this.index = Index;
        this.meetid = Meetid;
        this.meetname = Meetname;
        this.state = State;
        this.time=Time;
    }

    @Override
    public String toString() {
//        return "Account [uid=" + uid + ", userName=" + userName + ", password=" + password + ", telNumber=" + telNumber
//                + "]";
        return "";
    }
}