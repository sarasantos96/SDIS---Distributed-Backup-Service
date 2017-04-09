import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.util.regex.*;

public class Message{
  public final char[] CRLF = {0xD,0xA,0xD,0xA};
  public enum MsgType{PUTCHUNK, RESTORE,STORED,DELETE,REMOVED};
  private MsgType messageType;
  private String version;
  private int senderid;
  private String fileId;
  private int chunkNo;
  private int replicationDeg;
  private byte[] body;

  public Message(MsgType msgtype, int senderID){
    this.messageType = msgtype;
    this.senderid = senderID;
  }

  //<MessageType> <Version> <SenderID> <FileId> <ChunkNo> <RepDeg> <CRLF><CRLF> <Body>
  public Message(byte[] msgdata){
    String crlf = new String(CRLF);
    List<byte[]> headerNBody = split(crlf.getBytes(),msgdata);

    //Extracts Header Info
    String header = new String(headerNBody.get(0));
    Pattern pattern = Pattern.compile("\\s+");
    String[] split_header = pattern.split(header);

    if(split_header[0].equals("PUTCHUNK")){
      this.messageType = MsgType.PUTCHUNK;
    }else if(split_header[0].equals("STORED")){
      this.messageType = MsgType.STORED;
    }else if(split_header[0].equals("DELETE")){
      this.messageType = MsgType.DELETE;
    }else if(split_header[0].equals("REMOVED")){
      this.messageType = MsgType.REMOVED;
    }
    this.version = split_header[1];
    this.senderid = Integer.parseInt(split_header[2]);
    this.fileId = split_header[3];
    if(this.messageType != MsgType.DELETE)
      this.chunkNo = Integer.parseInt(split_header[4]);


    if(headerNBody.size() > 1){
      this.body = headerNBody.get(1);
    }
  }

/* Code to split byte[] when a pattern occurs
Source: http://stackoverflow.com/questions/22519346/how-to-split-a-byte-array-around-a-byte-sequence-in-java
*/
  public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
    for(int i=0; i< pattern.length; i++) {
        if(pattern[i] != input[pos+i]) {
            return false;
        }
    }
    return true;
}
public static List<byte[]> split(byte[] pattern, byte[] input) {
    List<byte[]> l = new LinkedList<byte[]>();
    int blockStart = 0;
    for(int i=0; i<input.length; i++) {
       if(isMatch(pattern,input,i)) {
          l.add(Arrays.copyOfRange(input, blockStart, i));
          blockStart = i+pattern.length;
          i = blockStart;
       }
    }
    l.add(Arrays.copyOfRange(input, blockStart, input.length ));
    return l;
}


  public MsgType getMessageType(){
    return this.messageType;
  }
  public String getVersion(){
    return this.version;
  }
  public int getsenderid(){
    return this.senderid;
  }
  public String getFileId(){
    return this.fileId;
  }
  public int getChunkNo(){
    return this.chunkNo;
  }
  public int getReplicationDeg(){
    return this.replicationDeg;
  }
  public byte[] getBody(){
    return this.body;
  }

  public void setFileID(String fileID){
    this.fileId = fileID;
  }

  public void setVersion(String version){
    this.version = version;
  }

  public byte[] createMessage(byte[] body,int chunckNo){
    String crlf = new String(CRLF);
    //Chooses correct header for message type
    String header_type = new String("Error");
    if(this.messageType == MsgType.PUTCHUNK){
      header_type = new String("PUTCHUNK");
    }else if(this.messageType == MsgType.RESTORE){
      header_type = new String("RESTORE");
    }else{
      System.out.println("HEADER Error");
      System.exit(1);
    }
    //Builds Header
    String headermessage = new String(header_type +" "+version+" "+senderid+" "+fileId+" "+chunckNo + crlf);
    byte[] header = headermessage.getBytes();
    byte[] full_msg = new byte[header.length + body.length];
    if(body.length != 0){
      System.arraycopy(header,0,full_msg,0,header.length);
      System.arraycopy(body,0, full_msg, header.length, body.length);
    }else{
      System.arraycopy(header,0,full_msg,0,header.length);
    }

    return full_msg;
  }

  public byte[] createStoredMessage(int chunckNo){
      String crlf = new String(CRLF);
      String msg = new String("STORED "+this.version + " "+this.senderid +" "+ this.fileId + " "+chunckNo + crlf);
      return msg.getBytes();
  }

  public byte[] createDeleteMessage(String version){
    String crlf = new String(CRLF);
    String msg = new String("DELETE "+version+" "+ this.senderid +" "+ this.fileId + crlf);
    return msg.getBytes();
  }

  public byte[] createRemovedMessage(int chunkNo){
    String crlf = new String(CRLF);
    String msg = new String("REMOVED "+this.version + " "+this.senderid +" "+ this.fileId + " "+chunkNo + crlf);
    return msg.getBytes();
  }

}
