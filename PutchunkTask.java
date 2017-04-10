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

class PutchunkTask implements Runnable
{
    private byte[] body;
    private String fileId;
    private int chunkNo;
    private int replicationDeg;
    private int serverid;
    private ReplicationControl control;
    private MulticastSocket socket;
    private InetAddress addr;
    private int port;

    public PutchunkTask(byte[] body, String fileId, int chunkNo, int replicationDeg, int serverid, MulticastSocket socket, InetAddress addr, int port,ReplicationControl control){
        this.body = body;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.serverid = serverid;
        this.socket = socket;
        this.port = port;
        this.addr = addr;
        this.control = control;
    }

    public void sendMDBMessage() throws IOException, InterruptedException{
        Message msg = new Message(Message.MsgType.PUTCHUNK, serverid);
        msg.setFileID(fileId);
        msg.setVersion("1.0");
        byte[] full_msg = msg.createMessage(body,chunkNo,replicationDeg);
        DatagramPacket packet = new DatagramPacket(full_msg,full_msg.length,this.addr,this.port);
        socket.send(packet);

        boolean continues = true;
        int tries = 0;
        String chunkname = new String(fileId+"_"+chunkNo);
        int time = 1000;
        while(continues && tries < 5){
          Thread.sleep(time);
          if(this.control.getAtualRepDeg(chunkname) < this.replicationDeg)
            socket.send(packet);
          else
            continues = false;
          tries++;
          time  = time *2;
        }
    }

    @Override
    public void run()
    {
        try
        {
          sendMDBMessage();
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
    }
}
