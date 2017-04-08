import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Date;

class SaveRestoreChunkTask implements Runnable{
    private RestoreMessage message;
    private int server_id;

    public SaveRestoreChunkTask(RestoreMessage message, int server_id){
        this.message = message;
        this.server_id = server_id;
    }

    public void saveChunkFile() throws FileNotFoundException, IOException, InterruptedException{
      String directory = new String("Peer"+this.server_id+ "/"+this.message.getFileId()+"/"+this.message.getFileId()+"_"+this.message.getChunkNo());
      File file = new File(directory);
      file.getParentFile().mkdir();
      if(!file.exists()){
        FileOutputStream fos = new FileOutputStream(directory);
        fos.write(this.message.getBody());
        fos.close();
      }
    }

    @Override
    public void run(){
        try
        {
          saveChunkFile();
            
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
          
    }
}
