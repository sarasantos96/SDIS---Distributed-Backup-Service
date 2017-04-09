import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.io.BufferedReader;
import java.nio.file.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.DatatypeConverter;
import java.nio.file.Path;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

class ProcessBackupTask implements Runnable
{
    public static final int CHUNCK_SIZE = 64000;

    private String filename;
    private int replicationDeg;
    private int serverid;
    private ReplicationControl control;
    private MulticastSocket socket;
    private InetAddress addr;
    private int port;
    private ExecutorService executor;
    private MyFilesLog myfiles;

    public ProcessBackupTask(String filename, int replicationDeg, int serverid,ReplicationControl control, MulticastSocket socket, InetAddress addr, int port, ExecutorService executor,MyFilesLog myfiles){
        this.filename = filename;
        this.replicationDeg = replicationDeg;
        this.serverid = serverid;
        this.control = control;
        this.socket = socket;
        this.port = port;
        this.executor = executor;
        this.addr = addr;
        this.myfiles = myfiles;
    }

    public void initiateBackup(){
      File input_file = new File(this.filename);
  		FileInputStream file_input_stream;
  		long file_size = input_file.length();
  		int read = 0, n_bytes = CHUNCK_SIZE;
  		byte[] byte_chunk;
      int n_chunks = 0;

  		try{
  			file_input_stream = new FileInputStream(input_file);
  			while(file_size > 0){
  				if(file_size <= CHUNCK_SIZE)
  					n_bytes = (int) file_size;
  				byte_chunk = new byte[n_bytes];
  				read = file_input_stream.read(byte_chunk, 0, n_bytes);
  				file_size = file_size - read;
  				n_chunks++;
          String fileId = createHashedName(filename);
          String chunkname = fileId + "_"+n_chunks;
          myfiles.addNewLog(fileId,this.filename,this.replicationDeg,5);
  				Runnable task = new PutchunkTask(byte_chunk, fileId, n_chunks,this.replicationDeg, this.serverid,this.socket, this.addr,this.port, this.control);
          executor.execute(task);
  				byte_chunk = null;
  			}
  			file_input_stream.close();
        if(read == 64000){
          n_chunks++;
          String fileId = createHashedName(filename);
          String chunkname = fileId + "_"+n_chunks;
          byte_chunk = new String("").getBytes();
          Runnable task = new PutchunkTask(byte_chunk, fileId, n_chunks,this.replicationDeg, this.serverid,this.socket, this.addr,this.port, this.control);
          executor.execute(task);
        }
  		}catch(Exception e){
  			System.out.println(e.getClass().getSimpleName());
  			e.printStackTrace(new PrintStream(System.out));
  		}
    }
    public static String hash(String text){
      try{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        String s = DatatypeConverter.printHexBinary(hash);
        return s;
      }catch(Exception e){
        System.out.println(e.getClass().getSimpleName());
        e.printStackTrace(new PrintStream(System.out));
        return null;
      }

    }

    public static String createName(String name) throws IOException{
      Path path = Paths.get(name);
      BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
      long size = attr.size();
      FileTime modified_date = attr.lastModifiedTime();

      String string_to_hash = name + size + modified_date;
      return string_to_hash;
    }

    public static String createHashedName(String name) throws IOException{
      String s = createName(name);
      s = hash(s);
      return s;
    }

    @Override
    public void run()
    {
        try
        {
          initiateBackup();
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
    }
}
