import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;



public class Peer{
  private String mc_addr;
  private int mc_port;
  private String mdb_addr;
  private int mdb_port;
  private String mdr_addr;
  private int mdr_port;
  private Client client;
  private Server server;
  private int serverid;
  private ServerThread thread;
  private Long size;

  public ExecutorService executor;
  public ReplicationControl control;


  public Peer(int server_id,String mc_addr, int mc_port,String mdb_addr, int mdb_port,String mdr_addr, int mdr_port) throws UnknownHostException, IOException, InterruptedException{
    this.serverid = server_id;
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.mdb_addr = mdb_addr;
    this.mdb_port = mdb_port;
    this.mdr_addr = mdr_addr;
    this.mdr_port = mdr_port;
    this.size = new Long(50000000);


    //Initiate Thread Pool
    executor = Executors.newFixedThreadPool(4);

    //Initialize folder for peer
    String folder_name = new String("./Peer" + this.serverid);
    boolean create_folder = (new File(folder_name)).mkdirs();

    String logfilename = new String(folder_name +"/"+"logfile.txt");
    this.control = new ReplicationControl(logfilename);

    this.client = new Client(server_id, this.mc_addr, this.mc_port, this.mdb_addr, this.mdb_port, this.mdr_addr, this.mdr_port,this.control,this.size,executor);
    this.server = new Server(this.mc_addr, this.mc_port, this.serverid , this.mdb_addr, this.mdb_port, this.mdr_addr, this.mdr_port, executor,this.control,this.size);
    this.thread = new ServerThread();
    this.thread.start();
  }

  //java Peer <protocol version> <server id> <access point> <MC> <MDB> <MDR>
  //java Peer <server id> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>
  public static void main(String [] args) throws UnknownHostException, IOException, InterruptedException{
    Peer peer = new Peer(Integer.parseInt(args[0]),args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]), args[5], Integer.parseInt(args[6]));
    //peer.sendMessage();
  }

  /*public void sendMessage()throws IOException{
    while(true){
      this.client.sendMulticastMessage(this.serverid);
    }
  }*/

  private class ServerThread extends Thread{
    public void run(){
      try{
        server.start_channels();
      }catch(Exception e){
        System.out.println("Exception caught");
      }
    }
  }

}
