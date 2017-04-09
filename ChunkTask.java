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

    public File checkFile(){
      File file = new File("Peer" + this.server_id + "/" + this.message.getFileId() + "_" + this.message.getChunkNo());
      if(file.exists() && !file.isDirectory())
        return file;
      else
        return null;
    }

    public byte[] getChunkBytes(File file) throws FileNotFoundException, IOException{
      long file_size = file.length();
      FileInputStream file_input_stream = new FileInputStream(file);
      byte[] byte_chunk = new byte[(int) file_size];
      file_input_stream.read(byte_chunk, 0, (int) file_size);
      System.out.println(byte_chunk.length);

      return byte_chunk;
    }

    public void sendChunkMessage(byte[] bytes) throws IOException, InterruptedException{
      RestoreMessage rm = new RestoreMessage(RestoreMessage.MsgType.CHUNK, "1", this.server_id, this.message.getFileId(), this.message.getChunkNo());

      byte[] rm_bytes = rm.createMessage(bytes);

      DatagramPacket packet = new DatagramPacket(rm_bytes,rm_bytes.length,this.mc_inetAddr,this.mc_port);
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
          File file = checkFile();
          System.out.println("OIOI");
          if(file != null){
            
            byte[] bytes = getChunkBytes(file);
            sendChunkMessage(bytes);
          }
          else{
            return;
          }
            
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
          
    }
}
