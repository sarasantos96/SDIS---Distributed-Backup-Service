import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;


public class ReplicationControl{
  private String logFileName;
  private HashMap<String, int[]> hmap;

  public ReplicationControl(String logFileName) throws IOException{
    this.hmap = new HashMap<String, int[]>();
    this.logFileName = logFileName;
    File logFile = new File(logFileName);
    if(!logFile.exists())
      logFile.createNewFile();
    else
      loadHmap();

  }

  public void addNewLog(String chunkname, int replicationDeg, int atualReplicationDeg) throws IOException{
    int [] value = {replicationDeg, atualReplicationDeg};
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
        int replicationDeg = Integer.parseInt(split[1]);
        int atualReplicationDeg = Integer.parseInt(split[2]);
        this.addNewLog(chunkname,replicationDeg,atualReplicationDeg);
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }

  public void saveHmap() throws IOException{
    String content = new String("");
    for(Map.Entry<String, int[]> entry: this.hmap.entrySet()) {
        String chunkname = entry.getKey();
        int replicationDeg = entry.getValue()[0];
        int atualReplicationDeg = entry.getValue()[1];
        content += chunkname+":"+replicationDeg+":"+atualReplicationDeg+"\n";

    }
    byte data[] = content.trim().getBytes();
    FileOutputStream out = new FileOutputStream(this.logFileName);
    out.write(data);
    out.close();
  }

  public void updateRepDeg(String chunckname) throws IOException{
    int [] oldvalue = this.hmap.get(chunckname);
    int [] newvalue = {oldvalue[0],oldvalue[1]+1};
    this.addNewLog(chunckname,newvalue[0],newvalue[1]);
  }

  public int getRepDeg(String chunckname){
    return this.hmap.get(chunckname)[0];
  }

  public int getAtualRepDeg(String chunckname){
    return this.hmap.get(chunckname)[1];
  }

  public boolean isChunkOwner(String chunckname){
    int [] value = this.hmap.get(chunckname);

    if(value != null)
      return true;

    return false;
  }
}
