import java.io.*;
import java.net.*;
import java.util.*;

public class Client{
  //Multicast
  private String mc_addr;
  private int mc_port;
  private String mdb_addr;
  private int mdb_port;
  private String mdr_addr;
  private int mdr_port;
  private InetAddress mcaddr;
  private MulticastSocket mcsocket;
  private InetAddress mdbaddr;
  private MulticastSocket mdbsocket;
  private InetAddress mdraddr;
  private MulticastSocket mdrsocket;

  public Client(String mc_addr, int mc_port, String mdb_addr, int mdb_port, String mdr_addr, int mdr_port) throws UnknownHostException, InterruptedException, IOException{
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.mdb_addr = mdb_addr;
    this.mdb_port = mdb_port;
    this.mdr_addr = mdr_addr;
    this.mdr_port = mdr_port;

    this.mcaddr = InetAddress.getByName(this.mc_addr);
    this.mcsocket = new MulticastSocket(this.mc_port);
    this.mcsocket.setTimeToLive(1);
    this.mdbaddr = InetAddress.getByName(this.mdb_addr);
    this.mdbsocket = new MulticastSocket(this.mdb_port);
    this.mdbsocket.setTimeToLive(1);
    this.mdraddr = InetAddress.getByName(this.mdr_addr);
    this.mdrsocket = new MulticastSocket(this.mdr_port);
    this.mdrsocket.setTimeToLive(1);
  }

  public void sendMulticastMessage(int senderid) throws IOException{
    System.out.println("Write something: ");
    String input = System.console().readLine();

    String[]  inputType = input.split(" ");
    if(inputType[0].equals("Backup")){
      sendMDBMessage(input, senderid);
    }else if(inputType[0].equals("Restore")){
      sendMDRMessage(input,senderid);
    }else{
      sendMCMessage(input,senderid);
    }
  }

  public void sendMCMessage(String message, int senderid) throws IOException{
    String mcmessage = new String(message +":"+ senderid);
    DatagramPacket packet = new DatagramPacket(mcmessage.getBytes(), mcmessage.length(),mcaddr,mc_port);
    mcsocket.send(packet);
  }

  public void sendMDBMessage(String message, int senderid) throws IOException{
    String mdbmessage = new String(message +":"+ senderid);
    DatagramPacket packet = new DatagramPacket(mdbmessage.getBytes(), mdbmessage.length(),mdbaddr,mdb_port);
    mdbsocket.send(packet);
  }

  public void sendMDRMessage(String message, int senderid)throws IOException{
    String mdrmessage = new String(message +":"+ senderid);
    DatagramPacket packet = new DatagramPacket(mdrmessage.getBytes(), mdrmessage.length(),mdraddr,mdr_port);
    mdrsocket.send(packet);
  }

}
