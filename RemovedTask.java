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

    public RemovedTask(Message message, int server_id, ReplicationControl control,StoredControl storedcontrol,MulticastSocket socket, InetAddress addr, int port){
        this.message = message;
        this.server_id = server_id;
        this.control = control;
        this.fileId = this.message.getFileId();
        this.chunkNo = this.message.getChunkNo();
        this.storedcontrol = storedcontrol;
    }

    public void sendMDBMessage() throws IOException, InterruptedException{
        /*Message msg = new Message(Message.MsgType.PUTCHUNK, serverid);
        msg.setFileID(fileId);
        msg.setVersion("1.0");
        byte[] full_msg = msg.createMessage(body,chunkNo,replicationDeg);
        DatagramPacket packet = new DatagramPacket(full_msg,full_msg.length,this.addr,this.port);
        socket.send(packet);

        boolean continues = true;
        int tries = 0;
        String chunkname = new String(fileId+"_"+chunkNo);
        while(continues && tries < 5){
          Thread.sleep(1000);
          if(this.control.getAtualRepDeg(chunkname) < this.replicationDeg)
            socket.send(packet);
          else
            continues = false;
          tries++;
        }*/
    }

    public void updateReplicationDeg() throws IOException{
      String chunkname = new String(message.getFileId()+"_"+message.getChunkNo());
      if(this.control.isChunkOwner(chunkname)){
        this.control.deleteAllSenderEntries(message.getFileId(), message.getsenderid());
      }if(this.storedcontrol.isChunkStored(chunkname) && this.storedcontrol.isPeerStored(chunkname,""+message.getsenderid())){
        this.storedcontrol.removePeer(chunkname,""+message.getsenderid());
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
