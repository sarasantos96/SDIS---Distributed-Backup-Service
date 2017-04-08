import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Date;

class BackupTask implements Runnable
{
    private Message message;
    private int server_id;
    private MulticastSocket mcsocket;
    private InetAddress mc_inetAddr;
    private int mc_port;
    private Long size;

    public BackupTask(Message message, int server_id, MulticastSocket mcsocket, InetAddress mc_inetAddr, int mc_port,Long size)
    {
        this.message = message;
        this.server_id = server_id;
        this.mcsocket = mcsocket;
        this.mc_inetAddr = mc_inetAddr;
        this.mc_port = mc_port;
        this.size = size;
    }

    public Long folderSize() {
        File directory = new File("./Peer"+this.server_id);
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
        }
        return new Long(length);
    }

    public void saveChunck(byte[] receive_bytes) throws FileNotFoundException, IOException, InterruptedException{
      if(folderSize() + receive_bytes.length < this.size){
        String directory = new String("Peer"+this.server_id+ "/"+this.message.getFileId()+"_"+this.message.getChunkNo());
        File file = new File(directory);
        if(!file.exists()){
          FileOutputStream fos = new FileOutputStream(directory);
          fos.write(receive_bytes);
          fos.close();
          sendStoredMessage();
        }
      }
    }

    public void sendStoredMessage() throws IOException, InterruptedException{
      Message stored = new Message(Message.MsgType.STORED,this.server_id);
      stored.setFileID(this.message.getFileId());
      stored.setVersion("1.0");
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
          saveChunck(message.getBody());
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
    }
}
