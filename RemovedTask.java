import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Date;

class RemovedTask implements Runnable
{
    private Message message;
    private int server_id;
    private ReplicationControl control;
    private String fileId;
    private int chunkNo;
    private StoredControl storedcontrol;
    private MulticastSocket socket;
    private InetAddress addr;
    private int port;
    private byte[] body;

    public RemovedTask(Message message, int server_id, ReplicationControl control,StoredControl storedcontrol,MulticastSocket socket, InetAddress addr, int port){
        this.message = message;
        this.server_id = server_id;
        this.control = control;
        this.fileId = this.message.getFileId();
        this.chunkNo = this.message.getChunkNo();
        this.storedcontrol = storedcontrol;
        this.socket = socket;
        this.addr = addr;
        this.port = port;
    }

    public void sendChunk(String chunkname) throws FileNotFoundException, IOException, InterruptedException{
      File input_file = new File("./Peer"+this.server_id+"/"+chunkname);
      int file_size = (int)input_file.length();
      int read = 0;
      FileInputStream file_input_stream = new FileInputStream(input_file);
      this.body = new byte[file_size];
      read = file_input_stream.read(this.body, 0, file_size);
      Random r = new Random();
      int wait = r.nextInt(400);
      Thread.sleep(wait);
      sendMDBMessage(chunkname);

    }

    public void sendMDBMessage(String chunkname) throws IOException, InterruptedException{
        Message msg = new Message(Message.MsgType.PUTCHUNK, server_id);
        msg.setFileID(fileId);
        msg.setVersion("1.0");
        byte[] full_msg = msg.createMessage(this.body,this.chunkNo,this.storedcontrol.getReplicationDeg(chunkname));
        DatagramPacket packet = new DatagramPacket(full_msg,full_msg.length,this.addr,this.port);
        socket.send(packet);

        boolean continues = true;
        int tries = 0;
        int time = 1000;
        while(continues && tries < 5){
          Thread.sleep(time);
          if(this.storedcontrol.getAtualRepDeg(chunkname) < this.storedcontrol.getReplicationDeg(chunkname))
            socket.send(packet);
          else
            continues = false;
          tries++;
          time = time * 2;
        }
    }

    public void updateReplicationDeg() throws FileNotFoundException, IOException, InterruptedException{
      String chunkname = new String(message.getFileId()+"_"+message.getChunkNo());
      if(this.control.isChunkOwner(chunkname)){
        this.control.deleteAllSenderEntries(message.getFileId(), message.getsenderid());
      }if(this.storedcontrol.isChunkStored(chunkname) && this.storedcontrol.isPeerStored(chunkname,""+message.getsenderid())){
        this.storedcontrol.removePeer(chunkname,""+message.getsenderid());
        if(this.storedcontrol.getAtualRepDeg(chunkname) < this.storedcontrol.getReplicationDeg(chunkname) ){
          sendChunk(chunkname);
        }
      }
    }

    @Override
    public void run()
    {
        try
        {
          updateReplicationDeg();
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
    }
}
