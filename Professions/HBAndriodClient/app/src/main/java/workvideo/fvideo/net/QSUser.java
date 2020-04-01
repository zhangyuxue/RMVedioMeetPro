package workvideo.fvideo.net;

public class QSUser {

    int index;
    String uid;
    String phone;
    String pwd;
    String level;
    String name;
    String state;
    String meetid;
    String role;

    public QSUser(int Index,
            String Uid,
            String Phone,
            String Pwd,
            String Level,
            String Name,
            String State,
            String Meetid,
                  String Role) {
        this.index = Index;
        this.uid = Uid;
        this.phone = Phone;
        this.pwd=Pwd;
        this.level=Level;
        this.name=Name;
        this.state=State;
        this.meetid=Meetid;
        this.role=Role;
    }

    @Override
    public String toString() {
//        return "Account [uid=" + uid + ", userName=" + userName + ", password=" + password + ", telNumber=" + telNumber
//                + "]";
        return "";
    }
}