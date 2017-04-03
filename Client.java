import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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

  public int saySomething() {
    System.out.println("Something!!");
    return 0;
  }

  public int rmiRequest(String type, String message) throws IOException {
    switch(type){
      case "Backup":
        sendMDBMessage(message, id);
        break;
      case "Restore":
        sendMDRMessage(message, id);
        break;
      case "Reclaim":
        String current_path = new java.io.File( "." ).getCanonicalPath();
        boolean isNumeric = message.matches("-?\\d+(\\.\\d+)?");
        if(isNumeric)
          reclaimStorage(current_path + "/Peer" + id, Integer.parseInt(message));
        else
          return -1;
        break;
      default:
        sendMCMessage(message, id);
    }
    return 0;
  }

  public Client() {}

  public Client(int id, String mc_addr, int mc_port, String mdb_addr, int mdb_port, String mdr_addr, int mdr_port, int whynot) throws UnknownHostException, InterruptedException, IOException {
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.mdb_addr = mdb_addr;
    this.mdb_port = mdb_port;
    this.mdr_addr = mdr_addr;
    this.mdr_port = mdr_port;
    this.id = id;

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

  public Client(int id, String mc_addr, int mc_port, String mdb_addr, int mdb_port, String mdr_addr, int mdr_port) throws UnknownHostException, InterruptedException, IOException{
    this.mc_addr = mc_addr;
    this.mc_port = mc_port;
    this.mdb_addr = mdb_addr;
    this.mdb_port = mdb_port;
    this.mdr_addr = mdr_addr;
    this.mdr_port = mdr_port;
    this.id = id;

    this.mcaddr = InetAddress.getByName(this.mc_addr);
    this.mcsocket = new MulticastSocket(this.mc_port);
    this.mcsocket.setTimeToLive(1);
    this.mdbaddr = InetAddress.getByName(this.mdb_addr);
    this.mdbsocket = new MulticastSocket(this.mdb_port);
    this.mdbsocket.setTimeToLive(1);
    this.mdraddr = InetAddress.getByName(this.mdr_addr);
    this.mdrsocket = new MulticastSocket(this.mdr_port);
    this.mdrsocket.setTimeToLive(1);

    System.out.println("Client ID : " + id);
    try{
      Client obj = new Client(id, mc_addr, mc_port, mdb_addr, mdb_port, mdr_addr, mdr_port, 0);
      RMI_Interface stub = (RMI_Interface) UnicastRemoteObject.exportObject(obj, 0);

      Registry registry = LocateRegistry.getRegistry();
      registry.rebind("RMI_Interface" + id, stub);
    }catch(Exception e){
      System.err.println("RMI NOT RUNNING EXCEPTION!!!");
    }
  }

  public void sendMulticastMessage(int senderid) throws IOException{
    System.out.println("Write something: ");
    String input = System.console().readLine();

    String[]  inputType = input.split(" ");
    if(inputType[0].equals("backup")){
      sendMDBMessage("backup", senderid);
    }else if(inputType[0].equals("restore")){
      sendMDRMessage("restore",senderid);
    }else{
      sendMCMessage(inputType[0],senderid);
    }
  }

  public void sendMCMessage(String message, int senderid) throws IOException{
    String mcmessage = new String(message +":"+ senderid);
    DatagramPacket packet = new DatagramPacket(mcmessage.getBytes(),mcmessage.length(),mcaddr,mc_port);
    mcsocket.send(packet);
  }

  public void sendMDBMessage(String message, int senderid) throws IOException{
    File input_file = new File("icon_test.png");
    FileInputStream file_input_stream = new FileInputStream(input_file);
    int n_bytes = (int) input_file.length();
    byte[] file_bytes = new byte[n_bytes];
    int read = file_input_stream.read(file_bytes,0,n_bytes);
    file_input_stream.close();

    Message msg = new Message(Message.MsgType.PUTCHUNK, senderid);
    byte[] full_msg = msg.createMessage(file_bytes);

    DatagramPacket packet = new DatagramPacket(full_msg,full_msg.length,mdbaddr,mdb_port);
    mdbsocket.send(packet);
  }

  //TODO: código repetido (Server.java)
  public void saveChunck(byte[] receive_bytes,int server_id) throws FileNotFoundException, IOException{
    String directory = new String("Peer"+server_id+"/restore_test.txt");
    FileOutputStream fos = new FileOutputStream(directory);
    fos.write(receive_bytes);
    fos.close();
  }

  public void sendMDRMessage(String message, int senderid)throws IOException, FileNotFoundException{
    Message msg = new Message(Message.MsgType.RESTORE,senderid);
    byte[] body = new byte[0];
    byte[] full_msg = msg.createMessage(body);

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

}
