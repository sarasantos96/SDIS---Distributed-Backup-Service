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

    public RemovedTask(Message message, int server_id, ReplicationControl control){
        this.message = message;
        this.server_id = server_id;
        this.control = control;
        this.fileId = this.message.getFileId();
        this.chunkNo = this.message.getChunkNo();
    }

    public void updateReplicationDeg() throws IOException{
      if(this.control.isChunkOwner(new String(this.fileId+"_"+this.chunkNo))){
        this.control.decreaseRepDeg(this.fileId,this.chunkNo);
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
