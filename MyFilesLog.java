import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;


public class MyFilesLog{
  private String logFileName;
  private HashMap<String,Value> hmap;

  public MyFilesLog(String logFileName) throws IOException{
    this.hmap = new HashMap<String,Value>();
    this.logFileName = logFileName;
    File logFile = new File(logFileName);
    if(!logFile.exists())
      logFile.createNewFile();
    else
      loadHmap();
  }

  public class Value{
    public String filename;
    public int replicationDeg;
    public int nChunks;

    public Value(String filename, int replicationDeg, int nChunks){
      this.filename = filename;
      this.replicationDeg = replicationDeg;
      this.nChunks = nChunks;
    }
  }

  public void addNewLog(String fileid, String filename,int replicationDeg, int nChunks) throws IOException{
    Value value = new Value(filename,replicationDeg, nChunks);
    this.hmap.put(fileid,value);
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
        if(split.length == 4){
          String fileid = split[0];
          String filename = split[1];
          int replicationDeg = Integer.parseInt(split[2]);
          int nChunks = Integer.parseInt(split[3]);
          this.addNewLog(fileid,filename, replicationDeg, nChunks);
        }
      }
      fileReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void saveHmap() throws IOException{
    String content = new String("");
    for(Map.Entry<String, Value> entry: this.hmap.entrySet()) {
        String fileId = entry.getKey();
        String filename = entry.getValue().filename;
        int replicationDeg = entry.getValue().replicationDeg;
        int nChunks = entry.getValue().nChunks;
        content += fileId+":"+filename+":"+replicationDeg+":"+nChunks+"\n";
    }

    byte data[] = content.trim().getBytes();
    FileOutputStream out = new FileOutputStream(this.logFileName);
    out.write(data);
    out.close();
  }

  public boolean isFileOwner(String fileid){
    Value name = hmap.get(fileid);
    if(name != null){
      return true;
    }
    return false;
  }

  public int getReplicationDeg(String fileId){
    int rep = hmap.get(fileId).replicationDeg;
    return rep;
  }

  public String getFileIdByFilename(String filename){
    for(Map.Entry<String, Value> entry: this.hmap.entrySet()) {
        String current_filename = entry.getValue().filename;
        if(filename.equals(current_filename)){
          return entry.getKey().split("_")[0];
        }
    }
    return null;
  }

  public void deleteAllEntries(String fileId) throws IOException{
    for(Iterator<Map.Entry<String, Value>> i = this.hmap.entrySet().iterator(); i.hasNext(); ){
      Map.Entry<String, Value> entry = i.next();
      String chunkname = entry.getKey();
      String entryfileId = chunkname.split("_")[0];

      if(entryfileId.equals(fileId)){
        i.remove();
      }
    }
    saveHmap();
  }
}
