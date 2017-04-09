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

public class Client implements RMI_Interface{

  //Multicast
  private String mc_addr;
  private int mc_port;
  private String mdb_addr;
  private int mdb_port;
  private String mdr_addr;
  private int mdr_port;
  private InetAddress mcaddr;
  private MulticastSocket mcsocket;
  private InetAddress mdbaddr;
  private MulticastSocket mdbsocket;
  private InetAddress mdraddr;
  private MulticastSocket mdrsocket;
  private int id;
  private ReplicationControl control;
  private Long size;
  private ExecutorService executor;
  private MyFilesLog myfiles;

  public int rmiRequest(String type, String arg1, String arg2) throws IOException, InterruptedException {
    switch(type){
      case "Backup":
        Runnable task = new ProcessBackupTask(arg1, Integer.parseInt(arg2), id, control, this.mdbsocket,this.mdbaddr, this.mdb_port, this.executor,this.myfiles);
        executor.execute(task);
        break;
      case "Restore":
        processRestore(arg1);
        break;
      case "Reclaim":
        String current_path = new java.io.File( "." ).getCanonicalPath();
        boolean isNumeric = arg1.matches("-?\\d+(\\.\\d+)?");
        if(isNumeric)
          reclaimStorage(current_path + "/Peer" + id, Integer.parseInt(arg1));
        else
          return -1;
        break;
      /*default:
        sendMCMessage(arg1, id);*/
      case "Delete":
        processDelete(arg1,id);
        break;
    }
    return 0;
  }

  public Client() {}

  public Client(int id, String mc_addr, int mc_port, String mdb_addr, int mdb_port, String mdr_addr, int mdr_port,ReplicationControl control,Long size,ExecutorService executor,MyFilesLog myfiles, int whynot) throws UnknownHostException, InterruptedException, IOException {
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.mdb_addr = mdb_addr;
    this.mdb_port = mdb_port;
    this.mdr_addr = mdr_addr;
    this.mdr_port = mdr_port;
    this.id = id;
    this.size = size;
    this.control = control;
    this.executor = executor;
    this.myfiles = myfiles;

    this.mcaddr = InetAddress.getByName(this.mc_addr);
    this.mcsocket = new MulticastSocket(this.mc_port);
    this.mcsocket.setTimeToLive(1);
    this.mdbaddr = InetAddress.getByName(this.mdb_addr);
    this.mdbsocket = new MulticastSocket(this.mdb_port);
    this.mdbsocket.setTimeToLive(1);
    this.mdraddr = InetAddress.getByName(this.mdr_addr);
    this.mdrsocket = new MulticastSocket(this.mdr_port);
    this.mdrsocket.setTimeToLive(1);
  }

  public Client(int id, String mc_addr, int mc_port, String mdb_addr, int mdb_port, String mdr_addr, int mdr_port, ReplicationControl control, Long size,ExecutorService executor,MyFilesLog myfiles) throws UnknownHostException, InterruptedException, IOException{
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.mdb_addr = mdb_addr;
    this.mdb_port = mdb_port;
    this.mdr_addr = mdr_addr;
    this.mdr_port = mdr_port;
    this.id = id;
    this.control = control;
    this.size = size;
    this.executor = executor;
    this.myfiles = myfiles;

    this.mcaddr = InetAddress.getByName(this.mc_addr);
    this.mcsocket = new MulticastSocket(this.mc_port);
    this.mcsocket.setTimeToLive(1);
    this.mdbaddr = InetAddress.getByName(this.mdb_addr);
    this.mdbsocket = new MulticastSocket(this.mdb_port);
    this.mdbsocket.setTimeToLive(1);
    this.mdraddr = InetAddress.getByName(this.mdr_addr);
    this.mdrsocket = new MulticastSocket(this.mdr_port);
    this.mdrsocket.setTimeToLive(1);

    System.out.println("Peer ID : " + id);
    try{
      Client obj = new Client(id, mc_addr, mc_port, mdb_addr, mdb_port, mdr_addr, mdr_port, control, size, executor,myfiles,0);
      RMI_Interface stub = (RMI_Interface) UnicastRemoteObject.exportObject(obj, 0);

      Registry registry = LocateRegistry.getRegistry();
      registry.rebind("RMI_Interface" + id, stub);
    }catch(Exception e){
      System.err.println("RMI not Running!");
    }
  }

  public void processDelete(String filename, int id) throws IOException, InterruptedException{
    /*String fileId = control.getFileIdByFilename(filename);
    Message message = new Message(Message.MsgType.DELETE, id);
    message.setFileID(fileId);
    byte [] msg = message.createDeleteMessage("1.0");
    int i = 0;
    while(i<3){
      sendMCMessage(msg);
      Thread.sleep(10);
      i++;
    }
    control.deleteAllEntries(fileId);*/
  }

  public void sendMCMessage(byte[] bytes) throws IOException{
    DatagramPacket packet = new DatagramPacket(bytes,bytes.length,mcaddr,mc_port);
    mcsocket.send(packet);
  }

  public static HashMap<String, Long> listFiles(File[] listOfFiles){
    HashMap<String, Long> file_map = new HashMap<String, Long>();

    for(int i = 0; i < listOfFiles.length; i++){
      if(listOfFiles[i].isFile()){
        long len = listOfFiles[i].length();
        String name = listOfFiles[i].getName();
        file_map.put(name, len);
      }
    }
    return file_map;
  }

  public static long calculateTotalSpace(HashMap<String, Long> map){
    Set set = map.entrySet();
    Iterator iterator = set.iterator();
    long len_sum = 0;

    while(iterator.hasNext()) {
        Map.Entry mentry  = (Map.Entry)iterator.next();
        len_sum = len_sum + (long) mentry.getValue();
      }

      return len_sum;
  }

  public void reclaimStorage(String path, long target_space) throws IOException{
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    ArrayList<File> filenames = new ArrayList<File>();
    for(int i=0; i < listOfFiles.length; i++){
      if(!listOfFiles[i].getName().equals("logfile.txt"))
        filenames.add(listOfFiles[i]);
    }
    HashMap<String, Long> map = listFiles(listOfFiles);
    long space = calculateTotalSpace(map);

    while(space > target_space){
      String filename = filenames.get(0).getName();
      File file = new File(path+"/"+filename);
      file.delete();
      filenames.remove(0);
      space -= map.get(filename);
      map.remove(filename);

      Message message = new Message(Message.MsgType.REMOVED, id);
      String fileId = filename.split("_")[0];
      int chunkNo = Integer.parseInt(filename.split("_")[1]);
      message.setFileID(fileId);
      message.setVersion("1.0");
      byte[] msg = message.createRemovedMessage(chunkNo);
      sendMCMessage(msg);
    }
    this.size = new Long(target_space);
  }
  //(String fileid, String filename, int number_of_chunks, int id, MulticastSocket mcsocket, InetAddress mc_inetAddr, int mc_port)
  public void processRestore(String arg1) throws IOException{
    //arg1 = file name
  /*  String fileid = control.getFileIdByFilename(arg1);
    int number_of_chunks = control.getNumberOfChunks(arg1);

    Runnable task = new RestoreTask(fileid, arg1, number_of_chunks, this.id, this.mcsocket, this.mcaddr, this.mc_port);
    executor.execute(task);*/
  }

}
