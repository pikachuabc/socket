package jiafan;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class demo {
    ServerSocket ss = null;	//服务器的的套接字，用于监听来自客户端的连接请求
    boolean started = false;

    List<Client> clients = new ArrayList<Client>();//维护一个客户端链表，用于记录已经连接上服务端的客户端们
    /*主程序*/
    public static void main(String[] args) {
        new demo().start();	//调用本类的start()方法。因为服务端的操作在 start()方法中。
    }

    /*服务端的操作*/
    public void start(){
        try {
            ss = new ServerSocket(8888); //指定服务端套接字的端口号为8888(可以任意指定，但必须大于1024)
            started = true;

        } catch (BindException e) { //若捕捉到此异常，表明指定的端口号已经被占用了
            System.out.println("端口使用中..请重新启动程序");
            System.exit(0);		//程序退出
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            while(started){
                Socket s = ss.accept();	//创建一个用于管理客户端连接的客户端套接字。每当有新的客户端连接，服务端就新建一个套接字给此客户端
                Client c = new Client(s);//每当一个客户端连上服务端，就新建一个Client类，指代此客户端，并传入客户端套接字

                new Thread(c).start();	//新建一个子线程，并启动它
                clients.add(c);		//把Client对象加入到客户端列表中
            }
        } catch(IOException e){
            e.printStackTrace();
        } finally{
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /*Client类实现Runnable接口，为一个线程类。*/
    class Client implements Runnable {
        Socket s = null;				//客户端套接字的引用
        DataInputStream dis = null;		//输入流的引用
        DataOutputStream dos = null;	//输出流的引用
        boolean bConnected = false;		//服务端是否已经获取客户端的输入流，输出流

        public Client(Socket s) {	//Client类的带参构造函数
            this.s = s;	//Client类中的客户端套接字引用指向 传入的客户端套接字
            try {
                dis = new DataInputStream(s.getInputStream());	//getInoutStream()用于获取客户端套接字自带的输入流
                dos = new DataOutputStream(s.getOutputStream());//getOutoutStream()用于获取客户端套接字自带的输出流
                bConnected = true;	//成功获取客户端的输入流，输出流后，把bConnected设置为true
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*用于向客户端发送信息*/
        public void send(String str){
            try {
                dos.writeUTF(str);//向输入流发送信息
            } catch (IOException e) { //当此Client对象出现异常，把此Client从客户端列表clients中删除
                clients.remove(s);
            }
        }
        /*Client线程的操作*/
        public void run() {
            String str = null;
            try{
                while(bConnected){	//当成功获取客户端的 输入流，输出流后。
                    str = dis.readUTF();	//从输入流读取来自客户端的信息
                    //System.out.println(str);
                    for(int i=0;i<clients.size();i++){	//从一个客户端中获取信息后，发此信息发送给已经连接上服务端的其他客户端
                        Client c = clients.get(i);		//用列表序号来获取对应的客户端
                        c.send(str);					//向以序号遍历的客户端发送信息
                    }
                }
            } catch (EOFException e){	//由于客户端关闭了的时候，服务端不能从流中读取信息，就会报此异常。表示客户端关闭了
                System.out.println("Clinet closed!");
            } catch (IOException e){
                e.printStackTrace();
            }
            finally{
                try {
                    if(s!=null) s.close();	//关闭客户端套接字
                    if(dos!=null) dos.close();//关闭此客户端的输出流
                    if(dis!=null) dis.close();//关闭此客户端的输入流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


