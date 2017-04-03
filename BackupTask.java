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

    public BackupTask(Message message, int server_id)
    {
        this.message = message;
        this.server_id = server_id;
    }

    public void saveChunck(byte[] receive_bytes) throws FileNotFoundException, IOException{
      String directory = new String("Peer"+this.server_id+"/test.png");
      FileOutputStream fos = new FileOutputStream(directory);
      fos.write(receive_bytes);
      fos.close();
    }

    @Override
    public void run()
    {
        try
        {
          saveChunck(message.getBody());
        }
        catch (Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
    }
}
