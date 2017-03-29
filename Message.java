import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;

public class Message{
  private String messageType;
  private int version;
  private int senderid;
  private String fileId;
  private int chunkNo;
  private int replicationDeg;
  private byte[] body;

  public Message(byte[] msgdata){
    int i = msgdata.length;
    while (i-- > 0 && msgdata[i] == '\00') {}

    this.body = new byte[i+1];
    System.arraycopy(msgdata, 0,this.body, 0, i+1);
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
