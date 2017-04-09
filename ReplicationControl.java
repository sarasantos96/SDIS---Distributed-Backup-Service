import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;


public class ReplicationControl{
  private String logFileName;
  private HashMap<Key,Integer> hmap;

  public ReplicationControl(String logFileName) throws IOException{
    this.hmap = new HashMap<Key, Integer>();
    this.logFileName = logFileName;
    File logFile = new File(logFileName);
    if(!logFile.exists())
      logFile.createNewFile();
    else
      loadHmap();

  }

  public class Key{
    public String chunkname;
    public int senderid;

    public Key(String chunkname, int senderid){
      this.chunkname = chunkname;
      this.senderid = senderid;
    }
  }

  public void addNewLog(String chunkname, int senderid,int chunkNo) throws IOException{
    Key key  = new Key(chunkname, senderid);
    this.hmap.put(key,chunkNo);
    this.saveHmap();
  }

  public void loadHmap() throws IOException{
    File logfile = new File(this.logFileName);

		try {
			FileReader fileReader = new FileReader(logfile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String [] split = line.split(":");
        if(split.length == 3){
          String chunkname = split[0];
          int senderid = Integer.parseInt(split[1]);
          int chunkNo = Integer.parseInt(split[2]);
          this.addNewLog(chunkname,senderid,chunkNo);
        }
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }

  public void saveHmap() throws IOException{
    String content = new String("");
    for(Map.Entry<Key, Integer> entry: this.hmap.entrySet()) {
        String chunkname = entry.getKey().chunkname;
        int senderid = entry.getKey().senderid;
        int chunkNo = entry.getValue();
        content += chunkname+":"+senderid+":"+chunkNo+"\n";

    }
    byte data[] = content.trim().getBytes();
    FileOutputStream out = new FileOutputStream(this.logFileName);
    out.write(data);
    out.close();
  }

  public int getAtualRepDeg(String chunkname){
    int rep = 0;
    for(Map.Entry<Key, Integer> entry: this.hmap.entrySet()) {
        String name = entry.getKey().chunkname;
        if(name.equals(chunkname))
          rep++;
    }
    return rep;
  }

  public boolean isChunkOwner(String chunkname){
    for(Map.Entry<Key, Integer> entry: this.hmap.entrySet()) {
        String name = entry.getKey().chunkname;
        if(name.equals(chunkname))
          return true;
    }
    return false;
  }

  public void deleteAllEntries(String fileId) throws IOException{

    for(Iterator<Map.Entry<Key, Integer>> i = this.hmap.entrySet().iterator(); i.hasNext(); ){
      Map.Entry<Key, Integer> entry = i.next();
      String chunkname = entry.getKey().chunkname;
      String entryfileId = chunkname.split("_")[0];

      if(entryfileId.equals(fileId)){
        i.remove();
      }
    }
    saveHmap();
  }

/*  public String getFileIdByFilename(String filename){
    for(Map.Entry<Key, Value> entry: this.hmap.entrySet()) {
        String current_filename = entry.getValue().filename;
        if(filename.equals(current_filename)){
          return entry.getKey().chunkname.split("_")[0];
        }
    }
    return null;
  }*/

/*  public int getNumberOfChunks(String filename){
    int n_chunks = 0;

    for(Map.Entry<Key, Value> entry: this.hmap.entrySet()) {
        String current_filename = entry.getValue().filename;
        if(filename.equals(current_filename)){
          n_chunks++;
        }
    }
    return n_chunks;
  }*/

  public boolean isStored(String chunkname, int senderid){
    for(Map.Entry<Key, Integer> entry: this.hmap.entrySet()) {
        String name = entry.getKey().chunkname;
        int id = entry.getKey().senderid;
        if(name.equals(chunkname) && senderid == id)
          return true;
    }
    return false;
  }
}
