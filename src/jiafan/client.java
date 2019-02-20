package jiafan;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class client extends Frame {	//继承Frame类，用于给ChatClient类创建界面

    Socket s = null;	//用于连接服务端的套接字
    DataInputStream dis = null;	 //用于获取来自服务端的信息的输入流
    DataOutputStream dos = null; //用于向服务端发送信息的输出流
    boolean bConnected = false;	// 连接服务端的标志位

    TextField tfTxt = new TextField();	//创建一个文本框（放在界面中）
    TextArea taContent = new TextArea();//创建一个消息框(放在界面中)

    public static void main(String[] args) {	//主函数
        new client().lauchFrame();			//lauchFrame()用于设置界面布局

    }
    /*设置界面布局*/
    public void lauchFrame() {
        this.setLocation(400, 300);	//界面在电脑屏幕中的显示位置
        this.setSize(300, 300);	//界面大小(长，宽)
        add(tfTxt, BorderLayout.SOUTH);	//把已创建的文本框加入到界面中的南方(即下方)
        add(taContent, BorderLayout.NORTH);	//把已创建的消息框加入到界面中的北方(即上方)
        this.addWindowListener(new WindowAdapter() {	//加入window监听器到界面中
            public void windowClosing(WindowEvent e) {	//当鼠标点击右上角的'X'时，会自动调用windowClosing()
                disconnect();	//调用disconnect()函数，用于断开与服务端的连接
                System.exit(0);	//程序退出
            }
        });
        pack();	//将界面上的控件位置自适应放好
        tfTxt.addActionListener(new TFListener()); //加入Action监听器到文本框中
        setVisible(true);	//使界面显示出来
        connect();			//调用connect()，用于连接服务端
        new Thread(new RecvThread()).start();	//创建一个新线程，用于接收来自服务端的信息
    }

    /*用于连接服务端*/
    public void connect() {
        try {
            s = new Socket("127.0.0.1", 8888); //创建套接字对象，第一个参数为服务端所在的ip地址，后一个参数为服务端的端口号
            dis = new DataInputStream(s.getInputStream());	//创建输入流，用于接收服务端的信息
            dos = new DataOutputStream(s.getOutputStream());	//创建输出流，用于向服务端发送信息
            bConnected = true;	//设置 连接服务端的标志位 为真
            /**-------------------
            客户端上的使用
            1.getInputStream方法可以得到一个输入流，客户端的Socket对象上的getInputStream方法得到输入流其实就是从服务器端发回的数据。
            2.getOutputStream方法得到的是一个输出流，客户端的Socket对象上的getOutputStream方法得到的输出流其实就是发送给服务器端的数据。

            服务器端上的使用
            1.getInputStream方法得到的是一个输入流，服务端的Socket对象上的getInputStream方法得到的输入流其实就是从客户端发送给服务器端的数据流。
            2.getOutputStream方法得到的是一个输出流，服务端的Socket对象上的getOutputStream方法得到的输出流其实就是发送给客户端的数据。
            ---------------------**/

            System.out.println("Client connected!");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*当要断开与服务端的连接时，作一些处理*/
    public void disconnect() {
        try {
            dos.close();	//断开输出流
            dis.close();	//断开输入流
            s.close();		//断开套接字
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /*自定义Action监听器的内容*/
    private class TFListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {//实现ActionListener接口中的actionPerformed()方法
            String str = tfTxt.getText().trim(); 	//当按下回车后，获取文本框中的信息。trim()用于消除文本信息前面和后尾的空格
            tfTxt.setText("");						//(发送信息后)把文本框置空。

            try {
                dos.writeUTF(str);	//往输出流写数据(用于发信息给服务端)
                dos.flush();		//强制把流缓冲区里的信息发出去。（否则系统会等到缓冲区满了以后才会发出去）
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /*自定义用于接收来自服务端的信息的子线程*/
    private class RecvThread implements Runnable{

        @Override
        public void run() {	//子线程工作的内容都写在 Runnable接口的 run()方法里
            try{
                while(bConnected){	//当bConnected 为true时，表示客户端连接服务端成功
                    String str = dis.readUTF();	//从输入流中获取来自服务端的信息
                    taContent.setText(taContent.getText()+str+'\n');	//把来自服务端的信息加上消息框以前的信息一起在消息框中打印出来
                    //System.out.println(str);
                }
            } catch(IOException e){	//当出现IO异常时，很可能是因为客户端程序被用户关闭了，此时打印“client close”
                System.out.println("client close!");
            }

        }

    }

}

