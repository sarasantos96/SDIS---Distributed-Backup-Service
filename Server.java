import java.net.*;
import java.io.*;
import java.util.*;

public class Server{
  //Multicast
  private String mcast_addr;
  private int mcast_port;
  private InetAddress mc_inetAddr;
	private MulticastSocket mcsocket;
  private int server_id;

  public Server(String mcast_addr, int mcast_port, int server_id) throws IOException{
		this.mcast_addr = mcast_addr;
		this.mcast_port = mcast_port;
    this.server_id = server_id;
    this.mc_inetAddr = InetAddress.getByName(this.mcast_addr);
		this.mcsocket = new MulticastSocket(this.mcast_port);
		this.mcsocket.setTimeToLive(1);
    this.mcsocket.joinGroup(this.mc_inetAddr);
	}

  public void receiveMulticastMessage() throws IOException{
    while(true){
      byte[] buf = new byte[254];
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      this.mcsocket.receive(packet);
      String msg = new String(packet.getData());
      msg.trim();
      String [] rcv = msg.split(":");
      String message = new String(rcv[0].trim());
      int senderid = Integer.parseInt(rcv[1].trim());
      if(senderid != this.server_id)
        System.out.println("Multicast message: "+message);
    }
  }

  private class ServiceThread extends Thread{
    public void run(){
      try{

      }catch(IOException e){
        System.out.println("Exception caught");
      }
    }
  }
}
