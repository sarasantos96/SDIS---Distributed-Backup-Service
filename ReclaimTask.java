import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Date;

class ReclaimTask implements Runnable{
    private String path;
    private long target_space;
    private StoredControl storedcontrol;
    private MulticastSocket mcsocket;
    private InetAddress mcaddr;
    private int mc_port;
    private int id;
    private Long size;

    public ReclaimTask(String path, long target_space, StoredControl storedcontrol,int id,Long size, MulticastSocket mcsocket, InetAddress mcaddr, int mc_port){
      this.path = path;
      this.storedcontrol = storedcontrol;
      this.id = id;
      this.size = size;
      this.mcsocket = mcsocket;
      this.mcaddr = mcaddr;
      this.mc_port = mc_port;
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

    public void sendMCMessage(byte[] bytes) throws IOException{
      DatagramPacket packet = new DatagramPacket(bytes,bytes.length,mcaddr,mc_port);
      mcsocket.send(packet);
    }

    public void reclaimStorage() throws IOException{
      File folder = new File(path);
      File[] listOfFiles = folder.listFiles();
      ArrayList<File> filenames = new ArrayList<File>();
      for(int i=0; i < listOfFiles.length; i++){
        if(!listOfFiles[i].getName().equals("logfile.txt") && !listOfFiles[i].getName().equals("myfileslog.txt") && !listOfFiles[i].getName().equals("storedlog.txt"))
          filenames.add(listOfFiles[i]);
      }
      HashMap<String, Long> map = listFiles(listOfFiles);
      long space = calculateTotalSpace(map);

      while(space > target_space && filenames.size()>0){
        String filename = filenames.get(0).getName();
        File file = new File(path+"/"+filename);
        file.delete();
        filenames.remove(0);
        space -= map.get(filename);
        map.remove(filename);
        Message message = new Message(Message.MsgType.REMOVED, id);
        String fileId = filename.split("_")[0];
        int chunkNo = Integer.parseInt(filename.split("_")[1]);
        message.setFileID(fileId);
        message.setVersion("1.0");
        byte[] msg = message.createRemovedMessage(chunkNo);
        sendMCMessage(msg);
        this.storedcontrol.deleteAllEntries(filename);
      }
      this.size = new Long(target_space);
    }

    @Override
    public void run()
    {
        try
        {
          reclaimStorage();
        }
        catch(Exception e)
        {
          System.err.println("Server exception: " + e.toString());
          e.printStackTrace();
        }
    }
}
