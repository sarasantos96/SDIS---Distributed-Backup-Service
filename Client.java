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

  public int rmiRequest(String type, String message) throws IOException {
    switch(type){
      case "Backup":
        processBackup(message,id);
        break;
      case "Restore":
        sendMDRMessage(message, id);
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
      registry.bind("RMI_Interface" + id, stub);
    }catch(Exception e){
      System.err.println("Server exception: " + e.toString());
      e.printStackTrace();
    }
  }

  public void processBackup(String message,int id){
    //Message terá depois tmb o repDeg
    String filepath = message;
		File input_file = new File(filepath);

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
        String fileId = createHashedName(filepath)+"_part_"+n_chunks;
				sendMDBMessage(byte_chunk, id, fileId);
				byte_chunk = null;
			}
			file_input_stream.close();
		}catch(Exception e){
			System.out.println(e.getClass().getSimpleName());
			e.printStackTrace(new PrintStream(System.out));
		}
  }

  /*public void sendMulticastMessage(int senderid) throws IOException{
    System.out.println("Write something: ");
    String input = System.console().readLine();

    String[]  inputType = input.split(" ");
    if(inputType[0].equals("backup")){
      sendMDBMessage("backup", senderid, inputType[1]);
    }else if(inputType[0].equals("restore")){
      sendMDRMessage("restore",senderid);
    }else{
      sendMCMessage(inputType[0],senderid);
    }
  }*/

  public void sendMCMessage(String message, int senderid) throws IOException{
    String mcmessage = new String(message +":"+ senderid);
    DatagramPacket packet = new DatagramPacket(mcmessage.getBytes(),mcmessage.length(),mcaddr,mc_port);
    mcsocket.send(packet);
  }

  public void sendMDBMessage(byte[] body, int senderid, String filename) throws IOException{

      Message msg = new Message(Message.MsgType.PUTCHUNK, senderid);
      msg.setFileID(filename);
      System.out.println(msg.getFileId());
      byte[] full_msg = msg.createMessage(body);

      DatagramPacket packet = new DatagramPacket(full_msg,full_msg.length,mdbaddr,mdb_port);
      mdbsocket.send(packet);
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

  public static String hash(String text){
    try{
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
      String s = DatatypeConverter.printHexBinary(hash);
      //System.out.println(s);
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
    //System.out.println(string_to_hash);
    return string_to_hash;
  }
  public static String createHashedName(String name) throws IOException{
    String s = createName(name);
    s = hash(s);
    return s;
  }

}
