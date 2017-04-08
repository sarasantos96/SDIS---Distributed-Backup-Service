import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;


public class ReplicationControl{
  private String logFileName;
  private HashMap<String,Value> hmap;

  public ReplicationControl(String logFileName) throws IOException{
    this.hmap = new HashMap<String, Value>();
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
    public int atualReplicationDeg;

    public Value(String filename, int replicationDeg, int atualReplicationDeg){
      this.filename = filename;
      this.replicationDeg = replicationDeg;
      this.atualReplicationDeg = atualReplicationDeg;
    }

  }

  public void addNewLog(String filename,String chunkname, int replicationDeg, int atualReplicationDeg) throws IOException{
    Value value = new Value(filename,replicationDeg, atualReplicationDeg);
    this.hmap.put(chunkname,value);
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
        String chunkname = split[0];
        String filename = split[1];
        int replicationDeg = Integer.parseInt(split[2]);
        int atualReplicationDeg = Integer.parseInt(split[3]);
        this.addNewLog(filename,chunkname,replicationDeg,atualReplicationDeg);
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }

  public void saveHmap() throws IOException{
    String content = new String("");
    for(Map.Entry<String, Value> entry: this.hmap.entrySet()) {
        String chunkname = entry.getKey();
        String filename = entry.getValue().filename;
        int replicationDeg = entry.getValue().replicationDeg;
        int atualReplicationDeg = entry.getValue().atualReplicationDeg;
        content += chunkname+":"+filename+":"+replicationDeg+":"+atualReplicationDeg+"\n";

    }
    byte data[] = content.trim().getBytes();
    FileOutputStream out = new FileOutputStream(this.logFileName);
    out.write(data);
    out.close();
  }

  public void updateRepDeg(String chunckname) throws IOException{
    Value oldvalue = this.hmap.get(chunckname);
    this.addNewLog(oldvalue.filename,chunckname,oldvalue.replicationDeg,oldvalue.atualReplicationDeg + 1);
  }

  public int getRepDeg(String chunckname){
    return this.hmap.get(chunckname).replicationDeg;
  }

  public int getAtualRepDeg(String chunckname){
    return this.hmap.get(chunckname).atualReplicationDeg;
  }

  public boolean isChunkOwner(String chunckname){
    Value value = this.hmap.get(chunckname);

    if(value != null)
      return true;

    return false;
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

  public String getFileIdByFilename(String filename){
    for(Map.Entry<String, Value> entry: this.hmap.entrySet()) {
        String current_filename = entry.getValue().filename;
        if(filename.equals(current_filename)){
          return entry.getKey().split("_")[0];
        }
    }
    return null;
  }

  public int getNumberOfChunks(String filename){
    int n_chunks = 0;

    for(Map.Entry<String, Value> entry: this.hmap.entrySet()) {
        String current_filename = entry.getValue().filename;
        if(filename.equals(current_filename)){
          n_chunks++;
        }
    }
    return n_chunks;
  }
}
