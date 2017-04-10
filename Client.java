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
  private StoredControl storedcontrol;

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
        if(isNumeric){
          Runnable reclaimtask = new ReclaimTask(current_path + "/Peer" + id, Integer.parseInt(arg1),storedcontrol,id,size,mcsocket,mcaddr, mc_port);
          executor.execute(reclaimtask);
        }
        else
          return -1;
        break;
      /*default:
        sendMCMessage(arg1, id);*/
      case "Delete":
        processDelete(arg1,id);
        break;
      case "State":
        processState();
    }
    return 0;
  }

  public Client() {}

  public Client(int id, String mc_addr, int mc_port, String mdb_addr, int mdb_port, String mdr_addr, int mdr_port, ReplicationControl control, Long size,ExecutorService executor,MyFilesLog myfiles,StoredControl storedcontrol) throws UnknownHostException, InterruptedException, IOException{
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
    this.storedcontrol = storedcontrol;

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
      RMI_Interface stub = (RMI_Interface) UnicastRemoteObject.exportObject(this, 0);

      Registry registry = LocateRegistry.getRegistry();
      registry.rebind("RMI_Interface" + id, stub);
    }catch(Exception e){
      System.err.println("RMI not Running!");
    }
  }

  public void processDelete(String filename, int id) throws IOException, InterruptedException{
    String fileId = myfiles.getFileIdByFilename(filename);
    Message message = new Message(Message.MsgType.DELETE, id);
    message.setFileID(fileId);
    byte [] msg = message.createDeleteMessage("1.0");
    int i = 0;
    while(i<3){
      sendMCMessage(msg);
      Thread.sleep(10);
      i++;
    }
    control.deleteAllEntries(fileId);
    myfiles.deleteAllEntries(fileId);
  }

  public void sendMCMessage(byte[] bytes) throws IOException{
    DatagramPacket packet = new DatagramPacket(bytes,bytes.length,mcaddr,mc_port);
    mcsocket.send(packet);
  }

  //(String fileid, String filename, int number_of_chunks, int id, MulticastSocket mcsocket, InetAddress mc_inetAddr, int mc_port)
  public void processRestore(String arg1) throws IOException{
    //arg1 = file name
  /*  String fileid = control.getFileIdByFilename(arg1);
    int number_of_chunks = control.getNumberOfChunks(arg1);

    Runnable task = new RestoreTask(fileid, arg1, number_of_chunks, this.id, this.mcsocket, this.mcaddr, this.mc_port);
    executor.execute(task);*/
  }

  public void processState(){

    HashMap<String,MyFilesLog.Value> files = this.myfiles.getHMap();
    HashMap<String,StoredControl.Value> chunks = this.storedcontrol.getHMap();

    System.out.println("");
    System.out.println("         ************************************");
    System.out.println("                    BACKED UP FILES          ");
    System.out.println("         ************************************");
    System.out.println("");

    if(files.isEmpty()){
      System.out.println("No files have been backed up by this Peer");
    }


    Iterator it = files.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        String key = (String) pair.getKey();
        System.out.println("FileId: " + key);
        MyFilesLog.Value value = (MyFilesLog.Value) pair.getValue();
        System.out.println("Filename: " + value.filename);
        System.out.println("N_Chunks: " + value.nChunks);
        System.out.println("Target RepDegree:" + value.replicationDeg);

        for(int i = 0; i < value.nChunks; i++){
          System.out.println("    Chunk Number: " + (i + 1));
          int repdeg = control.getAtualRepDeg(key + "_" + (i + 1));
          System.out.println("    RepDegree: " + repdeg);
        }
        System.out.println("--------------------------------------------");
        it.remove(); // avoids a ConcurrentModificationException
    }

    System.out.println("");
    System.out.println("         ************************************");
    System.out.println("                      SAVED CHUNKS           ");
    System.out.println("         ************************************");
    System.out.println("");

    if(chunks.isEmpty()){
      System.out.println("No chunks have been stored in this Peer");
    }

    Iterator it_chunks = chunks.entrySet().iterator();
    while (it_chunks.hasNext()) {
        Map.Entry pair = (Map.Entry)it_chunks.next();
        String key = (String) pair.getKey();
        System.out.println("ChunkId: " + key);
        StoredControl.Value value = (StoredControl.Value) pair.getValue();
        File chunk_file = new File("Peer" + this.id + "/" + key);
        System.out.println("Size: " + chunk_file.length());
        int rep = storedcontrol.getAtualRepDeg(key);
        System.out.println("RepDegree: " + rep);

        System.out.println("--------------------------------------------");
        it_chunks.remove(); // avoids a ConcurrentModificationException
    }

  System.out.println("");
  }



}
