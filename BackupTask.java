import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class BackupTask implements Runnable
{
    private Message message;
    private int server_id;
    private MulticastSocket mcsocket;
    private InetAddress mc_inetAddr;
    private int mc_port;

    public BackupTask(Message message, int server_id, MulticastSocket mcsocket, InetAddress mc_inetAddr, int mc_port)
    {
        this.message = message;
        this.server_id = server_id;
        this.mcsocket = mcsocket;
        this.mc_inetAddr = mc_inetAddr;
        this.mc_port = mc_port;
    }

    public void saveChunck(byte[] receive_bytes) throws FileNotFoundException, IOException{
      String directory = new String("Peer"+this.server_id+ "/"+this.message.getFileId());
      FileOutputStream fos = new FileOutputStream(directory);
      fos.write(receive_bytes);
      fos.close();
    }

    public void sendStoredMessage() throws IOException{
      Message stored = new Message(Message.MsgType.STORED,this.server_id);
      stored.setFileID(this.message.getFileId());
      byte[] stored_msg = stored.createStoredMessage(message.getChunkNo());

      DatagramPacket packet = new DatagramPacket(stored_msg,stored_msg.length,this.mc_inetAddr,this.mc_port);
      this.mcsocket.send(packet);

    }

    @Override
    public void run()
    {
        try
        {
          saveChunck(message.getBody());
          sendStoredMessage();
        }
        catch(Exception e)
        {
          System.out.println("oi");
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
    }
}
