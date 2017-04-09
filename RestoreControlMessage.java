import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Arrays;
import java.nio.*;
import java.util.regex.*;

public class RestoreControlMessage{
	public final char[] CRLF = {0xD,0xA,0xD,0xA};
	public enum MsgType{GETCHUNK};
	private MsgType messageType;
  private String version;
  private int senderid;
  private String fileid;
  private int chunkNo;

	//GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
  public RestoreControlMessage(MsgType msgtype, String version, int senderID, String fileID, int chunkNo){
  	this.messageType = msgtype;
  	this.version = version;
  	this.senderid = senderID;
  	this.fileid = fileID;
  	this.chunkNo = chunkNo;
 	}

 	public RestoreControlMessage(byte[] msgdata){
	 	String crlf = new String(CRLF);
	  List<byte[]> headerNBody = split(crlf.getBytes(),msgdata);

	  String header = new String(headerNBody.get(0));
    Pattern pattern = Pattern.compile("\\s+");
    String[] split_header = pattern.split(header);

    if(split_header[0].equals("GETCHUNK"))
      this.messageType = MsgType.GETCHUNK;
    else{
    	System.out.println("Wrong header");
     	System.exit(1);
     }

    this.version = new String(split_header[1]);
    this.senderid = Integer.parseInt(split_header[2]);
    this.fileid = new String(split_header[3]);
    this.chunkNo = Integer.parseInt(split_header[4]);

	}

	public byte[] createMessage(){
		String crlf = new String(CRLF);
		String header_type = new String("");
		if(this.messageType == MsgType.GETCHUNK)
			header_type = new String("GETCHUNK");
		else{
			System.out.println("Control Message Error");
	    System.exit(1);
		}

		String headermessage = new String(header_type + " " + this.version  + " " +  this.senderid + " " +  this.fileid + " " + this.chunkNo + crlf);
		byte[] messagebytes = headermessage.getBytes();

		return messagebytes;
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

	public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
    for(int i=0; i< pattern.length; i++) {
        if(pattern[i] != input[pos+i]) {
            return false;
        }
    }
    return true;
	}

	public void print(){
		if(this.messageType == MsgType.GETCHUNK)
			System.out.println("Type: GETCHUNK");
		System.out.println("version: " + this.version);
		System.out.println("senderid: " + this.senderid);
		System.out.println("fileid: " + this.fileid);
		System.out.println("chunkNo: " + this.chunkNo);
	}

  public int getSenderId(){
    return this.senderid;
  }

  public String getFileId(){
    return this.fileid;
  }

  public int getChunkNo(){
    return this.chunkNo;
  }
}