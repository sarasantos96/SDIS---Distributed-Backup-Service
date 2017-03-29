import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;

public class Message{
  private final char[] CRLF = {0xD,0xA,0xD,0xA};
  private String messageType;
  private int version;
  private int senderid;
  private String fileId;
  private int chunkNo;
  private int replicationDeg;
  private byte[] body;

  public Message(byte[] msgdata){
    String crlf = new String(CRLF);
    List<byte[]> headerNBody = split(crlf.getBytes(),msgdata);

    /*String rawmsg = new String(headerNBody.get(0));
    String[] msg_split = rawmsg.split(":");
    this.messageType = msg_split[0];
    this.senderid = Integer.parseInt(msg_split[1]);*/

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

  public String getMessageType(){
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
  public int getReplicationDeg(){
    return this.replicationDeg;
  }
  public byte[] getBody(){
    return this.body;
  }

}
