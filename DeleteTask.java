import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Date;

class DeleteTask implements Runnable{
    private String fileId;
    private ReplicationControl control;
    private int id;

    public DeleteTask(String fileId, ReplicationControl control, int id){
      this.fileId = fileId;
      this.control = control;
      this.id = id;
    }

    public ArrayList<String> getChunkNames(){
      ArrayList<String> chunkNames = new ArrayList<String>();

      File folder = new File("./Peer"+this.id);
      File[] listOfFiles = folder.listFiles();

      for (int i = 0; i < listOfFiles.length; i++) {
        if (listOfFiles[i].isFile()) {
          String name = listOfFiles[i].getName();
          String chunckname = name.split("_")[0];
          if(chunckname.equals(this.fileId))
            chunkNames.add(name);
        }
      }

      return chunkNames;
    }

    public void deleteAllChunks(){
      ArrayList<String> chunkNames = getChunkNames();
      if(chunkNames.size() != 0){
        for(String filename : chunkNames){
          String path = new String("./Peer"+this.id+"/"+filename);
          File f = new File(path);
          f.delete();
        }
      }
    }

    @Override
    public void run()
    {
        try
        {
          deleteAllChunks();
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
    }
}
