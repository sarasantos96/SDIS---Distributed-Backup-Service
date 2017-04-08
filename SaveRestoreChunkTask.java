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

    public boolean checkFolder(){
      File f = new File("Peer" + this.server_id + "/tmp/" +this.message.getFileId());
      return (f.exists() && f.isDirectory());

    }

    public void saveChunkFile() throws FileNotFoundException, IOException, InterruptedException{
      String file_name = new String("Peer"+this.server_id+ "/tmp/" +this.message.getFileId()+"/"+this.message.getFileId()+"_"+this.message.getChunkNo());
      File file = new File(file_name);
      if(!file.exists()){
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(this.message.getBody());
        fos.close();
      }
    }

    @Override
    public void run(){
        try
        {
          if(checkFolder())
            saveChunkFile();
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
          
    }
}
