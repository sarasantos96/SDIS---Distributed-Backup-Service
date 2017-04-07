import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Server{
  private String mc_addr;
  private int mc_port;
  private String mdb_addr;
  private int mdb_port;
  private String mdr_addr;
  private int mdr_port;
  private InetAddress mc_inetAddr;
	private MulticastSocket mcsocket;
  private InetAddress mdb_inetAddr;
	private MulticastSocket mdbsocket;
  private InetAddress mdr_inetAddr;
	private MulticastSocket mdrsocket;
  private int server_id;
  private MCThread mcthread;
  private MDBThread mdbthread;
  private MDRThread mdrthread;
  private ExecutorService peerExecutor;
  private ReplicationControl control;

  public Server(String mc_addr, int mc_port, int serverid , String mdb_addr, int mdb_port, String mdr_addr, int mdr_port, ExecutorService peerExecutor, ReplicationControl control) throws IOException{
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.mdb_addr = mdb_addr;
    this.mdb_port = mdb_port;
    this.mdr_addr = mdr_addr;
    this.mdr_port = mdr_port;
    this.server_id = serverid;
    this.peerExecutor = peerExecutor;
    this.control = control;

    //Threads
    this.mcthread = new MCThread();
    this.mdbthread = new MDBThread();
    this.mdrthread = new MDRThread();
	}

  public void start_channels(){
    this.mcthread.start();
    this.mdbthread.start();
    this.mdrthread.start();
  }

  private class MCThread extends Thread {
    public void run(){
      try{
        mc_inetAddr = InetAddress.getByName(mc_addr);
        mcsocket = new MulticastSocket(mc_port);
        mcsocket.setTimeToLive(1);
        mcsocket.joinGroup(mc_inetAddr);

        while(true){
          byte[] buf = new byte[1000];
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          mcsocket.receive(packet);
          String type = getControlType(packet);

          if(type.equals("STORED")){
            Message message = new Message(packet.getData());
            if(message.getsenderid() != server_id && control.isChunkOwner(message.getFileId()+"_"+message.getChunkNo())){
                control.updateRepDeg(message.getFileId()+"_"+message.getChunkNo());
            }

          }
          if(type.equals("GETCHUNK")){
            RestoreControlMessage message = new RestoreControlMessage(packet.getData());
            if(message.getSenderId() != server_id){
              message.print();
              Runnable task = new ChunkTask(message,server_id,mdrsocket,mdr_inetAddr,mdr_port);
              peerExecutor.execute(task);
            }
          }
        }
      }catch(Exception e){
        System.out.println(e.getClass().getSimpleName());
      }
    }
  }

  public static String getControlType(DatagramPacket packet){
    byte[] bytes = packet.getData();
    String text = new String(bytes);

    String[] parts = text.split(" ");
    return parts[0];
  }

  private class MDBThread extends Thread{
    public void run(){
      try{
        mdb_inetAddr = InetAddress.getByName(mdb_addr);
        mdbsocket = new MulticastSocket(mdb_port);
        mdbsocket.setTimeToLive(1);
        mdbsocket.joinGroup(mdb_inetAddr);

        while(true){
          byte[] buf = new byte[80000];
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          mdbsocket.receive(packet);
          Message msg = new Message(packet.getData());
          if(server_id != msg.getsenderid()){
            System.out.println("MDB message: "+ "backup");
            Runnable task = new BackupTask(msg,server_id,mcsocket,mc_inetAddr,mc_port);
            peerExecutor.execute(task);
          }
        }
      }catch(Exception e){
        System.out.println("Exception caught");
      }
    }
  }

  private class MDRThread extends Thread{
    public void run(){
      try{
        mdr_inetAddr = InetAddress.getByName(mdr_addr);
        mdrsocket = new MulticastSocket(mdr_port);
        mdrsocket.setTimeToLive(1);
        mdrsocket.joinGroup(mdr_inetAddr);

        while(true){
          System.out.println("begin");
          byte[] buf = new byte[80000];
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          mdrsocket.receive(packet);
          System.out.println(packet.getData().length);
          System.out.println(new String(packet.getData()));
          return;
          /*
          Message msg = new Message(packet.getData());
          if(server_id != msg.getsenderid()){
            System.out.println("MDR message: "+ "restore");
*/
            //TODO: código repetido
            /*File input_file = new File("./Peer"+server_id+"/r_test.txt");
            if(input_file.exists() && !input_file.isDirectory()){
              System.out.println("ei");
              FileInputStream file_input_stream = new FileInputStream(input_file);
              int n_bytes = (int) input_file.length();
              byte[] file_bytes = new byte[n_bytes];
              int read = file_input_stream.read(file_bytes,0,n_bytes);
              file_input_stream.close();
              System.out.println(new String(file_bytes));
              packet = new DatagramPacket(file_bytes,file_bytes.length,mdr_inetAddr,mdr_port);
              mdrsocket.send(packet);
              Thread.sleep(400);
            }*//*
          }*/
        }
      }catch(Exception e){
        System.err.println("Server exception: " + e.toString());
        e.printStackTrace();
      }
    }
  }
}
