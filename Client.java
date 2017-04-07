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


public class Client implements RMI_Interface{
  public static final int CHUNCK_SIZE = 64000;

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

  public int rmiRequest(String type, String arg1, String arg2) throws IOException {
    switch(type){
      case "Backup":
        processBackup(arg1,Integer.parseInt(arg2),id);
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
    }
    return 0;
  }

  public Client() {}

  public Client(int id, String mc_addr, int mc_port, String mdb_addr, int mdb_port, String mdr_addr, int mdr_port,ReplicationControl control, int whynot) throws UnknownHostException, InterruptedException, IOException {
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.mdb_addr = mdb_addr;
    this.mdb_port = mdb_port;
    this.mdr_addr = mdr_addr;
    this.mdr_port = mdr_port;
    this.id = id;
    this.control = control;

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

  public Client(int id, String mc_addr, int mc_port, String mdb_addr, int mdb_port, String mdr_addr, int mdr_port, ReplicationControl control) throws UnknownHostException, InterruptedException, IOException{
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.mdb_addr = mdb_addr;
    this.mdb_port = mdb_port;
    this.mdr_addr = mdr_addr;
    this.mdr_port = mdr_port;
    this.id = id;
    this.control = control;

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
      Client obj = new Client(id, mc_addr, mc_port, mdb_addr, mdb_port, mdr_addr, mdr_port, control, 0);
      RMI_Interface stub = (RMI_Interface) UnicastRemoteObject.exportObject(obj, 0);

      Registry registry = LocateRegistry.getRegistry();
      registry.rebind("RMI_Interface" + id, stub);
    }catch(Exception e){
      System.err.println("RMI not Running!");
    }
  }

  public void processBackup(String filename,int replicationDeg,int id){
		File input_file = new File(filename);
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
        this.control.addNewLog(filename,chunkname,replicationDeg,0);
				sendMDBMessage(byte_chunk, id, fileId,n_chunks);

        boolean continues = true;
        int tries = 0;
        while(continues && tries < 5){
          Thread.sleep(1000);
          if(this.control.getAtualRepDeg(chunkname) < this.control.getRepDeg(chunkname))
            sendMDBMessage(byte_chunk, id, fileId,n_chunks);
          else
            continues = false;

          tries++;
        }
				byte_chunk = null;
			}
			file_input_stream.close();
		}catch(Exception e){
			System.out.println(e.getClass().getSimpleName());
			e.printStackTrace(new PrintStream(System.out));
		}
  }

  public void sendMCMessage(byte[] bytes) throws IOException{
    DatagramPacket packet = new DatagramPacket(bytes,bytes.length,mcaddr,mc_port);
    mcsocket.send(packet);
  }

  public void sendMDBMessage(byte[] body, int senderid, String filename, int chunkNo) throws IOException{

      Message msg = new Message(Message.MsgType.PUTCHUNK, senderid);
      msg.setFileID(filename);
      byte[] full_msg = msg.createMessage(body,chunkNo);

      DatagramPacket packet = new DatagramPacket(full_msg,full_msg.length,mdbaddr,mdb_port);
      mdbsocket.send(packet);
  }

  public void sendMDRMessage(String message, int senderid)throws IOException, FileNotFoundException{
    Message msg = new Message(Message.MsgType.RESTORE,senderid);
    byte[] body = new byte[0];
    byte[] full_msg = msg.createMessage(body,1); //!!!!

    DatagramPacket packet = new DatagramPacket(full_msg, full_msg.length,mdraddr,mdr_port);
    mdrsocket.send(packet);

    //Waits to receive and saves file
  /*  System.out.println("Waiting file");
    byte[] buf = new byte[254];
    packet = new DatagramPacket(buf, buf.length);
    mdrsocket.receive(packet);
    //TODO: código repetido (Message.java)
    byte[] rawbody = packet.getData();
    int i = rawbody.length;
    while (i-- > 0 && rawbody[i] == '\00') {}
    byte[] receive_body = new byte[i+1];
    System.arraycopy(rawbody, 0,receive_body, 0, i+1);
    saveChunck(body,senderid);
    System.out.println("Ficheiro guardado");*/
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

  public static void reclaimStorage(String path, long target_space){
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();

    HashMap<String, Long> map = listFiles(listOfFiles);
    long space = calculateTotalSpace(map);

    while(space > target_space){
      listOfFiles[0].delete();
      System.out.println("Deleted file " + listOfFiles[0].getName());
      listOfFiles = folder.listFiles();
      map = listFiles(listOfFiles);
      space = calculateTotalSpace(map);
    }
  }

  public void processRestore(String arg1) throws IOException{
    //arg1 = file name
    String fileid = "FEB0FD69BDC3F7914F18C8C6FE379B8A0C01F0904DFA12F5DA93953DDEA483B4";
    int chunk_number = 1;
    int version = 1;
    RestoreControlMessage cm = new RestoreControlMessage(RestoreControlMessage.MsgType.GETCHUNK, version, this.id, fileid, 1);
    byte[] bytes = cm.createMessage();
    sendMCMessage(bytes);
  }

}
