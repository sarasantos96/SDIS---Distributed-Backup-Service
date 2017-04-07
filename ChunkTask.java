import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Date;

class ChunkTask implements Runnable
{
    private RestoreControlMessage message;
    private int server_id;
    private MulticastSocket mcsocket;
    private InetAddress mc_inetAddr;
    private int mc_port;

    public ChunkTask(RestoreControlMessage message, int server_id, MulticastSocket mcsocket, InetAddress mc_inetAddr, int mc_port)
    {
        this.message = message;
        this.server_id = server_id;
        this.mcsocket = mcsocket;
        this.mc_inetAddr = mc_inetAddr;
        this.mc_port = mc_port;
    }

    public boolean checkFile(){
      File file = new File("Peer" + this.server_id + "/" + this.message.getFileId() + "_part_" + this.message.getChunkNo());
      if(file.exists() && !file.isDirectory())
        return true;
      else
        return false;
    }

    public void sendStoredMessage() throws IOException, InterruptedException{
      Message stored = new Message(Message.MsgType.STORED,this.server_id);
      stored.setFileID(this.message.getFileId());
      byte[] stored_msg = stored.createStoredMessage(message.getChunkNo());

      DatagramPacket packet = new DatagramPacket(stored_msg,stored_msg.length,this.mc_inetAddr,this.mc_port);
      Random r = new Random();
      int wait = r.nextInt(400);
      Thread.sleep(wait);
      this.mcsocket.send(packet);

    }

    @Override
    public void run()
    {
        try
        {
          if(checkFile())
            System.out.println("SIM");
          else
            System.out.println("NAO");
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
    }
}
