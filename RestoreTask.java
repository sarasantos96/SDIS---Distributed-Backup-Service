import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Date;

class RestoreTask implements Runnable
{
    private int id;
    private MulticastSocket mcsocket;
    private InetAddress mc_inetAddr;
    private int mc_port;
    private String fileid;
    private int number_of_chunks;
    private String filename;

    public RestoreTask(String fileid, String filename, int number_of_chunks, int id, MulticastSocket mcsocket, InetAddress mc_inetAddr, int mc_port)
    {
        this.id = id;
        this.mcsocket = mcsocket;
        this.mc_inetAddr = mc_inetAddr;
        this.mc_port = mc_port;
        this.fileid = fileid;
        this.number_of_chunks = number_of_chunks;
        this.filename = filename;
    }

    public void sendMCMessage(byte[] bytes) throws IOException{
      DatagramPacket packet = new DatagramPacket(bytes,bytes.length,this.mc_inetAddr,this.mc_port);
      this.mcsocket.send(packet);
    }

    public void processRestore(String filename, String file_id, int number_of_chunks) throws IOException{
      String folder_name = new String("./Peer" + this.id + "/tmp/" + fileid);
      boolean create_folder = (new File(folder_name)).mkdirs();
      int chunk_number = 1;
      String version = "1";

      for(int i = 0; i < number_of_chunks; i++){
        RestoreControlMessage cm = new RestoreControlMessage(RestoreControlMessage.MsgType.GETCHUNK, version, this.id, fileid, i + 1);
        byte[] bytes = cm.createMessage();
        sendMCMessage(bytes);
      }

      int i = 0;
      while(i != number_of_chunks){
        i = new File("Peer" + this.id + "/tmp/" + fileid).list().length;
      }

      joinFiles(fileid, filename, number_of_chunks);

      deleteRestoreChunks(new File(folder_name));
    }

    public void joinFiles(String fileid, String filename, int n_chunks) throws IOException{
      File output_file = new File("Peer" + this.id + "/" + "restored_" + filename);
      String chunks_path = "Peer" + this.id + "/tmp/" + fileid + "/";
      System.out.println(chunks_path);
      FileInputStream file_input_stream;
      FileOutputStream file_output_stream;
      int n_bytes_read = 0;
      byte[] input_bytes;

      try{
        file_output_stream = new FileOutputStream(output_file, true);
        for(int i = 0; i < n_chunks; i++){
          String input_file_name = chunks_path + fileid + "_" + (i + 1);

          File input_file = new File(input_file_name);
          file_input_stream = new FileInputStream(input_file);
          input_bytes = new byte[(int) input_file.length()];
          n_bytes_read = file_input_stream.read(input_bytes, 0, (int) input_file.length());
          file_output_stream.write(input_bytes);
          file_output_stream.flush();
          file_input_stream.close();
          file_input_stream = null;
          input_bytes = null;

        }
        file_output_stream.close();
        file_output_stream = null;
      }catch(Exception e){
        System.out.println(e.getClass().getSimpleName());
        e.printStackTrace(new PrintStream(System.out));
      }
    }

    //http://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
    //erickson's response
    void deleteRestoreChunks(File f) throws IOException {
      if (f.isDirectory()) {
        for (File c : f.listFiles())
          deleteRestoreChunks(c);
      }
      if (!f.delete())
        throw new FileNotFoundException("Failed to delete file: " + f);
    }

    @Override
    public void run()
    {
        try
        {
          processRestore(this.filename, this.fileid, this.number_of_chunks);
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }

    }
}
