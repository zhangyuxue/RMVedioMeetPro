package com.hbajlive.net;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by shensky on 2018/1/15.
 */

public class TcpCenter {
    private static TcpCenter instance;
    private static final String TAG = "TaskCenter";

    //    Socket
    private Socket m_socket =null;
//    IP地址
    private String m_ipAddress;
//    端口号
    private int m_port;
    private Thread m_thread =null;

//    Socket输出流
    private OutputStream outputStream;
//    Socket输入流
    private InputStream inputStream;
//    连接回调
    private OnServerConnectedCallbackBlock connectedCallback;
//    断开连接回调(连接失败)
    private OnServerDisconnectedCallbackBlock disconnectedCallback;
//    接收信息回调
    private OnReceiveCallbackBlock receivedCallback;
//    构造函数私有化
    private TcpCenter() {
        super();
    }
//    提供一个全局的静态方法
    public static TcpCenter sharedCenter() {
        if (instance == null) {
            synchronized (TcpCenter.class) {
                if (instance == null) {
                    instance = new TcpCenter();
                }
            }
        }
        return instance;
    }

    void start_thread()
    {
        m_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    m_socket = new Socket(m_ipAddress, m_port);
//                    socket.setSoTimeout ( 2 * 1000 );//设置超时时间
                    if (isConnected()) {
                        outputStream = m_socket.getOutputStream();
                        inputStream = m_socket.getInputStream();
                        Log.i(TAG,"连接成功");
                        if (connectedCallback != null) {
                            connectedCallback.callback();
                        }
                        receive_loop();
                    }else {
                        Log.i(TAG,"连接失败");
                        if (disconnectedCallback != null) {
                            disconnectedCallback.callback(new IOException("连接失败"));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"连接异常");
                    if (disconnectedCallback != null) {
                        disconnectedCallback.callback(e);
                    }
                }
            }
        });
        m_thread.start();
    }

    /**
     * 通过IP地址(域名)和端口进行连接
     *
     * @param ipAddress  IP地址(域名)
     * @param port       端口
     */
    public void connect(final String ipAddress, final int port)
    {
        // 先退出旧的线程
        disconnect();

        // 启动连接线程
        m_ipAddress =ipAddress;
        m_port =port;

        synchronized (this)
        {
            if(m_thread==null)
                start_thread();
        }
    }
    /**
     * 判断是否连接
     */
    public boolean isConnected()
    {
        return m_socket!=null && m_socket.isConnected();
    }
    /**
     * 连接
     */
    public void connect() {
        connect(m_ipAddress,m_port);
    }
    /**
     * 断开连接
     */
    public void disconnect()
    {
        boolean notify =false;
        synchronized (this)
        {
            if (m_thread != null) {
                if (m_socket != null) {
                    try {
                        if (outputStream != null)
                            outputStream.close();
                        m_socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    outputStream = null;
                    m_socket = null;
                }

                try {
                    m_thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                m_thread = null;
                notify =true;
            }
        }
        if (disconnectedCallback != null && notify)
            disconnectedCallback.callback(new IOException("断开连接"));
    }
    /**
     * 接收数据
     */
    public void receive_loop()
    {
        String Tempstr =new String();
        while (isConnected())
        {
            try {
                /**得到的是16进制数，需要进行解析*/

                int count = 65535;
                byte[] bt = new byte[count];
                int readCount = inputStream.read(bt);

                Log.w(TAG, "read() got: "+readCount);

//                获取正确的字节
                byte[] bs = new byte[readCount];
                System.arraycopy(bt, 0, bs, 0, readCount);

                int cutpos=0;
                for (int i=0;i<readCount;i++)
                {
                    if(bs[i] == '\n')
                    {
                        byte[] bsStr = new byte[i-cutpos];
                        System.arraycopy(bs, cutpos, bsStr, 0, i-cutpos);
                        String str = new String(bsStr, "UTF-8");
                        if(cutpos == 0)
                        {
                            str = Tempstr+str;
                            Tempstr="";
                        }
                        if (str != null) {
                            if (receivedCallback != null) {
                                receivedCallback.callback(str);
                            }
                        }
                        cutpos=i;
                    }
                }

                if(cutpos != (readCount-1))
                {
                    //说明数据没有接收完全，缓存数据
                    byte[] bsStr = new byte[readCount-cutpos];
                    System.arraycopy(bs, cutpos, bsStr, 0, readCount-cutpos);
                    String str = new String(bsStr, "UTF-8");
                    Tempstr +=str;
                }

                Log.i(TAG,"接收成功");
            } catch (IOException e) {
                Log.i(TAG,"接收失败");
            }
        }
    }
    /**
     * 发送数据
     *
     * @param data  数据
     */
    public boolean send(final byte[] data)
    {
        synchronized (this) {
            if (null ==outputStream)
                return false;
            try {
                outputStream.write(data);
                outputStream.flush();
                Log.w(TAG, "send() done.");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 回调声明
     */
    public interface OnServerConnectedCallbackBlock {
        void callback();
    }
    public interface OnServerDisconnectedCallbackBlock {
        void callback(IOException e);
    }
    public interface OnReceiveCallbackBlock {
        void callback(String receicedMessage);
    }

    public void setConnectedCallback(OnServerConnectedCallbackBlock connectedCallback) {
        this.connectedCallback = connectedCallback;
    }

    public void setDisconnectedCallback(OnServerDisconnectedCallbackBlock disconnectedCallback) {
        this.disconnectedCallback = disconnectedCallback;
    }

    public void setReceivedCallback(OnReceiveCallbackBlock receivedCallback) {
        this.receivedCallback = receivedCallback;
    }
    /**
     * 移除回调
     */
    private void removeCallback()
    {
        connectedCallback = null;
        disconnectedCallback = null;
        receivedCallback = null;
    }
}
