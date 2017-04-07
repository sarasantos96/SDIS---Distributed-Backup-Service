import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.util.regex.*;
import java.util.Arrays;

public class RestoreMessage{
  public final char[] CRLF = {0xD,0xA,0xD,0xA};
  public enum MsgType{CHUNK};
  private MsgType messageType;
  private int version;
  private int senderid;
  private String fileId;
  private int chunkNo;
  private byte[] body;

  public RestoreMessage(MsgType msgtype, int version, int senderID, String fileId, int chunkNo){
    this.messageType = msgtype;
    this.version = version;
    this.senderid = senderID;
    this.fileId = fileId;
    this.chunkNo = chunkNo;
  }

  //<MessageType> <Version> <SenderID> <FileId> <ChunkNo> <CRLF><CRLF> <Body>
  public RestoreMessage(byte[] msgdata){
    String crlf = new String(CRLF);
    List<byte[]> headerNBody = split(crlf.getBytes(),msgdata);

    //Extracts Header Info
    String header = new String(headerNBody.get(0));
    Pattern pattern = Pattern.compile("\\s+");
    String[] split_header = pattern.split(header);

    if(split_header[0].equals("CHUNK"))
      this.messageType = MsgType.CHUNK;
    else{
      System.out.println("Wrong header");
      System.exit(1);
    }

    this.version = Integer.parseInt(split_header[1]);
    this.senderid = Integer.parseInt(split_header[2]);
    this.fileId = split_header[3];
    this.chunkNo = Integer.parseInt(split_header[4]);


    //Deletes all NULL positions from the received data and saves the content in the body
    byte[] rawbody = headerNBody.get(1);
    int i = rawbody.length;
    while (i-- > 0 && rawbody[i] == '\00') {}
    this.body = new byte[i+1];
    System.arraycopy(rawbody, 0,this.body, 0, i+1);
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
  public int getVersion(){
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
  public byte[] getBody(){
    return this.body;
  }

  public void setFileID(String fileID){
    this.fileId = fileID;
  }

//<MessageType> <Version> <SenderID> <FileId> <ChunkNo> <CRLF><CRLF> <Body>
  public byte[] createMessage(byte[] body){
    String crlf = new String(CRLF);
    //Chooses correct header for message type
    String header_type = new String("Error");
    if(this.messageType == MsgType.CHUNK){
      header_type = new String("CHUNK");
    }else{
      System.out.println("HEADER Error");
      System.exit(1);
    }
    //Builds Header
    String headermessage = new String(header_type +" "+ this.version + " " +this.senderid+" "+this.fileId+" "+this.chunkNo + crlf);
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

  public void print(){
    if(this.messageType == MsgType.CHUNK)
      System.out.println("Type: CHUNK");
    System.out.println("version: " + this.version);
    System.out.println("senderid: " + this.senderid);
    System.out.println("fileid: " + this.fileId);
    System.out.println("chunkNo: " + this.chunkNo);
    System.out.println("body: " + new String(this.body));
  }

}
