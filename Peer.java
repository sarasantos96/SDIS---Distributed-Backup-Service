import java.io.*;
import java.net.*;
import java.util.*;

public class Peer{
  private String mc_addr;
  private int mc_port;
  private Client client;
  private Server server;
  private int serverid;
  private AdvertiseThread thread;

  public Peer(int server_id,String mc_addr, int mc_port) throws UnknownHostException, IOException, InterruptedException{
    this.serverid = server_id;
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.client = new Client(this.mc_addr, this.mc_port);
    this.server = new Server(this.mc_addr, this.mc_port, this.serverid);
    this.thread = new ServerThread();
    this.thread.start();
  }

  //java Peer <protocol version> <server id> <access point> <MC> <MDB> <MDR>
  //java <server id> <mc_addr> <mc_port>
  public static void main(String [] args) throws UnknownHostException, IOException, InterruptedException{
    Peer peer = new Peer(Integer.parseInt(args[0]),args[1], Integer.parseInt(args[2]));
    peer.sendMessage();
  }

  public void sendMessage()throws IOException{
    while(true){
      this.client.sendMulticastMessage(this.serverid);
    }
  }

  private class ServerThread extends Thread{
    public void run(){
      try{
        server.receiveMulticastMessage();
      }catch(IOException e){
        System.out.println("Exception caught");
      }
    }
  }

}
