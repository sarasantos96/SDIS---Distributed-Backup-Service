import java.io.*;
import java.net.*;
import java.util.*;

public class Client{
  //Multicast
  private String mc_addr;
  private int mc_port;
  private InetAddress mcaddr;
  private MulticastSocket mcsocket;

  public Client(String mc_addr, int mc_port) throws UnknownHostException, InterruptedException, IOException{
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.mcaddr = InetAddress.getByName(this.mc_addr);
    this.mcsocket = new MulticastSocket(this.mc_port);
    this.mcsocket.setTimeToLive(1);
  }

  public void sendMulticastMessage(int senderid) throws IOException{
    System.out.println("Write something: ");
    String input = System.console().readLine();
    String message = new String(input +":"+ senderid);
    DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(),mcaddr,mc_port);
    mcsocket.send(packet);
  }
}
